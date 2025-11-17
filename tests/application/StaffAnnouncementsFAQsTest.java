package application;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import model.*;
import databasePart1.DatabaseHelper;

import java.util.*;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Extended tests for staff role functions, announcements, and FAQ flows.
 * Uses an in-memory MockDatabaseHelper that simulates DB operations.
 *
 * Each test prints progress and results to the console for clarity.
 */
public class StaffAnnouncementsFAQsTest {

    private MockDatabaseHelper db;

    @BeforeEach
    public void setUp() {
        db = new MockDatabaseHelper();
        System.out.println("\n==============================");
        System.out.println("🔧 Setting up new test instance...");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("✅ Test completed successfully!");
        System.out.println("==============================\n");
    }
    
    /**
     * Helper: application-style permission check and action wrapper.
     * Returns true only if permission allowed AND DB operation succeeded.
     */
    private boolean attemptMarkFAQAsUser(User actingUser, FAQ faq) {
        if (actingUser == null) return false;
        String roleName = (actingUser.getRole() != null) ? actingUser.getRole().name().toLowerCase() : "";
        boolean isStaffOrAdmin = roleName.contains("staff") || roleName.contains("admin");
        System.out.println("AttemptMarkFAQ: user=" + actingUser.getUserName() + " role=" + roleName +
                           " -> permission=" + isStaffOrAdmin);
        if (!isStaffOrAdmin) return false;
        return db.markQuestionAsFAQ(faq);
    }

    private boolean attemptRemoveFAQAsUser(User actingUser, int questionId) {
        if (actingUser == null) return false;
        String roleName = (actingUser.getRole() != null) ? actingUser.getRole().name().toLowerCase() : "";
        boolean isStaffOrAdmin = roleName.contains("staff") || roleName.contains("admin");
        System.out.println("AttemptRemoveFAQ: user=" + actingUser.getUserName() + " role=" + roleName +
                           " -> permission=" + isStaffOrAdmin);
        if (!isStaffOrAdmin) return false;
        return db.removeQuestionFromFAQ(questionId);
    }

    @Test
    public void testFAQPermission_enforceStaffOrAdmin_only() {
        System.out.println("▶ TEST: Ensure only STAFF or ADMIN can mark a question as FAQ");

        // prepare question + solution
        Question q = new Question(0, 1, "owner", "Permission Q", "permission description");
        db.insertQuestion(q);

        // create one solution answer so questionHasSolution returns true
        Answer sol = new Answer(0, 2, q.getQuestionId(), "res", "Correct!", "2025-01-01T10:00:00", true);
        db.insertAnswer(sol);
        assertTrue(db.questionHasSolution(q.getQuestionId()), "precondition: question has solution");

        // users with different roles
        User admin = User.createUser("adminUser", "pw", User.Role.ADMIN, "Admin", "a@x", null);
        User staff = User.createUser("staffUser", "pw", User.Role.STAFF, "Staff", "s@x", null);
        User reviewer = User.createUser("revUser", "pw", User.Role.REVIEWER, "Reviewer", "r@x", null);
        User student = User.createUser("stuUser", "pw", User.Role.STUDENT, "Student", "st@x", null);

        // attempt mark with ADMIN -> should succeed
        FAQ faqAdmin = new FAQ(q.getQuestionId(), "General", "Permission Q", "note", admin.getId());
        boolean adminMarked = attemptMarkFAQAsUser(admin, faqAdmin);
        System.out.println("adminMarked = " + adminMarked);
        assertTrue(adminMarked, "ADMIN should be allowed to mark FAQ");
        assertTrue(db.isQuestionMarkedAsFAQ(q.getQuestionId()), "DB should reflect FAQ after admin mark");

        // cleanup for next check
        db.removeQuestionFromFAQ(q.getQuestionId());
        assertFalse(db.isQuestionMarkedAsFAQ(q.getQuestionId()), "clean up");

        // attempt mark with STAFF -> should succeed
        FAQ faqStaff = new FAQ(q.getQuestionId(), "General", "Permission Q", "note", staff.getId());
        boolean staffMarked = attemptMarkFAQAsUser(staff, faqStaff);
        System.out.println("staffMarked = " + staffMarked);
        assertTrue(staffMarked, "STAFF should be allowed to mark FAQ");
        assertTrue(db.isQuestionMarkedAsFAQ(q.getQuestionId()), "DB should reflect FAQ after staff mark");

        // attempt remove with REVIEWER -> should be denied (no change)
        boolean reviewerRemove = attemptRemoveFAQAsUser(reviewer, q.getQuestionId());
        System.out.println("reviewerRemove = " + reviewerRemove);
        assertFalse(reviewerRemove, "REVIEWER should NOT be allowed to remove FAQ");
        assertTrue(db.isQuestionMarkedAsFAQ(q.getQuestionId()), "DB should still have FAQ");

        // attempt mark with STUDENT -> should be denied
        FAQ faqStudent = new FAQ(q.getQuestionId(), "General", "Permission Q", "note", student.getId());
        boolean studentMarked = attemptMarkFAQAsUser(student, faqStudent);
        System.out.println("studentMarked = " + studentMarked);
        assertFalse(studentMarked, "STUDENT should NOT be allowed to mark FAQ");
        assertTrue(db.isQuestionMarkedAsFAQ(q.getQuestionId()), "DB still has previously marked FAQ");

        // now attempt remove with ADMIN -> should succeed
        boolean adminRemove = attemptRemoveFAQAsUser(admin, q.getQuestionId());
        System.out.println("adminRemove = " + adminRemove);
        assertTrue(adminRemove, "ADMIN should be allowed to remove FAQ");
        assertFalse(db.isQuestionMarkedAsFAQ(q.getQuestionId()), "DB should no longer have FAQ after admin removal");
    }

