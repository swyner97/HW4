package application;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.*;
import databasePart1.DatabaseHelper;
import javafx.collections.*;
import logic.*;
import model.*;
import pages.*;

/**
 * <p> Title: Instructor Testing </p>
 *
 * <p> Description: Showcases effective automated testing with JUnit
 *  of the Instructors responsibilites. </p>
 *
 *   <p> Copyright: Kristi Van @ 2025 </p>
 *
 *   @author Kristi Van
 *
 *   @version 0.0 2025-11-06
 */

class InstructorTesting {
    private DatabaseHelper dB;
    private InstructorHomePage instructorHomePage;
    /**
     * Initializes the test structure and how each test should be before
     * each run.
     *
     * @throws SQLException
     */

    @BeforeEach  
    public void setup() throws SQLException {
        //make an admin acc and sign in as admin
        StatusData.databaseHelper = new DatabaseHelper();
        StatusData.databaseHelper.connectToDatabase();
        User instructor = new User("instructorUser", "Password123!", User.Role.INSTRUCTOR, null, null, null);
        StatusData.currUser = instructor;
        //other users
        User student1 = new User("ash", "Password123!", User.Role.STUDENT, null, null, null);
        StatusData.databaseHelper.reviewerRequest("ash");
        User student2 = new User("bob", "Password123!", User.Role.STUDENT, null, null, null);
        StatusData.databaseHelper.reviewerRequest("bob");
        User student3 = new User("rob", "Password123!", User.Role.STUDENT, null, null, null);
        StatusData.databaseHelper.reviewerRequest("rob");
        User student4 = new User("lee", "Password123!", User.Role.STUDENT, null, null, null);
        User student5 = new User("max", "Password123!", User.Role.STUDENT, null, null, null);
    }

    /**
     * Tests that request can be correctly added into database once clicked on
     *
     * @throws SQLException if a database access error occurs
     */

    @Test
    public void reviewerRequest() throws SQLException {
        ObservableList<User> requestList = StatusData.databaseHelper.getAllReviewerRequest();
        boolean found = false;
        for (User user : requestList) {
            if (user.getUserName().equals("bob")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
    /**
     * Tests if accepting the request to be reviewer can be correctly added into database.
     *
     * @throws SQLException if a database access error occurs
     */
    
    @Test
    public void acceptRequest() throws SQLException {
        StatusData.databaseHelper.addUserRoles("ash", User.Role.REVIEWER);
        List<String> roles = StatusData.databaseHelper.allUserRoles("ash");
        assertTrue(roles.contains("reviewer"));
        ObservableList<User> requestList = StatusData.databaseHelper.getAllReviewerRequest();
        StatusData.databaseHelper.deleteReviewerRequest("ash");
        assertTrue(!requestList.contains("ash"));
    }

    /**
     * Tests once instructor accepts or denies request, it will delete the request in database
     *
     * @throws SQLException if a database access error occurs
     */

    @Test
    public void deleteRequest() throws SQLException {
        StatusData.databaseHelper.deleteReviewerRequest("bob");
        StatusData.databaseHelper.deleteReviewerRequest("rob");
        ObservableList<User> requestList = StatusData.databaseHelper.getAllReviewerRequest();
        assertTrue(!requestList.contains("bob"));
        assertTrue(!requestList.contains("rob"));
    }
}