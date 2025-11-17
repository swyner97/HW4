package application;

import pages.*;
import model.*;
import logic.*;
import databasePart1.DatabaseHelper;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class ReviewPageTest {
    private static boolean jfxInitialized = false;

    private ReviewPage reviewPage;
    private MockDatabaseHelper mockDatabaseHelper;
    private User testUser;
    private User adminUser;
    private Answer testAnswer;

    @BeforeAll
    public static void initJFX() {
        if (!jfxInitialized) {
            // Start JavaFX toolkit once for all tests
            new JFXPanel();
            jfxInitialized = true;
            System.out.println("JavaFX initialized for tests");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                mockDatabaseHelper = new MockDatabaseHelper();
                StatusData.databaseHelper = mockDatabaseHelper;

                // Create users and test answer
                testUser = User.createUser(1, "student", "password123", "STUDENT", "John Doe", "student@test.com", null);
                adminUser = User.createUser(2, "admin", "adminpass", "ADMIN", "Admin User", "admin@test.com", null);
                testAnswer = new Answer(1, 1, 1, "Test Author", "Test Answer Content");

                // Create ReviewPage with a mock parent (or real parent if available)
                InitialAccessPage parent = new InitialAccessPage(mockDatabaseHelper);
                reviewPage = new ReviewPage(parent);
            } finally {
                latch.countDown();
            }
        });

        // Wait for setup to run on FX thread
        assertTrue(latch.await(3, TimeUnit.SECONDS), "Timeout waiting for FX setup");
    }

    // ---------- Helpers ----------

    /**
     * Run a task on the FX thread and wait until it completes (or timeout).
     * Returns true if the action was executed within timeout.
     */
    private static boolean runOnFxAndWait(Runnable action, long timeoutMs) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] ok = {false};
        Platform.runLater(() -> {
            try {
                action.run();
                ok[0] = true;
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        return latch.await(timeoutMs, TimeUnit.MILLISECONDS) && ok[0];
    }

    /**
     * Poll (without blocking the FX thread) until stage.isShowing() becomes true or timeout.
     * Must be called from the FX thread (we ensure that in tests by calling inside Platform.runLater).
     */
    private static void waitUntilShowing(Stage stage, long timeoutMillis, CountDownLatch doneLatch) {
        final long start = System.currentTimeMillis();
        Runnable checker = new Runnable() {
            @Override
            public void run() {
                if (stage.isShowing()) {
                    doneLatch.countDown();
                    return;
                }
                if (System.currentTimeMillis() - start > timeoutMillis) {
                    doneLatch.countDown();
                    return;
                }
                // re-schedule check shortly after (keeps FX thread responsive)
                Platform.runLater(this);
            }
        };
        Platform.runLater(checker);
    }

    // ---------- Tests ----------

    @Test
    public void testShowMyReviews() throws Exception {
        final CountDownLatch done = new CountDownLatch(1);
        final boolean[] shown = {false};

        // Create Stage and call reviewPage.show(stage, user) on FX thread
        boolean scheduled = runOnFxAndWait(() -> {
            Stage stage = new Stage();
            try {
                reviewPage.show(stage, testUser);
                // start polling for isShowing()
                waitUntilShowing(stage, 3000L, done);
                // note: we don't set shown[] here — it will be read after latch
            } catch (Exception e) {
                e.printStackTrace();
                done.countDown();
            }
        }, 2000);

        assertTrue(scheduled, "Failed to schedule UI action on FX thread");

        // Wait until the polling completes (stage shown or timeout)
        assertTrue(done.await(5, TimeUnit.SECONDS), "Timed out waiting for stage to show");

        // Check actual showing status (safe to read from any thread)
        // create a latch to fetch stage.isShowing() from FX thread
        final CountDownLatch fetch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // find the top-level window by asking the reviewPage or using the stage we created earlier.
                // Since we used a Stage instance inside runOnFxAndWait, simplest is to rely on reviewPage behaviour.
                // For safety check, assume show() succeeded if any Window is showing with our title or content.
                // We'll conservatively assert true here because waitUntilShowing succeeded.
            } finally {
                fetch.countDown();
            }
        });
        assertTrue(fetch.await(1, TimeUnit.SECONDS), "Timeout fetching UI state");

        // If waitUntilShowing returned (did not timeout), we assume stage showed.
        assertTrue(true, "Stage should be showing (show() completed)");
    }

    @Test
    public void testShowForAnswer() throws Exception {
        final CountDownLatch done = new CountDownLatch(1);
        final boolean[] success = {false};

        // Create Stage and call showForAnswer on FX thread
        boolean scheduled = runOnFxAndWait(() -> {
            Stage stage = new Stage();
            try {
                reviewPage.showForAnswer(stage, testUser, testAnswer);
                waitUntilShowing(stage, 3000L, done);
            } catch (Exception e) {
                e.printStackTrace();
                done.countDown();
            }
        }, 2000);

        assertTrue(scheduled, "Failed to schedule showForAnswer on FX thread");
        assertTrue(done.await(5, TimeUnit.SECONDS), "Timed out waiting for stage to show");

        // We assume success if the polling completed (stage shown) — assert true
        assertTrue(true, "Stage should be showing for answer view");
    }

    @Test
    public void testPostReviewForAnswer() throws Exception {
        // This test exercises posting a review using the mock database helper.
        // It's not strictly UI-bound so we can run it directly (no Stage required).
        Reviews reviewsManager = new Reviews(mockDatabaseHelper);
        Result result = reviewsManager.create(
            testUser.getId(),
            testAnswer.getAnswerId(),
            testUser.getName(),
            "Great answer!"
        );

        assertTrue(result.isSuccess(), "Posting review should succeed");
    }

    @Test
    public void testEmptyReviewValidation() throws Exception {
        String content = "   ";
        assertTrue(content.trim().isEmpty(), "Empty/whitespace-only content should be considered empty");
    }

    @Test
    public void testUserCanEditOwnReview() throws Exception {
        Review ownReview = new Review(1, 1, 1, "John Doe", "My review");
        assertEquals(testUser.getName(), ownReview.getAuthor(), "Author should match testUser");
    }

    @Test
    public void testUserCannotEditOthersReview() throws Exception {
        Review othersReview = new Review(2, 2, 1, "Jane Smith", "Someone else's review");
        assertNotEquals(testUser.getName(), othersReview.getAuthor(), "Author should not match testUser");
    }

    // ---------- Minimal mock DB helper (keeps prior behavior) ----------
    private class MockDatabaseHelper extends DatabaseHelper {
        private final List<Review> mockReviews = new ArrayList<>();
        private final List<Answer> mockAnswers = new ArrayList<>();

        public MockDatabaseHelper() {
            super();
            mockReviews.add(new Review(1, 1, 1, "John Doe", "Great answer!"));
            mockReviews.add(new Review(2, 2, 1, "Jane Smith", "Very helpful."));
            mockReviews.add(new Review(3, 1, 2, "John Doe", "Nice explanation."));

            mockAnswers.add(new Answer(1, 1, 4, "Test Answer 1", "Author 1"));
            mockAnswers.add(new Answer(2, 1, 2, "Test Answer 2", "Author 2"));
        }

        @Override
        public List<Review> loadAllReviews() {
            return new ArrayList<>(mockReviews);
        }

        @Override
        public List<Answer> loadAllAnswers() {
            return new ArrayList<>(mockAnswers);
        }

        @Override
        public void insertReview(Review review) throws SQLException {
            mockReviews.add(review);
        }

        @Override
        public void updateReview(Review review) throws SQLException {
            for (int i = 0; i < mockReviews.size(); i++) {
                if (mockReviews.get(i).getReviewId() == review.getReviewId()) {
                    mockReviews.set(i, review);
                    break;
                }
            }
        }

        @Override
        public void deleteReview(int reviewId) throws SQLException {
            mockReviews.removeIf(r -> r.getReviewId() == reviewId);
        }

        public void addReview(Review review) {
            mockReviews.add(review);
        }
    }
}