    @Test
    public void testFAQPermission_preventNonStaffFromAddingOrRemoving() {
        System.out.println("▶ TEST: Non-staff/non-admin cannot add or remove FAQs");

        // prepare question + solution
        Question q = new Question(0, 3, "owner2", "Another Q", "desc");
        db.insertQuestion(q);
        Answer sol = new Answer(0, 4, q.getQuestionId(), "res2", "Solution", "2025-01-02T10:00:00", true);
        db.insertAnswer(sol);
        assertTrue(db.questionHasSolution(q.getQuestionId()), "precondition");

        // non-privileged users
        User reviewer = User.createUser("rev2", "pw", User.Role.REVIEWER, "Rev", "r2@x", null);
        User student = User.createUser("stu2", "pw", User.Role.STUDENT, "Stu", "s2@x", null);

        FAQ faq = new FAQ(q.getQuestionId(), "Technical", "Another Q", "notes", 7);

        // both attempts should be denied and DB unchanged
        boolean revAttempt = attemptMarkFAQAsUser(reviewer, faq);
        boolean stuAttempt = attemptMarkFAQAsUser(student, faq);

        System.out.println("revAttempt=" + revAttempt + ", stuAttempt=" + stuAttempt);
        assertFalse(revAttempt, "Reviewer should not be allowed to add FAQ");
        assertFalse(stuAttempt, "Student should not be allowed to add FAQ");
        assertFalse(db.isQuestionMarkedAsFAQ(q.getQuestionId()), "DB should not mark the question");

        // Also verify removal attempt fails for non-privileged users
        boolean revRemove = attemptRemoveFAQAsUser(reviewer, q.getQuestionId());
        boolean stuRemove = attemptRemoveFAQAsUser(student, q.getQuestionId());
        assertFalse(revRemove);
        assertFalse(stuRemove);
    }


