package application;

import databasePart1.DatabaseHelper;
import model.User;
import pages.TrustedReviewersPage;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying functionality of both the
 * {@link databasePart1.DatabaseHelper} and {@link application.TrustedReviewersPage}.
 * <p>
 * Covers adding/removing trusted reviewers, handling default ratings,
 * and reviewer search logic for UI filtering.
 * </p>
 */
public class TylerJUnitTests {

    private DatabaseHelper db;

    private User student;
    private User reviewer1;
    private User reviewer2;

    @BeforeEach
    void setUp() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();

        // Clean tables that matter so each test starts fresh
        try (Statement stmt = db.getConnection().createStatement()) {
            stmt.executeUpdate("DELETE FROM trustedReviewers");
            stmt.executeUpdate("DELETE FROM cse360users");
        }

        // Create one student and two reviewers
        student = User.createUser("student1", "Password123!", User.Role.STUDENT,
                                  "Student One", "s1@example.com", null);
        reviewer1 = User.createUser("reviewerAmy", "Password123!", User.Role.REVIEWER,
                                    "Amy Reviewer", "amy@example.com", null);
        reviewer2 = User.createUser("reviewerBob", "Password123!", User.Role.REVIEWER,
                                    "Bob Reviewer", "bob@example.com", null);

        db.register(student);
        db.register(reviewer1);
        db.register(reviewer2);

        assertTrue(student.getId() > 0, "Student ID should be > 0 after register()");
        assertTrue(reviewer1.getId() > 0, "Reviewer 1 ID should be > 0 after register()");
        assertTrue(reviewer2.getId() > 0, "Reviewer 2 ID should be > 0 after register()");
    }

    @AfterEach
    void tearDown() {
        db.closeConnection();
    }

    // ---------- DatabaseHelper: trusted reviewer core behavior ----------

    /**
     * Tests that adding a trusted reviewer successfully inserts a new relationship
     * and that the student's trusted reviewer list contains the expected reviewer ID.
     * @throws SQLException
     */
    @Test
    void addTrustedReviewer_andGetTrustedReviewerIds() throws SQLException {
        boolean added = db.addTrustedReviewer(student.getId(), reviewer1.getId());

        assertTrue(added, "addTrustedReviewer should return true on insert.");

        List<Integer> ids = db.getTrustedReviewerIds(student.getId());
        assertEquals(1, ids.size(), "Student should have exactly 1 trusted reviewer.");
        assertEquals(reviewer1.getId(), ids.get(0), "Trusted reviewer ID should match reviewer1.");
    }

    /**
     * Tests that adding the same trusted reviewer twice does not create duplicate rows
     * in the trustedReviewers table.
     */
    @Test
    void addTrustedReviewer_twice_doesNotCreateDuplicates() throws SQLException {
        boolean first = db.addTrustedReviewer(student.getId(), reviewer1.getId());
        boolean second = db.addTrustedReviewer(student.getId(), reviewer1.getId());

        assertTrue(first, "First add should insert a row.");
        // MERGE may return 1 both times; main thing is no duplicate row exists.
        List<Integer> ids = db.getTrustedReviewerIds(student.getId());
        assertEquals(1, ids.size(), "There should be only one trusted reviewer row.");
        assertEquals(reviewer1.getId(), ids.get(0));
    }

    /**
     * Tests that a reviewer becomes trusted when added and is no longer trusted
     * after being removed.
     */
    @Test
    void isTrusted_andRemoveTrustedReviewer_flow() throws SQLException {
        db.addTrustedReviewer(student.getId(), reviewer1.getId());

        assertTrue(db.isTrusted(student.getId(), reviewer1.getId()),
                   "Reviewer1 should be trusted after add.");

        boolean removed = db.removeTrustedReviewer(student.getId(), reviewer1.getId());
        assertTrue(removed, "removeTrustedReviewer should return true when a row is deleted.");

        assertFalse(db.isTrusted(student.getId(), reviewer1.getId()),
                    "Reviewer1 should no longer be trusted after removal.");

        assertTrue(db.getTrustedReviewerIds(student.getId()).isEmpty(),
                   "Student should have no trusted reviewers after removal.");
    }

    /**
     * Tests that updating a trusted reviewer's rating to 3 is reflected correctly
     * in the retrieved ratings map.
     */
    @Test
    void updateTrustedReviewerRating_andGetTrustedReviewerRatings() throws SQLException {
        db.addTrustedReviewer(student.getId(), reviewer1.getId());

        // This mirrors what the UI does: default rating 3 after adding
        db.updateTrustedReviewerRating(student.getId(), reviewer1.getId(), 3);

        Map<Integer, Integer> ratings = db.getTrustedReviewerRatings(student.getId());
        assertEquals(1, ratings.size(), "Exactly one rating expected.");
        Integer rating = ratings.get(reviewer1.getId());
        assertNotNull(rating, "Rating should exist for reviewer1.");
        assertEquals(3, rating.intValue(), "Rating should be 3 after update.");
    }

    /**
     * Tests that calling getTrustedReviewerRatings() for a student
     * with no trusted reviewers returns an empty map.
     */
    @Test
    void getTrustedReviewerRatings_returnsEmptyMapWhenNone() throws SQLException {
        Map<Integer, Integer> ratings = db.getTrustedReviewerRatings(student.getId());
        assertNotNull(ratings, "Map should not be null.");
        assertTrue(ratings.isEmpty(), "No ratings expected for a student with no trusted reviewers.");
    }

    // ---------- Logic-only: matching reviewers by search text (from TrustedReviewersPage) ----------

    /**
     * Tests that the filterReviewerMatches method finds reviewers by partial
     * username or name and excludes the current user.
     */
    @Test
    void filterReviewerMatches_matchesByUsernameOrName_andExcludesSelf() {
        // student is current user; reviewers are possible matches
        List<User> reviewers = List.of(student, reviewer1, reviewer2);

        // search by part of name "amy" should match Amy Reviewer (in name or username)
        List<User> matches = TrustedReviewersPage.filterReviewerMatches(
                reviewers,
                "amy",
                student.getId()
        );

        assertEquals(1, matches.size(), "Should find exactly one match for 'amy'.");
        assertEquals(reviewer1.getId(), matches.get(0).getId(), "Match should be reviewer1.");
        assertNotEquals(student.getId(), matches.get(0).getId(), "Should not match current user.");
    }

    /**
     * Tests that filterReviewerMatches performs case-insensitive matching.
     */
    @Test
    void filterReviewerMatches_handlesCaseInsensitiveSearch() {
        List<User> reviewers = List.of(student, reviewer1, reviewer2);

        // "REVIEW" should match both, since they have "Reviewer" in the name
        List<User> matches = TrustedReviewersPage.filterReviewerMatches(
                reviewers,
                "REVIEW",
                student.getId()
        );

        assertEquals(2, matches.size(), "Should match both reviewers by 'REVIEW'.");
        assertTrue(matches.stream().anyMatch(u -> u.getId() == reviewer1.getId()));
        assertTrue(matches.stream().anyMatch(u -> u.getId() == reviewer2.getId()));
    }

    /**
     * Tests that filterReviewerMatches returns an empty list when no reviewers match.
     */
    @Test
    void filterReviewerMatches_returnsEmptyWhenNoMatch() {
        List<User> reviewers = List.of(student, reviewer1, reviewer2);

        List<User> matches = TrustedReviewersPage.filterReviewerMatches(
                reviewers,
                "zzz-does-not-exist",
                student.getId()
        );

        assertTrue(matches.isEmpty(), "Should return empty list when nothing matches.");
    }
}