    // ---------------------------
    // User role tests
    // ---------------------------
    @Test
    public void testAddAndListUserRoles_singleUserMultipleRoles() throws SQLException {
        System.out.println("▶ TEST: Add and list multiple roles for a single user");
        String username = "alice";
        db.addUserRoles(username, User.Role.STAFF);
        db.addUserRoles(username, User.Role.ADMIN);
        db.addUserRoles(username, User.Role.REVIEWER);

        List<String> roles = db.allUserRoles(username);
        System.out.println("Roles for " + username + ": " + roles);

        assertTrue(roles.contains("STAFF"));
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("REVIEWER"));
        assertEquals(3, roles.size());
    }

    @Test
    public void testAddRoles_multipleUsersIndependentSets() throws SQLException {
        System.out.println("▶ TEST: Add roles for multiple users and verify independence");
        db.addUserRoles("u1", User.Role.STAFF);
        db.addUserRoles("u2", User.Role.ADMIN);
        db.addUserRoles("u1", User.Role.REVIEWER);

        System.out.println("Roles u1: " + db.allUserRoles("u1"));
        System.out.println("Roles u2: " + db.allUserRoles("u2"));

        assertEquals(2, db.allUserRoles("u1").size());
        assertEquals(1, db.allUserRoles("u2").size());
    }

    @Test
    public void testDeleteUserRole_edgeCases() throws SQLException {
        System.out.println("▶ TEST: Deleting user roles (including non-existent ones)");
        String username = "bob";
        db.deleteUserRole(username, User.Role.STAFF);
        db.addUserRoles(username, User.Role.STAFF);
        System.out.println("Roles before delete: " + db.allUserRoles(username));
        db.deleteUserRole(username, User.Role.STAFF);
        System.out.println("Roles after delete: " + db.allUserRoles(username));
        assertFalse(db.allUserRoles(username).contains("STAFF"));
    }

    // ---------------------------
    // FAQ & solutions tests
    // ---------------------------
    @Test
    public void testMarkQuestionAsFAQ_multipleSolutionsAndRemoval() {
        System.out.println("▶ TEST: Mark a question as FAQ, then remove it (multiple solutions)");
        Question q = new Question(0, 42, "tester", "How to Z?", "Use Z.");
        db.insertQuestion(q);

        Answer s1 = new Answer(0, 10, q.getQuestionId(), "res1", "Solution 1", "2025-01-01T08:00:00", true);
        Answer s2 = new Answer(0, 11, q.getQuestionId(), "res2", "Solution 2", "2025-01-01T09:00:00", true);
        db.insertAnswer(s1);
        db.insertAnswer(s2);

        List<Answer> sols = db.getSolutionsForQuestion(q.getQuestionId());
        System.out.println("Solutions found: " + sols.size());
        sols.forEach(a -> System.out.println(" - " + a.getAuthor() + ": " + a.getContent()));

        FAQ faq = new FAQ(q.getQuestionId(), "General", q.getTitle(), "staff reason", 123);
        boolean marked = db.markQuestionAsFAQ(faq);
        System.out.println("Marked FAQ? " + marked);

        boolean markedAgain = db.markQuestionAsFAQ(faq);
        System.out.println("Marked twice? " + markedAgain);

        boolean removed = db.removeQuestionFromFAQ(q.getQuestionId());
        System.out.println("Removed FAQ? " + removed);

        assertTrue(marked);
        assertFalse(markedAgain);
        assertTrue(removed);
    }

    @Test
    public void testMarkQuestionAsFAQ_whenNoSolution_shouldFail() {
        System.out.println("▶ TEST: Try to mark as FAQ with no solution answers");
        Question q = new Question(0, 50, "noans", "Unanswered?", "No answers yet");
        db.insertQuestion(q);
        FAQ faq = new FAQ(q.getQuestionId(), "General", q.getTitle(), "should fail", 77);
        boolean marked = db.markQuestionAsFAQ(faq);
        System.out.println("Result of marking FAQ without solution: " + marked);
        assertFalse(marked);
    }

    @Test
    public void testGetSolutionsOrder_and_singleSolutionCase() {
        System.out.println("▶ TEST: Get solutions sorted by timestamp and single-solution case");
        Question q = new Question(0, 12, "ordertest", "Order?", "Check timestamps");
        db.insertQuestion(q);

        Answer a1 = new Answer(0, 2, q.getQuestionId(), "A", "First", "2025-01-01T06:00:00", true);
        Answer a2 = new Answer(0, 3, q.getQuestionId(), "B", "Second", "2025-01-01T07:00:00", true);
        db.insertAnswer(a2);
        db.insertAnswer(a1);

        List<Answer> sols = db.getSolutionsForQuestion(q.getQuestionId());
        System.out.println("Solutions order: ");
        sols.forEach(a -> System.out.println(" - " + a.getTimestamp() + " : " + a.getContent()));

        assertEquals(2, sols.size());
        assertTrue(sols.get(0).getTimestamp().compareTo(sols.get(1).getTimestamp()) < 0);
    }

    // ---------------------------
    // Announcement tests
    // ---------------------------
    @Test
    public void testCreateAndRetrieveAnnouncements_variousPriorities() {
        System.out.println("▶ TEST: Create multiple announcements with different priorities");
        Announcement a1 = new Announcement();
        a1.setTitle("Downtime");
        a1.setContent("System maintenance tonight");
        a1.setPriority(Announcement.Priority.NORMAL);
        a1.setStartDate("2025-01-01T00:00:00");
        a1.setDisplayType(Announcement.DisplayType.SHOW_ALWAYS);
        a1.setCreatedByStaffId(99);

        Announcement a2 = new Announcement();
        a2.setTitle("Urgent DB");
        a2.setContent("DB urgent fix");
        a2.setPriority(Announcement.Priority.URGENT);
        a2.setStartDate("2025-01-02T00:00:00");
        a2.setDisplayType(Announcement.DisplayType.SHOW_ALWAYS);
        a2.setCreatedByStaffId(100);

        db.createAnnouncement(a1);
        db.createAnnouncement(a2);

        List<Announcement> all = db.getAllAnnouncements();
        System.out.println("Announcements created: " + all.size());
        all.forEach(a -> System.out.println(" - " + a.getTitle() + " (" + a.getPriority() + ")"));
        assertEquals(2, all.size());
    }

    @Test
    public void testMarkAnnouncementReadMultipleUsers_andIdempotency() {
        System.out.println("▶ TEST: Mark announcement read by multiple users and check idempotency");
        Announcement ann = new Announcement();
        ann.setTitle("Note");
        ann.setContent("Hello world");
        ann.setPriority(Announcement.Priority.NORMAL);
        ann.setStartDate("2025-01-01T00:00:00");
        ann.setDisplayType(Announcement.DisplayType.SHOW_ALWAYS);
        ann.setCreatedByStaffId(1);

        db.createAnnouncement(ann);
        int annId = db.getAllAnnouncements().get(0).getAnnouncementId();

        db.markAnnouncementAsRead(annId, 500);
        db.markAnnouncementAsRead(annId, 501);
        System.out.println("Marked read: user500=" + db.hasUserReadAnnouncement(annId, 500) +
                           ", user501=" + db.hasUserReadAnnouncement(annId, 501));

        // Re-mark to test idempotency
        db.markAnnouncementAsRead(annId, 500);
        assertTrue(db.hasUserReadAnnouncement(annId, 500));
    }

    @Test
    public void testHasUserReadAnnouncement_falseWhenNotRead() {
        System.out.println("▶ TEST: Verify unread announcement state");
        Announcement ann = new Announcement();
        ann.setTitle("Unread");
        ann.setContent("No one read yet");
        ann.setPriority(Announcement.Priority.NORMAL);
        ann.setStartDate("2025-01-01T00:00:00");
        ann.setDisplayType(Announcement.DisplayType.SHOW_ALWAYS);
        ann.setCreatedByStaffId(2);

        db.createAnnouncement(ann);
        int annId = db.getAllAnnouncements().get(0).getAnnouncementId();
        boolean result = db.hasUserReadAnnouncement(annId, 999);
        System.out.println("User 999 read? " + result);
        assertFalse(result);
    }

    // -------------------------
    // MockDatabaseHelper
    // -------------------------
    private static class MockDatabaseHelper extends DatabaseHelper {
        private final Map<Integer, Question> questions = new HashMap<>();
        private final Map<Integer, Answer> answers = new HashMap<>();
        private final List<FAQ> faqs = new ArrayList<>();
        private final List<Announcement> announcements = new ArrayList<>();
        private final Map<Integer, Set<Integer>> announcementReads = new HashMap<>();
        private final Map<String, Set<String>> userRoles = new HashMap<>();
        private int nextQuestionId = 1;
        private int nextAnswerId = 1;
        private int nextFaqId = 1;
        private int nextAnnouncementId = 1;

        // ---- role management ----
        @Override
        public void addUserRoles(String userName, User.Role role) {
            userRoles.computeIfAbsent(userName.toLowerCase(), k -> new HashSet<>()).add(role.name());
        }
        @Override
        public void deleteUserRole(String userName, User.Role role) {
            Set<String> s = userRoles.get(userName.toLowerCase());
            if (s != null) s.remove(role.name());
        }
        @Override
        public List<String> allUserRoles(String userName) {
            Set<String> s = userRoles.get(userName.toLowerCase());
            if (s == null) return new ArrayList<>();
            return new ArrayList<>(s);
        }

        // ---- Q&A management ----
        @Override
        public void insertQuestion(Question question) {
            int id = nextQuestionId++;
            question.setQuestionId(id);
            questions.put(id, question);
        }
        @Override
        public void insertAnswer(Answer answer) {
            int id = nextAnswerId++;
            answer.setAnswerId(id);
            answers.put(id, answer);
        }
        @Override
        public boolean questionHasSolution(int questionId) {
            return answers.values().stream().anyMatch(a -> a.getQuestionId() == questionId && a.isSolution());
        }
        @Override
        public List<Answer> getSolutionsForQuestion(int questionId) {
            return answers.values().stream()
                    .filter(a -> a.getQuestionId() == questionId && a.isSolution())
                    .sorted(Comparator.comparing(Answer::getTimestamp))
                    .collect(Collectors.toList());
        }
        public boolean markQuestionAsFAQ(FAQ faq) {
            if (!questionHasSolution(faq.getQuestionId())) {
                System.out.println("⚠️ Cannot mark FAQ — no solution answers exist for question " + faq.getQuestionId());
                return false;
            }
            if (faqs.stream().anyMatch(f -> f.getQuestionId() == faq.getQuestionId())) {
                System.out.println("⚠️ Question already marked as FAQ: " + faq.getQuestionId());
                return false;
            }
            faq.setFaqId(nextFaqId++);
            faqs.add(faq);
            System.out.println("✅ Marked question " + faq.getQuestionId() + " as FAQ.");
            return true;
        }
        @Override
        public boolean isQuestionMarkedAsFAQ(int questionId) {
            return faqs.stream().anyMatch(f -> f.getQuestionId() == questionId);
        }
        @Override
        public boolean removeQuestionFromFAQ(int questionId) {
            // unconditional removal for simplicity in test contexts
            return faqs.removeIf(f -> f.getQuestionId() == questionId);
        }

        @SuppressWarnings("unused")
		public boolean removeQuestionFromFAQ(int questionId, User actingUser) {
            if (actingUser == null || actingUser.getRole() == null) return false;
            String role = actingUser.getRole().name().toUpperCase();
            boolean allowed = role.equals("STAFF") || role.equals("ADMIN");
            if (!allowed) return false;
            return faqs.removeIf(f -> f.getQuestionId() == questionId);
        }


        @Override
        public List<FAQ> getAllFAQs() { return new ArrayList<>(faqs); }

        // ---- Announcements ----
        @Override
        public boolean createAnnouncement(Announcement announcement) {
            announcement.setAnnouncementId(nextAnnouncementId++);
            announcements.add(announcement);
            System.out.println("📢 Created announcement: " + announcement.getTitle());
            return true;
        }
        @Override
        public List<Announcement> getAllAnnouncements() { return new ArrayList<>(announcements); }
        @Override
        public boolean markAnnouncementAsRead(int announcementId, int userId) {
            announcementReads.computeIfAbsent(announcementId, k -> new HashSet<>()).add(userId);
            System.out.println("👀 User " + userId + " read announcement " + announcementId);
            return true;
        }
        @Override
        public boolean hasUserReadAnnouncement(int announcementId, int userId) {
            Set<Integer> s = announcementReads.get(announcementId);
            return s != null && s.contains(userId);
        }
    }
}
