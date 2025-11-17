package databasePart1;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.*;
import model.User.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.SQLException;

import application.*; //we can delete this import if we don't want to have it once the messaging features get relocated


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 *
 * NOTE: Rating-related tables and methods were removed per request (only reviews themselves are stored).
 */
public class DatabaseHelper {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

    // Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            try {
                createTables();  // Create necessary tables if they don't exist
            } catch (SQLException e) {
                System.err.println("Table creation failed: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "role VARCHAR(20),"
                + "name VARCHAR(255), "
                + "email VARCHAR(255),"
                + "phone VARCHAR(20),"
                + "bio TEXT,"
                + "temp_password VARCHAR(255))";  
        
       
        // Adding a table for private messaging
        String messagesTable = "CREATE TABLE IF NOT EXISTS privateMessages ("
        		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
        		+ "sender_id INT, "
        		+ "recipient_id INT, "
        		+ "message TEXT, "
        		+ "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
        		+ "is_read BOOLEAN DEFAULT FALSE, "
        		+ "FOREIGN KEY (sender_id) REFERENCES cse360users(id), "
        		+ "FOREIGN KEY (recipient_id) REFERENCES cse360users(id))";
        
        // Questions and Answers
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
        		+ "question_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "author VARCHAR(50),"
        		+ "title VARCHAR(200),"
        		+ "description VARCHAR(5000),"
        		+ "timestamp TIMESTAMP,"
        		+ "status VARCHAR(20),"
        		+ "follow_up INT NULL,"
        		+ "user_id INT,"
        		+ "FOREIGN KEY (follow_up) REFERENCES questions(question_id))";
        
        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
        		+ "answer_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "user_id INT,"
        		+ "question_id INT,"
        		+ "author VARCHAR(50),"
        		+ "content VARCHAR(2000),"
        		+ "timestamp TIMESTAMP,"
        		+ "is_solution BOOLEAN,"
        		+ "FOREIGN KEY (question_id) REFERENCES questions(question_id))";
        
        // adding table to store clarifications 
        String clarificationsTable = "CREATE TABLE IF NOT EXISTS clarifications ("
        		+ "clarification_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "question_id INT,"
        		+ "answer_id INT,"
        		+ "author_id INT,"
        		+ "recipient_id INT,"
        		+ "author VARCHAR(50),"
        		+ "content VARCHAR(2000),"
        		+ "timestamp TIMESTAMP,"
        		+ "is_read BOOLEAN,"
        		+ "FOREIGN KEY (question_id) REFERENCES questions(question_id),"
        		+ "FOREIGN KEY (answer_id) REFERENCES answers(answer_id))";

        statement.execute(userTable);
        statement.execute(messagesTable);
        statement.execute(questionsTable);
        statement.execute(answersTable);
        statement.execute(clarificationsTable);
        
        // separate table for user having more than one role
        String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) NOT NULL, "
                + "role VARCHAR(255) NOT NULL)";
        statement.execute(userRolesTable);

        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);
        
     // separate table for invitation code roles
        String codeRolesTable = "CREATE TABLE IF NOT EXISTS CodeRoles("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "code VARCHAR(10),"
                + "initialRole VARCHAR(255))";
        statement.execute(codeRolesTable);
        
        // adding table to store trusted reviewers
        String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS trustedReviewers ("
        		+ "student_id INT NOT NULL, "
        		+ "reviewer_id INT NOT NULL, "
        		+ "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
        		+ "rating INT DEFAULT 0, "
        		+ "PRIMARY KEY (student_id, reviewer_id), "
        		+ "FOREIGN KEY (student_id) REFERENCES cse360users(id), "
        		+ "FOREIGN KEY (reviewer_id) REFERENCES cse360users(id))";
        statement.execute(trustedReviewersTable);
        
        // optimization for lookup
        String trustedIndex = "CREATE INDEX IF NOT EXISTS trustedStudentIndex "
        		+ "ON trustedReviewers(student_id)";
        statement.execute(trustedIndex);

        // Reviews table (no single 'rating' column - ratings were removed)
        String reviewsTable = "CREATE TABLE IF NOT EXISTS reviews ("
                + "review_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT,"
                + "answer_id INT,"
                + "author VARCHAR(50),"
                + "content VARCHAR(2000),"
                + "timestamp VARCHAR(30),"
                + "FOREIGN KEY (answer_id) REFERENCES answers(answer_id))";
        statement.execute(reviewsTable);

        // Note: review_ratings table and related indexes have been removed as requested.

        String requestReviewerRoleTable = "CREATE TABLE IF NOT EXISTS requestReviewerRole("
        		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
        		+  "userName VARCHAR(255),"
        		+ "role VARCHAR(255))";
        statement.execute(requestReviewerRoleTable);
        
        createFAQTable();
        createAnnouncementsTable();
        createAnnouncementReadsTable();
    }
    

    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, role, name, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRoleName()); // store enum as string
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
            	try (ResultSet keys = pstmt.getGeneratedKeys()) {
            		if (keys.next()) {
            			int newId = keys.getInt(1);
            			user.setId(newId);
            		}
            	}
            }
        }
    }

    public String loginWithOTPcheck(String userName, String enteredPw, String role) throws SQLException {
        String query = "SELECT password, temp_password FROM cse360users WHERE userName = ? AND role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, role);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String realPassword = rs.getString("password");
                String tempPassword = rs.getString("temp_password");

                if (tempPassword != null) {
                    if (enteredPw.equals(tempPassword)) {
                        clearTempPassword(userName);
                        return "temp";
                    } else {
                        return null; // OTP incorrect
                    }
                } else if (enteredPw.equals(realPassword)) {
                    return "normal";
                }
            }
        }
        return null;
    }

    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Updates an existing user in the database
    public void updateUser(User user, String oldUserName) throws SQLException {
        String updateUser = "UPDATE cse360users SET userName=?, password=?, role=?, name=?, email=? WHERE userName=?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRoleName()); // store enum as string
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, oldUserName);
            pstmt.executeUpdate();
        }
    }

    public void loadUserDetails(User user) {
        String query = "SELECT id, name, email, phone, bio FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
            	user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setBio(rs.getString("bio"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFullProfile(User user) throws SQLException {
    	String sql = "UPDATE cse360users SET userName=?, password=?, role=?, name=?, email=?, phone=?, bio=? WHERE id=?";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRoleName()); // store enum as string
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getBio());
            pstmt.setInt(8, user.getId());
    	}
	}

    public void updateUserProfile(int userId, String name, String email, String phone, String bio) throws SQLException {
        String sql = "UPDATE cse360users SET email = ?, phone = ?, bio = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, phone);
            pstmt.setString(3, bio);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
    	}
    }

    public Map<String, String> getUserProfile(int userId) throws SQLException {
        String sql = "SELECT username, role, name, email, phone, bio FROM cse360users WHERE id = ?";
        Map<String, String> profile = new HashMap<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                profile.put("username", rs.getString("userName"));
                profile.put("role", rs.getString("role"));
                profile.put("name", rs.getString("name"));
                profile.put("email", rs.getString("email"));
                profile.put("phone", rs.getString("phone"));
                profile.put("bio", rs.getString("bio"));
            }
        }
        return profile;
    }

    public String generateInvitationCode() {
        String code = UUID.randomUUID().toString().substring(0, 4);
        String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                markInvitationCodeAsUsed(code);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String generatePassword(String userName) {
        String otp = UUID.randomUUID().toString().substring(0, 8);
        String sql = "UPDATE cse360users SET temp_password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            pstmt.setString(2, userName);
            int updated = pstmt.executeUpdate();
            if (updated > 0) return otp;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // add marker to Admin's table for user's who need OTP
    public boolean requestedPw(String userName, String email) {
        String sql = "UPDATE cse360users SET temp_password = 'PENDING' WHERE userName = ? AND email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateOTP(String otp) {
        String query = "SELECT userName FROM cse360users WHERE temp_password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, otp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String userName = rs.getString("userName");
                clearTempPassword(userName);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearTempPassword(String userName) {
        String query = "UPDATE cse360users SET temp_password = NULL WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserRole(int id, String newRole) throws SQLException {
        String query = "UPDATE cse360users SET role = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public void addUserRoles(String userName, User.Role role) throws SQLException {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("userName must not be null/empty");
        }
        if (role == null || role == User.Role.UNKNOWN) {
            throw new IllegalArgumentException("role must be a valid Role enum");
        }

        String roleName = role.name(); // store enum name in DB

        String check = "SELECT COUNT(*) FROM UserRoles WHERE userName = ? AND role = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(check)) {
            checkStmt.setString(1, userName);
            checkStmt.setString(2, roleName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) { // only insert if it doesn't exist
                    String insertRole = "INSERT INTO UserRoles (userName, role) VALUES (?, ?)";
                    try (PreparedStatement pstmt = connection.prepareStatement(insertRole)) {
                        pstmt.setString(1, userName);
                        pstmt.setString(2, roleName);
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }

    //where invitation codes will assigned
    public void addRoleVIACode(String code, Role role) throws SQLException {
        String query = "INSERT INTO CodeRoles (code, initialRole) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            System.out.println("Inserting: " + role);
        }
    }

    //list all invitation codes with roles assigned
    public List<String> allCodeRoles(String code) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT initialRole FROM CodeRoles WHERE code =?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                roles.add(rs.getString("initialRole"));
            }
        }
        return roles;
    }


    public void deleteUserRole(String userName, Role role) throws SQLException {
        String query = "DELETE FROM UserRoles WHERE userName =? and role = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
        }
    }

    //List all roles for user
    public List<String> allUserRoles(String userName) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT role FROM UserRoles WHERE userName =?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                roles.add(rs.getString("role"));
            }
        }
        return roles;
    }

    public ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String query = "SELECT * FROM cse360users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String userName = rs.getString("userName");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String bio = rs.getString("bio");
                String tempPw = rs.getString("temp_password");

                //******debugging print
                System.out.println("Loaded User ID: " + id + " | Role: " + role + " | Username: " + userName);
                //******
                users.add(User.createUser(id, userName, password, role, name, email, phone, bio, tempPw));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public List<User> getAllUsersExcept(int excludeUserId) {
    	List<User> users = new ArrayList<>();
    	String query = "SELECT * FROM cse360users WHERE id != ?";
    	try (PreparedStatement pstmt = connection.prepareStatement(query)) {
    		pstmt.setInt(1, excludeUserId);
    		ResultSet rs = pstmt.executeQuery();
    		
    		while (rs.next()) {
    			int id = rs.getInt("id");
    			String userName = rs.getString("userName");
    			String password = rs.getString("password");
    			String role = rs.getString("role");
    			String name = rs.getString("name");
    			String email = rs.getString("email");
    			String phone = rs.getString("phone");
    			String bio = rs.getString("bio");
    			String tempPw = rs.getString("temp_password");
    			
    			users.add(User.createUser(id, userName, password, role, name, email, phone, bio, tempPw));
    		}    		
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return users;
    }

    //where request will be 
    public void reviewerRequest(String userName) throws SQLException{
        String query = "INSERT INTO requestReviewerRole (userName) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
            System.out.println("Inserting: " + userName);
        }
    }
    
    public void deleteReviewerRequest(String userName) throws SQLException {
        String query = "DELETE FROM requestReviewerRole WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
            System.out.println("Deleted reviewer request for user: " + userName);
        }
    }
    
    public ObservableList<User> getAllReviewerRequest() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String query = "SELECT * FROM requestReviewerRole";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String userName = rs.getString("userName");
                String role = rs.getString("role");
                users.add(User.createUser(id, userName, (String)null, role, (String)null, (String)null, (String)null));
            }
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return users;
    }

    public List<User> getUsersByRole(Role role) throws SQLException {
    	String sql = "SELECT id, userName, password, role, name, email, temp_password " +
    				 "FROM cse360users WHERE LOWER(role) = LOWER(?)";
    	
    	List<User> users = new ArrayList<>();
    	
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setString(1,  role.name());
    		try (ResultSet rs = ps.executeQuery()) {
    			while (rs.next()) {
    				User u = User.createUser(
    						rs.getInt("id"),
    						rs.getString("userName"),
    						rs.getString("password"),
    						rs.getString("role"),
    						rs.getString("name"),
    						rs.getString("email"),
    						rs.getString("temp_password")
    				);
    				users.add(u);
    			}
    			return users;
    		}
    	}
    	//System.out.println("Question author: " + question.getAuthor() + ", " + question.getUserId());
    }
    
    public User getUserById(int id) throws SQLException {
    	String sql = "SELECT id, userName, password, role, name, email, temp_password " + "FROM cse360users WHERE id = ?";
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1, id);
    		try (ResultSet rs = ps.executeQuery()) {
    			if (rs.next()) {
    				return User.createUser(
    						rs.getInt("id"),
    						rs.getString("userName"),
    		                rs.getString("password"),
    		                rs.getString("role"),
    		                rs.getString("name"),
    		                rs.getString("email"),
    		                rs.getString("temp_password")
    				);
    			}
    		}
    	}
    	return null;
    }
    
    // Searches for a User by both name and username
    public User getUserByName(String name) throws SQLException {
    	String sql = "SELECT id, userName, password, role, name, email, temp_password "
    			+ "FROM cse360users "
    			+ "WHERE LOWER(userName) = LOWER(?) "
    			+ "OR LOWER(name) = Lower(?) " 
    			+ "OR LOWER(userName) LIKE LOWER(?) "
    			+ "OR LOWER(name) LIKE LOWER(?) "
    			+ "LIMIT 1";
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		// Exact match
    		ps.setString(1, name);
    		ps.setString(2, name);
    		// Partial match
    		ps.setString(3, "%" + name + "%");
    		ps.setString(4, "%" + name + "%");
    		
    		try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return User.createUser(
                        rs.getInt("id"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("temp_password")
                    );
                }
            }
    	}
    	return null;
    }

  
  //======================Start: Questions and Answers========================================   

    // Store questions and answers
    public void insertQuestion1(Question question) throws SQLException {
    	String sql = "INSERT INTO questions (author, title, description, timestamp, status, follow_up) VALUES (?, ?, ?, ?, ?, ?)";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setString(1, question.getAuthor());
    		pstmt.setString(2, question.getTitle());
    		pstmt.setString(3, question.getDescription());
    		pstmt.setString(4, question.getTimestamp());
    		pstmt.setString(5, question.getStatus());
    		if (question.getFollowUp() > 0) {
    			pstmt.setInt(6, question.getFollowUp());
    		}
    	}
    }
    public void insertQuestion(Question question) throws SQLException {
    	String sql = "INSERT INTO questions (author, title, description, timestamp, status, follow_up, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    		pstmt.setString(1, question.getAuthor());
    		pstmt.setString(2, question.getTitle());
    		pstmt.setString(3, question.getDescription());
    		pstmt.setString(4, question.getTimestamp());
    		pstmt.setString(5, question.getStatus());
    		if (question.getFollowUp() > 0) {
    			pstmt.setInt(6, question.getFollowUp());
    		}
    		else {
    			pstmt.setNull(6, Types.INTEGER);
    		}
    		pstmt.setInt(7, question.getUserId());
    		
    		int rowsInserted = pstmt.executeUpdate();
    		System.out.println("Rows inserted: " + rowsInserted);
    		
    		// Add the auto generated ID number to the question object
    		ResultSet generatedKeys = pstmt.getGeneratedKeys();
    		if (generatedKeys.next()) {
    			int generateId = generatedKeys.getInt(1);
    			question.setQuestionId(generateId);
    		}
    		System.out.println("Question author: " + question.getAuthor() + ", userId: " + question.getUserId());
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    		System.err.println("insertQuestion failed: " + e.getMessage());
    	}
    }

    public void insertAnswer(Answer answer) throws SQLException {
        String sql = "INSERT INTO answers (user_id, question_id, author, content, timestamp, is_solution) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answer.getUserId());
            pstmt.setInt(2, answer.getQuestionId());
            pstmt.setString(3, answer.getAuthor());
            pstmt.setString(4, answer.getContent());
            pstmt.setString(5, answer.getTimestamp());
            pstmt.setBoolean(6, answer.isSolution());
            pstmt.executeUpdate();
        }
    }
    
    public Question getQuestionById(int questionId) throws SQLException {
    	String sql = "SELECT question_id, user_id, author, title, description, timestamp, status, follow_up " + "FROM questions WHERE question_id = ?";
    	 try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    	        pstmt.setInt(1, questionId);
    	        try (ResultSet rs = pstmt.executeQuery()) {
    	            if (rs.next()) {
    	                int id = rs.getInt("question_id");
    	                int userId = rs.getInt("user_id");
    	                String author = rs.getString("author");
    	                String title = rs.getString("title");
    	                String description = rs.getString("description");
    	                String timestamp = rs.getString("timestamp");
    	                String status = rs.getString("status");
    	                int followUp = rs.getInt("follow_up");
    	                
    	                Question q = new Question(id, userId, author, title, description);
    	                q.setTimestamp(timestamp);
    	                q.setStatus(status);
    	                q.setFollowUp(followUp);
    	                return q;
    	            }
    			}
    		} catch (SQLException e) {
        		e.printStackTrace();
        		System.err.println("insertQuestion failed: " + e.getMessage());
        	}
    	
    	return null;
    }
    
    public Question getQuestionByUser(String userName) throws SQLException{
    	String sql = "SELECT question_id, user_id, author, title, description, timestamp, status, follow_up " + "FROM questions WHERE author = ?";
   	 	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
   	        pstmt.setString(1, userName);
   	        try (ResultSet rs = pstmt.executeQuery()) {
   	            if (rs.next()) {
   	                int id = rs.getInt("question_id");
   	                int userId = rs.getInt("user_id");
   	                String author = rs.getString("author");
   	                String title = rs.getString("title");
   	                String description = rs.getString("description");
   	                String timestamp = rs.getString("timestamp");
   	                String status = rs.getString("status");
   	                int followUp = rs.getInt("follow_up");
   	                
   	                Question q = new Question(id, userId, author, title, description);
   	                q.setTimestamp(timestamp);
   	                q.setStatus(status);
   	                q.setFollowUp(followUp);
   	                return q;
   	            }
   			}
   		} 
   	
   	return null;
   }
  

    public Answer getAnswerById(int answerId) throws SQLException {
        String sql = "SELECT answer_id, user_id, question_id, author, content, timestamp, is_solution "
                   + "FROM answers WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("answer_id");
                    int userId = rs.getInt("user_id");
                    int questionId = rs.getInt("question_id");
                    String author = rs.getString("author");
                    String content = rs.getString("content");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    boolean isSolution = rs.getBoolean("is_solution");
                    
                    Answer a = new Answer(id, userId, questionId, author, content, ts.toLocalDateTime().toString(), isSolution);
                    return a;
                }
            }
        } catch (SQLException e) {
    		e.printStackTrace();
    	}
        return null;
    }
    
    public List<Question> loadAllQs() {

    	List<Question> questions = new ArrayList<>();
    	String sql = "SELECT * FROM questions";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			int id = rs.getInt("question_id");
    			String author = rs.getString("author");
    			String title = rs.getString("title");
    			String description = rs.getString("description");
    			String timestamp = rs.getString("timestamp");
    			String status = rs.getString("status");
    			int followUp = rs.getInt("follow_up");
    			int userId = rs.getInt("user_id");
    			if (rs.wasNull()) { followUp = 0; }
    			
    			Question q = new Question(id, userId, author, title, description, timestamp, status, new ArrayList<>());
    			//add timestamp and status when needed
    			q.setTimestamp(timestamp);
    			q.setStatus(status);
    			q.setFollowUp(followUp);
    			
    			//Attach the answers and load them
    			List<Answer> answers = loadAnswersForQs(id);
    			q.setAnswers(answers);
    			
    			questions.add(q);
    		}
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return questions;
    }

    //Load all answers
    public List<Answer> loadAllAnswers() {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int answerId = rs.getInt("answer_id");
                int userId = rs.getInt("user_id");
                int questionId = rs.getInt("question_id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");
                boolean isSolution = rs.getBoolean("is_solution");

                Answer a = new Answer(answerId, userId, questionId, author, content, timestamp, isSolution);
                answers.add(a);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading answers: " + e.getMessage());
        }
        return answers;
    }

    //Load answers for questions
    public List<Answer> loadAnswersForQs(int questionId) {
    	List<Answer> answers = new ArrayList<>();
    	String sql = "SELECT * FROM answers WHERE question_id = ?";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setInt(1, questionId);
    		ResultSet rs = pstmt.executeQuery();
    		
    		while (rs.next()) {
    			int answerId = rs.getInt("answer_id");
    			int userId = rs.getInt("user_id");
    			String author = rs.getString("author");
    			String content = rs.getString("content");
    			String timestamp = rs.getString("timestamp");
    			boolean isSolution = rs.getBoolean("is_solution");
    			
    			Answer answer = new Answer(answerId, userId, questionId, author, content, timestamp, isSolution);

                answers.add(answer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public List<Question> searchQuestions(String keyword, String status, String author) {
        List<Question> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE 1=1");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
        }

        if (author != null && !author.isBlank()) {
            sql.append(" AND LOWER(author) LIKE ?");
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int index = 1;

            if (keyword != null && !keyword.isBlank()) {
                pstmt.setString(index++, "%" + keyword + "%");
                pstmt.setString(index++, "%" + keyword + "%");
            }
            if (status != null && !status.isBlank()) {
                pstmt.setString(index++, status);
            }
            if (author != null && !author.isBlank()) {
                pstmt.setString(index++, "%" + author.toLowerCase() + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("question_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String auth = rs.getString("author");
                String time = rs.getString("timestamp");
                String stat = rs.getString("status");
                int followUp = rs.getInt("follow_up");
                if (rs.wasNull()) {
                    followUp = 0;
                }

                Question q = new Question(id, -1, auth, title, description);
                q.setTimestamp(time);
                q.setStatus(stat);
                q.setFollowUp(followUp);
                q.setAnswers(loadAnswersForQs(id));
                result.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Search answers
    public List<Answer> searchAnswers(String keyword, String author, Boolean isSolution) {
        List<Answer> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM answers WHERE 1=1");

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND LOWER(content) LIKE ?");
        }
        if (author != null && !author.isBlank()) {
            sql.append(" AND LOWER(author) LIKE ?");
        }
        if (isSolution != null) {
            sql.append(" AND is_solution = ?");
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (keyword != null && !keyword.isBlank()) {
                pstmt.setString(paramIndex++, "%" + keyword.toLowerCase() + "%");
            }
            if (author != null && !author.isBlank()) {
                pstmt.setString(paramIndex++, "%" + author.toLowerCase() + "%");
            }
            if (isSolution != null) {
                pstmt.setBoolean(paramIndex++, isSolution);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int answerId = rs.getInt("answer_id");
                int userId = rs.getInt("user_id");
                int questionId = rs.getInt("question_id");
                String ansAuthor = rs.getString("author");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");
                Boolean solution = rs.getBoolean("is_solution");

                Answer a = new Answer(answerId, -1, questionId, ansAuthor, content, timestamp, solution);
                results.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error searching answers: " + e.getMessage());
        }
        return results;

    }

    // Update existing question (title, description, status)
    public void updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE questions SET title = ?, description = ?, status = ?, timestamp = ? WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getDescription());
            pstmt.setString(3, question.getStatus());
            pstmt.setString(4, question.getTimestamp());
            pstmt.setInt(5, question.getQuestionId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                System.err.println("⚠️ No question found with ID: " + question.getQuestionId());
            } else {
                System.out.println("✅ Question " + question.getQuestionId() + " updated successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ updateQuestion failed: " + e.getMessage());
            throw e;
        }
    }

    // Delete a question by ID
    public void deleteQuestion(int questionId) throws SQLException {
        // First delete all answers associated with this question
        String deleteAnswers = "DELETE FROM answers WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswers)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }

        // Then delete the question itself
        String deleteQuestion = "DELETE FROM questions WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuestion)) {
            pstmt.setInt(1, questionId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted == 0) {
                System.err.println("⚠️ No question found with ID: " + questionId);
            } else {
                System.out.println("✅ Question " + questionId + " deleted successfully!");
            }
        }
    }

    // Delete an answer by ID
    public void deleteAnswer(int answerId) throws SQLException {
        String deleteAnswer = "DELETE FROM answers WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswer)) {
            pstmt.setInt(1, answerId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted == 0) {
                System.err.println("⚠️ No answer found with ID: " + answerId);
            } else {
                System.out.println("✅ Answer " + answerId + " deleted successfully!");
            }
        }
    }

    // Update an existing answer in the database
    public void updateAnswer(Answer answer) throws SQLException {
        String sql = "UPDATE answers SET content = ?, is_solution = ? WHERE answer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, answer.getContent());
            pstmt.setBoolean(2, answer.isSolution());
            pstmt.setInt(3, answer.getAnswerId());

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated == 0) {
                System.err.println("⚠️ No answer found with ID: " + answer.getAnswerId());
            } else {
                System.out.println("✅ Answer " + answer.getAnswerId() + " updated successfully!");
            }
        }
    }

    public List<Question> getQuestionsByUser(String username) {
        List<Question> questionsByUser = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE author = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("question_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String author = rs.getString("author");
                String timestamp = rs.getString("timestamp");
                String status = rs.getString("status");
                int followUp = rs.getInt("follow_up");
                if (rs.wasNull()) followUp = 0;
                
                Question q = new Question(id, -1, author, title, description);
                q.setTimestamp(timestamp);
                q.setStatus(status);
                q.setFollowUp(followUp);

                q.setAnswers(loadAnswersForQs(id));
                
                questionsByUser.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return questionsByUser;
    }
    
    public List<Answer> getAnswersByUser(String username) {
        List<Answer> answersByUser = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE LOWER(author) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int answerId = rs.getInt("answer_id");
                int userId = rs.getInt("user_id");
                int questionId = rs.getInt("question_id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");
                boolean isSolution = rs.getBoolean("is_solution");
                
                Answer a = new Answer(answerId, userId, questionId, author, content, timestamp, isSolution);
                answersByUser.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answersByUser;
    }
    
    /**
     * Create the faqs table if it does not exist.
     *
     * The table stores FAQ entries created by staff. Each row references a
     * question_id from the questions table and records who (staff id) created
     * the FAQ and when it was created.
     *
     * @throws SQLException if table creation fails
     */
    private void createFAQTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS faqs (" +
                     "faq_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "question_id INT NOT NULL, " +
                     "category VARCHAR(100) NOT NULL, " +
                     "display_title VARCHAR(500), " +
                     "staff_notes TEXT, " +
                     "date_marked TIMESTAMP NOT NULL, " +
                     "marked_by_staff_id INT NOT NULL, " +
                     "FOREIGN KEY (question_id) REFERENCES questions(question_id), " +
                     "FOREIGN KEY (marked_by_staff_id) REFERENCES cse360users(id)" +
                     ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
//======================Begin: Clarification functions====================================
    //Insert Clarifictaion
    public void insertClarification(Clarification clarification) throws SQLException {
    	String sql = "INSERT INTO clarifications (question_id, answer_id, author_id, recipient_id, author, content, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    	 try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    	        pstmt.setObject(1, clarification.getQuestionId() > 0 ? clarification.getQuestionId() : null);
    	        if (clarification.getAnswerId() > 0) {
    	        	pstmt.setObject(2, clarification.getAnswerId());
    	        } else {
    	        	pstmt.setNull(2, Types.INTEGER);
    	        }
    	        pstmt.setInt(3, clarification.getAuthorId());
    	        pstmt.setInt(4, clarification.getRecipientId());
    	        pstmt.setString(5, clarification.getAuthor());
    	        pstmt.setString(6, clarification.getContent());
    	        pstmt.setTimestamp(7, Timestamp.valueOf(clarification.getTimestamp()));
    	        pstmt.setBoolean(8, clarification.isRead());
    	        
    	        pstmt.executeUpdate();
    	 }
    }

    // Load clarifications logic
    public List<Clarification> loadClarifications(String type, int id) throws SQLException {
    	List<Clarification> clarifications = new ArrayList<>();
    	String sql;

    	switch (type) {
    		case "question_id":
	    		sql = "SELECT * FROM clarifications WHERE question_id = ?";
	    		break;
    		case "answer_id":
    			sql = (id > 0)
        		? "SELECT * FROM clarifications WHERE answer_id = ?"
        		: "SELECT * FROM clarifications WHERE answer_id IS NULL";
    		case "recipient_id":
    			sql = "SELECT * FROM clarifications WHERE recipient_id = ?";
    			break;
    		default:
    			throw new IllegalArgumentException("Invalid clarification type: " + type);
    	}
    	
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setInt(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			//clarifications.add(new Clarification (
    			Clarification c = new Clarification(		
    					rs.getInt("clarification_id"),
    					rs.getInt("question_id"),
    					rs.getInt("answer_id"),
    					rs.getInt("author_id"),
    					rs.getInt("recipient_id"),
    					rs.getString("author"),
    					rs.getString("content"),
    					rs.getTimestamp("timestamp").toLocalDateTime(),
    					rs.getBoolean("is_read")
    			);
    			
    			Question q = getQuestionById(c.getQuestionId());
    			c.setQuestionTitle(q != null ? q.getTitle() : "[Unknown Question]");
    			clarifications.add(c);
    		}
    	}
    	
    	return clarifications;
    }

    // Load clarifications for question
    public List<Clarification> loadClarificationsforQ(int questionId) throws SQLException {
        return loadClarifications("question_id", questionId);
    }

    // Load clarifications for answer
    public List<Clarification> loadClarificationsforA(int answerId) throws SQLException {
        return loadClarifications("answer_id", answerId);
    }
    
    public List<Clarification> loadClarificationsForUser(int recipientId) throws SQLException {
    	return loadClarifications("recipient_id", recipientId);
    }
    
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void markClarificationAsRead(int clarificationId) throws SQLException {
    	String sql = "UPDATE clarifications SET is_read = TRUE WHERE id=?";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setInt(1, clarificationId);
    		pstmt.executeUpdate();
    	} catch (SQLException e) {
            System.err.println("Error marking clarifications as read: " + e.getMessage());
        }
    }
    
  //======================End: Clarification functions========================================
    
  //======================Begin: Private Message functions====================================
    public void sendMessage(Messages msg) {
    	
        if (msg.getMessage() == null || msg.getMessage().trim().isEmpty()) {
            System.out.println("Cannot send empty message.");
            return;
        }
        String content = msg.getMessage().trim();
        if (content.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING, "Message cannot be empty.");
			alert.show();
			return;
		}

    	try {
    		String sql = "INSERT INTO privateMessages (sender_id, recipient_id, message, timestamp, is_read) VALUES (?, ?, ?, ?, ?)";
    		PreparedStatement pstmt = connection.prepareStatement(sql);
    		pstmt.setInt(1, msg.getSenderId());
    		pstmt.setInt(2, msg.getRecipientId());
    		pstmt.setString(3, msg.getMessage());
    		pstmt.setTimestamp(4, Timestamp.valueOf(msg.getTimestamp()));
    		pstmt.setBoolean(5, msg.isRead());
    		pstmt.executeUpdate();
    	}
    	catch (SQLException e) {
    		System.err.println("Error sending message: " + e.getMessage());
    		e.printStackTrace();
    	}
    }
    
    public List<Messages> getMessagesForUser(int userId) {
    	
    	
    	List<Messages> messages = new ArrayList<>();
    	try {
    		//join messages table with users table to be able to get sender's name via sender_id
        	String sql = "SELECT m.id, m.sender_id, m.recipient_id, m.message, m.timestamp, m.is_read, " +
        	"COALESCE(u.name, u.userName) AS sender_name " +
        	"FROM privateMessages m " +
        	"JOIN cse360users u ON m.sender_id = u.id " +
        	"WHERE m.recipient_id=? " +
        	"ORDER BY m.timestamp DESC";
        	PreparedStatement pstmt = connection.prepareStatement(sql);
    		pstmt.setInt(1, userId);
    		
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			Messages msg = new Messages(
    					rs.getInt("id"),
    					rs.getInt("sender_id"),
    					rs.getInt("recipient_id"),
    					rs.getString("message"),
    					rs.getTimestamp("timestamp").toLocalDateTime(),
    					rs.getBoolean("is_read")
    			);
    			msg.setSenderName(rs.getString("sender_name"));
    			messages.add(msg);
    		}
    	} catch (SQLException e) {
    		System.err.println("Error retrieving inbox for user " + userId + ": " + e.getMessage());
    		e.printStackTrace();
    	}
    	return messages;
    }
    
    public List<Messages> getSentMessagesForUser(int userId) {
        
        List<Messages> sentMessages = new ArrayList<>();
        try {
        	String sql = "SELECT m.id, m.sender_id, m.recipient_id, m.message, m.timestamp, m.is_read, " +
                    "COALESCE(u.name, u.userName) AS recipient_name " +
                    "FROM privateMessages m " +
                    "JOIN cse360users u ON m.recipient_id = u.id " +
                    "WHERE m.sender_id = ? " +
                    "ORDER BY m.timestamp DESC";
        	PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Messages msg = new Messages(
                        rs.getInt("id"),
                        rs.getInt("sender_id"),
                        rs.getInt("recipient_id"),
                        rs.getString("message"),
                        rs.getTimestamp("timestamp").toLocalDateTime(),
                        rs.getBoolean("is_read")
                );
                msg.setRecipientName(rs.getString("recipient_name"));
                sentMessages.add(msg);
            }
        } catch (SQLException e) {
    		System.err.println("Error retrieving sent messages for user " + userId + ": " + e.getMessage());
    		e.printStackTrace();
    	}
        return sentMessages;
    }
    
    public List<Messages> getMessagesBetweenUsers(int user1, int user2) {
    	List<Messages> messages = new ArrayList<>();
    	
    	try {
    		String sql = "SELECT * FROM privateMessages WHERE " +
        			"((sender_id=? AND recipient_id=?) OR (sender_id=? AND recipient_id=?)) " +
        			"ORDER BY timestamp DESC";
    		PreparedStatement pstmt = connection.prepareStatement(sql);
    		pstmt.setInt(1, user1);
    		pstmt.setInt(2, user2);
    		pstmt.setInt(3, user2);
    		pstmt.setInt(4, user1);
    		
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			messages.add(new Messages(
    					rs.getInt("id"),
    					rs.getInt("sender_id"),
    					rs.getInt("recipient_id"),
    					rs.getString("message"),
    					rs.getTimestamp("timestamp").toLocalDateTime(),
    					rs.getBoolean("is_read")
    			));
    		}
    	} catch (SQLException e) {
    		System.err.println("Error retrieving messages between users " + user1 + ", " + user2 + ": " + e.getMessage());
    		e.printStackTrace();
    	}
    	return messages;
    }
    
    public void markMessagesAsRead(int messageId) throws SQLException {
    	String sql = "UPDATE privateMessages SET is_read = TRUE WHERE id=?";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setInt(1, messageId);
    		pstmt.executeUpdate();
    	} catch (SQLException e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
        }
    }
    
    //Reviewers can send private messages to authors of quesitons and answers
    
    
    //********Insert test user for debugging
   /* public User insertTestUser() throws SQLException {
    	String sql = "INSERT INTO cse360users (id, userName, password, role, name, email, phone, bio, temp_password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	 try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    	        pstmt.setInt(1, 9);  // ID
    	        pstmt.setString(2, "testUser1");  
    	        pstmt.setString(3, "testPassword1");  
    	        pstmt.setString(4, "user"); 
    	        pstmt.setString(5, "Test User"); 
    	        pstmt.setString(6, "test@example.com");
    	        pstmt.setString(7, "123-456-7890"); 
    	        pstmt.setString(8, "This is a test user.");  
    	        pstmt.setString(9, null);

    	        int rowsInserted = pstmt.executeUpdate();
    	        System.out.println("Inserted test user, rows affected: " + rowsInserted);
    	 } catch (SQLException e) {
    	        e.printStackTrace();
    	 }
    	 return null;
    }    */
//======================End: Private Message functions====================================
    
//======================Begin: Trusted Reviewer Functions=================================
    
    public boolean addTrustedReviewer(int studentId, int reviewerId) throws SQLException {
    	String sql = "MERGE INTO trustedReviewers (student_id, reviewer_id) "
    			+ "KEY (student_id, reviewer_id) VALUES (?, ?)";
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1,  studentId);
    		ps.setInt(2, reviewerId);
    		return ps.executeUpdate() > 0;
    	}
    }
    
    public boolean removeTrustedReviewer(int studentId, int reviewerId) throws SQLException {
    	String sql = "DELETE FROM trustedReviewers WHERE student_id=? AND reviewer_id=?";
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1, studentId);
    		ps.setInt(2, reviewerId);
    		return ps.executeUpdate() > 0;
    	}
    }
    
    public List<Integer> getTrustedReviewerIds(int studentId) throws SQLException {
    	String sql = "SELECT reviewer_id FROM trustedReviewers WHERE student_id=?";
    	List<Integer> ids = new ArrayList<>();
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1,  studentId);
    		try (ResultSet rs = ps.executeQuery()) {
    			while (rs.next()) ids.add(rs.getInt(1));
    		}
    	}
    	return ids;
    }
    
    public boolean isTrusted(int studentId, int reviewerId) throws SQLException {
    	String sql = "SELECT 1 FROM trustedReviewers WHERE student_id=? AND reviewer_id=? LIMIT 1";
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1,  studentId);
    		ps.setInt(2,  reviewerId);
    		try (ResultSet rs = ps.executeQuery()) {
    			return rs.next();
    		}
    	}
    }
    
    public void updateTrustedReviewerRating(int studentId, int reviewerId, int rating) throws SQLException {
    	String sql = "UPDATE trustedReviewers SET rating = ? " +
    				"WHERE student_id = ? AND reviewer_id = ?";
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1, rating);
    		ps.setInt(2, studentId);
    		ps.setInt(3, reviewerId);
    		ps.executeUpdate();
    	}
    }
    
    public Map<Integer, Integer> getTrustedReviewerRatings(int studentId) throws SQLException {
    	String sql = "SELECT reviewer_id, rating FROM trustedReviewers WHERE student_id=?";
    	Map<Integer, Integer> map = new HashMap<>();
    	try (PreparedStatement ps = connection.prepareStatement(sql)) {
    		ps.setInt(1, studentId);
    		try (ResultSet rs = ps.executeQuery()) {
    			while (rs.next()) {
    				int reviewerId = rs.getInt("reviewer_id");
    				int rating = rs.getInt("rating");
    				map.put(reviewerId, rating);
    			}
    		}
    	}
    	return map;
    }
    
//======================End: Trusted Reviewer Functions===================================

//======================Reviews ==========================================================

    public List<Review> loadAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int reviewId = rs.getInt("review_id");
                int userId = rs.getInt("user_id");
                int answerId = rs.getInt("answer_id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                Review r = new Review(reviewId, userId, answerId, author, content);
                // If your Review class supports timestamp, set it here:
                // r.setTimestamp(timestamp);
                reviews.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading reviews: " + e.getMessage());
        }
        return reviews;
    }

    /**
     * Insert a Review (no rating column).
     * Upon success, the generated review_id is set into the Review object (if Review has a setter).
     */
    public void insertReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (user_id, answer_id, author, content, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (review.getUserId() > 0) {
                pstmt.setInt(1, review.getUserId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            if (review.getAnswerId() > 0) {
                pstmt.setInt(2, review.getAnswerId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setString(3, review.getAuthor());
            pstmt.setString(4, review.getContent());

            // timestamp (if Review provides one, otherwise generate)
            String ts = null;
            try {
                ts = review.getTimestamp();
            } catch (NoSuchMethodError ignored) { /* ignore if Review doesn't expose timestamp */ }

            if (ts == null || ts.isBlank()) {
                ts = String.valueOf(System.currentTimeMillis());
            }
            pstmt.setString(5, ts);

            int rows = pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    try {
                        review.setReviewId(generatedId); // assumes Review has setter
                    } catch (NoSuchMethodError ignored) {
                        // if Review does not have setter, ignore
                    }
                }
            }
        }
    }

    // Load reviews for a specific answer
    public List<Review> loadReviewsForAnswer(int answerId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int reviewId = rs.getInt("review_id");
                int userId = rs.getInt("user_id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                Review review = new Review(reviewId, userId, answerId, author, content);
                // if Review has setTimestamp, apply it
                // review.setTimestamp(timestamp);
                reviews.add(review);
            }
        }
        return reviews;
    }

    // Update an existing review
    public void updateReview(Review review) throws SQLException {
        String sql = "UPDATE reviews SET content = ?, timestamp = ? WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, review.getContent());
            // timestamp parameter
            try {
                pstmt.setString(2, review.getTimestamp());
            } catch (NoSuchMethodError ignored) {
                pstmt.setString(2, String.valueOf(System.currentTimeMillis()));
            }
            pstmt.setInt(3, review.getReviewId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                System.err.println("⚠️ No review found with ID: " + review.getReviewId());
            } else {
                System.out.println("✅ Review " + review.getReviewId() + " updated successfully!");
            }
        }
    }

    // Delete a review by ID
    public void deleteReview(int reviewId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted == 0) {
                System.err.println("⚠️ No review found with ID: " + reviewId);
            } else {
                System.out.println("✅ Review " + reviewId + " deleted successfully!");
            }
        }
    }

    /**
     * Mark a question as FAQ, but only if the acting user has STAFF or ADMIN role.
     *
     * @param faq the FAQ object to create
     * @param actingUser the user attempting the action
     * @return true if FAQ was created successfully; false if unauthorized or failed
     */
    public boolean markQuestionAsFAQ(FAQ faq, User actingUser) {
        if (actingUser == null || actingUser.getRole() == null) {
            System.err.println("❌ Unauthorized: null user");
            return false;
        }

        String roleName = actingUser.getRole().name().toUpperCase();
        boolean isAuthorized = roleName.equals("STAFF") || roleName.equals("ADMIN");

        if (!isAuthorized) {
            System.err.println("❌ Unauthorized: " + actingUser.getUserName() + " (" + roleName + ")");
            return false;
        }

        // Ensure the question has at least one solution
        if (!questionHasSolution(faq.getQuestionId())) {
            System.err.println("⚠️ Cannot mark FAQ — no solution answers exist.");
            return false;
        }

        String sql = "INSERT INTO faqs (question_id, category, display_title, staff_notes, marked_by_staff_id, date_marked) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, faq.getQuestionId());
            pstmt.setString(2, faq.getCategory());
            pstmt.setString(3, faq.getDisplayTitle());
            pstmt.setString(4, faq.getStaffNotes());
            pstmt.setInt(5, actingUser.getId());
            int rows = pstmt.executeUpdate();
            System.out.println("✅ FAQ marked by " + actingUser.getRole() + ": " + rows + " row(s) inserted.");
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns whether the provided question is already present in the faqs table.
     *
     * @param questionId the question id to check
     * @return true if the question is already marked as an FAQ, false otherwise
     */
    public boolean isQuestionMarkedAsFAQ(int questionId) {
        String sql = "SELECT COUNT(*) FROM faqs WHERE question_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking FAQ status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Backwards-compatible removal without an acting user.
     * This removes the FAQ entry unconditionally (no permission check).
     * Use carefully — prefer removeQuestionFromFAQ(int, User) when permissions matter.
     *
     * @param questionId the FAQ question id to remove
     * @return true if removed successfully, false otherwise
     */
    public boolean removeQuestionFromFAQ(int questionId) {
        String sql = "DELETE FROM faqs WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            int rows = pstmt.executeUpdate();
            System.out.println("🗑️ FAQ removed (no-actor): " + rows + " row(s) deleted.");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error removing FAQ (no-actor): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load all FAQ entries.
     *
     * Returns FAQ objects enriched with the question title (if available) and
     * ordered by category and the date they were marked.
     *
     * @return List of FAQ objects (empty list if none)
     */
    public List<FAQ> getAllFAQs() {
        List<FAQ> faqs = new ArrayList<>();
        String sql = "SELECT f.*, q.title as question_title " +
                     "FROM faqs f " +
                     "LEFT JOIN questions q ON f.question_id = q.question_id " +
                     "ORDER BY f.category, f.date_marked DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FAQ faq = new FAQ();
                faq.setFaqId(rs.getInt("faq_id"));
                faq.setQuestionId(rs.getInt("question_id"));
                faq.setCategory(rs.getString("category"));
                faq.setDisplayTitle(rs.getString("display_title"));
                faq.setStaffNotes(rs.getString("staff_notes"));
                faq.setDateMarked(rs.getString("date_marked"));
                faq.setMarkedByStaffId(rs.getInt("marked_by_staff_id"));
                faq.setQuestionTitle(rs.getString("question_title"));  // Add this
                faqs.add(faq);
            }
        } catch (SQLException e) {
            System.err.println("Error loading FAQs: " + e.getMessage());
        }
        return faqs;
    }
    
    /**
     * Update FAQ details
     */
    public boolean updateFAQ(FAQ faq) {
        String sql = "UPDATE faqs SET category = ?, display_title = ?, staff_notes = ? WHERE faq_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, faq.getCategory());
            pstmt.setString(2, faq.getDisplayTitle());
            pstmt.setString(3, faq.getStaffNotes());
            pstmt.setInt(4, faq.getFaqId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating FAQ: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Returns true if any answer for the given question is marked as a solution.
     *
     * @param questionId the question id to check
     * @return true if at least one answer has is_solution = TRUE
     */
    public boolean questionHasSolution(int questionId) {
        String sql = "SELECT COUNT(*) FROM answers WHERE question_id = ? AND is_solution = TRUE";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking for solution: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get the solution answer for a question
     */
    public Answer getSolutionForQuestion(int questionId) {
    	 String sql = "SELECT * FROM answers WHERE question_id = ? AND is_solution = TRUE LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int answerId = rs.getInt("answer_id");
                int userId = rs.getInt("user_id");
                int qId = rs.getInt("question_id");
                String author = rs.getString("author");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");
                boolean isSolution = rs.getBoolean("is_solution");
                
                // Use the Answer constructor that matches your class
                Answer answer = new Answer(answerId, userId, qId, author, content, timestamp, isSolution);
                return answer;
            }
        } catch (SQLException e) {
            System.err.println("Error getting solution: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Return all answers for a question that are marked as solutions.
     *
     * @param questionId the question id to load solutions for
     * @return list of Answer objects where is_solution = TRUE (possibly empty)
     */

    public List<Answer> getSolutionsForQuestion(int questionId) {
        List<Answer> solutions = new ArrayList<>();
        // Use boolean literal TRUE for H2 (don't compare BOOLEAN to INTEGER)
        String sql = "SELECT * FROM answers WHERE question_id = ? AND is_solution = TRUE ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer(
                        rs.getInt("answer_id"),
                        rs.getInt("user_id"),
                        rs.getInt("question_id"),
                        rs.getString("author"),
                        rs.getString("content"),
                        rs.getString("timestamp"),
                        rs.getBoolean("is_solution")
                    );
                    solutions.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return solutions;
    }

    
    /**
     * Create announcements table if it does not exist.
     *
     * Stores administrative announcements made by staff. Announcements can
     * have priority, start/end dates, and display types to control visibility.
     *
     * @throws SQLException if table creation fails
     */
    private void createAnnouncementsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS announcements (" +
                     "announcement_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "title VARCHAR(500) NOT NULL, " +
                     "content TEXT NOT NULL, " +
                     "priority VARCHAR(20) NOT NULL, " +
                     "start_date TIMESTAMP NOT NULL, " +
                     "end_date TIMESTAMP, " +
                     "display_type VARCHAR(20) NOT NULL, " +
                     "created_by_staff_id INT NOT NULL, " +
                     "created_date TIMESTAMP NOT NULL, " +
                     "last_modified_date TIMESTAMP, " +
                     "FOREIGN KEY (created_by_staff_id) REFERENCES cse360users(id)" +
                     ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }


    private void createAnnouncementReadsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS announcement_reads (" +
                     "read_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "announcement_id INT NOT NULL, " +
                     "user_id INT NOT NULL, " +
                     "read_date TIMESTAMP NOT NULL, " +
                     "FOREIGN KEY (announcement_id) REFERENCES announcements(announcement_id), " +
                     "FOREIGN KEY (user_id) REFERENCES cse360users(id), " +
                     "UNIQUE(announcement_id, user_id)" +
                     ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Persist a new Announcement.
     *
     * The provided {@link Announcement} object must contain at least title, content,
     * priority, startDate and displayType. The created_by_staff_id should refer to a staff user.
     *
     * @param announcement Announcement to create
     * @return true on success
     */
    public boolean createAnnouncement(Announcement announcement) {
        String sql = "INSERT INTO announcements (title, content, priority, start_date, end_date, " +
                     "display_type, created_by_staff_id, created_date, last_modified_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setString(3, announcement.getPriority().name());
            pstmt.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.parse(announcement.getStartDate())));
            
            if (announcement.getEndDate() != null && !announcement.getEndDate().isEmpty()) {
                pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.parse(announcement.getEndDate())));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }
            
            pstmt.setString(6, announcement.getDisplayType().name());
            pstmt.setInt(7, announcement.getCreatedByStaffId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating announcement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load all announcements and the staff user name who created each one.
     *
     * @return list of Announcement objects (empty list if none)
     */
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT a.*, u.userName as staff_name " +
                     "FROM announcements a " +
                     "LEFT JOIN cse360users u ON a.created_by_staff_id = u.id " +
                     "ORDER BY a.created_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Announcement announcement = new Announcement();
                announcement.setAnnouncementId(rs.getInt("announcement_id"));
                announcement.setTitle(rs.getString("title"));
                announcement.setContent(rs.getString("content"));
                announcement.setPriority(Announcement.Priority.valueOf(rs.getString("priority")));
                
                Timestamp startTs = rs.getTimestamp("start_date");
                announcement.setStartDate(startTs != null ? startTs.toLocalDateTime().toString() : null);
                
                Timestamp endTs = rs.getTimestamp("end_date");
                announcement.setEndDate(endTs != null ? endTs.toLocalDateTime().toString() : null);
                
                announcement.setDisplayType(Announcement.DisplayType.valueOf(rs.getString("display_type")));
                announcement.setCreatedByStaffId(rs.getInt("created_by_staff_id"));
                announcement.setCreatedByStaffName(rs.getString("staff_name"));
                
                Timestamp createdTs = rs.getTimestamp("created_date");
                announcement.setCreatedDate(createdTs != null ? createdTs.toLocalDateTime().toString() : null);
                
                Timestamp modifiedTs = rs.getTimestamp("last_modified_date");
                announcement.setLastModifiedDate(modifiedTs != null ? modifiedTs.toLocalDateTime().toString() : null);
                
                announcements.add(announcement);
            }
        } catch (SQLException e) {
            System.err.println("Error loading announcements: " + e.getMessage());
            e.printStackTrace();
        }
        return announcements;
    }

    /**
     * Get active announcements for a user (H2 syntax)
     */
    public List<Announcement> getActiveAnnouncementsForUser(int userId) {
        List<Announcement> announcements = new ArrayList<>();
        Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
        
        String sql = "SELECT DISTINCT a.*, u.userName as staff_name " +
                     "FROM announcements a " +
                     "LEFT JOIN cse360users u ON a.created_by_staff_id = u.id " +
                     "LEFT JOIN announcement_reads ar ON a.announcement_id = ar.announcement_id AND ar.user_id = ? " +
                     "WHERE a.start_date <= ? " +
                     "AND (a.end_date IS NULL OR a.end_date >= ?) " +
                     "AND (a.display_type = 'SHOW_ALWAYS' OR ar.read_id IS NULL) " +
                     "ORDER BY CASE a.priority " +
                     "  WHEN 'URGENT' THEN 1 " +
                     "  WHEN 'IMPORTANT' THEN 2 " +
                     "  WHEN 'NORMAL' THEN 3 " +
                     "END, a.created_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setTimestamp(2, now);
            pstmt.setTimestamp(3, now);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Announcement announcement = new Announcement();
                announcement.setAnnouncementId(rs.getInt("announcement_id"));
                announcement.setTitle(rs.getString("title"));
                announcement.setContent(rs.getString("content"));
                announcement.setPriority(Announcement.Priority.valueOf(rs.getString("priority")));
                
                Timestamp startTs = rs.getTimestamp("start_date");
                announcement.setStartDate(startTs != null ? startTs.toLocalDateTime().toString() : null);
                
                Timestamp endTs = rs.getTimestamp("end_date");
                announcement.setEndDate(endTs != null ? endTs.toLocalDateTime().toString() : null);
                
                announcement.setDisplayType(Announcement.DisplayType.valueOf(rs.getString("display_type")));
                announcement.setCreatedByStaffId(rs.getInt("created_by_staff_id"));
                announcement.setCreatedByStaffName(rs.getString("staff_name"));
                
                Timestamp createdTs = rs.getTimestamp("created_date");
                announcement.setCreatedDate(createdTs != null ? createdTs.toLocalDateTime().toString() : null);
                
                Timestamp modifiedTs = rs.getTimestamp("last_modified_date");
                announcement.setLastModifiedDate(modifiedTs != null ? modifiedTs.toLocalDateTime().toString() : null);
                
                announcements.add(announcement);
            }
        } catch (SQLException e) {
            System.err.println("Error loading active announcements: " + e.getMessage());
            e.printStackTrace();
        }
        return announcements;
    }

    /**
     * Update an announcement (H2 syntax)
     */
    public boolean updateAnnouncement(Announcement announcement) {
        String sql = "UPDATE announcements SET title = ?, content = ?, priority = ?, " +
                     "start_date = ?, end_date = ?, display_type = ?, last_modified_date = CURRENT_TIMESTAMP() " +
                     "WHERE announcement_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setString(3, announcement.getPriority().name());
            pstmt.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.parse(announcement.getStartDate())));
            
            if (announcement.getEndDate() != null && !announcement.getEndDate().isEmpty()) {
                pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.parse(announcement.getEndDate())));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }
            
            pstmt.setString(6, announcement.getDisplayType().name());
            pstmt.setInt(7, announcement.getAnnouncementId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating announcement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete an announcement (H2 syntax)
     */
    public boolean deleteAnnouncement(int announcementId) {
        // First delete all read records
        String deleteReads = "DELETE FROM announcement_reads WHERE announcement_id = ?";
        String deleteAnnouncement = "DELETE FROM announcements WHERE announcement_id = ?";
        
        try (PreparedStatement pstmt1 = connection.prepareStatement(deleteReads);
             PreparedStatement pstmt2 = connection.prepareStatement(deleteAnnouncement)) {
            
            pstmt1.setInt(1, announcementId);
            pstmt1.executeUpdate();
            
            pstmt2.setInt(1, announcementId);
            int rowsAffected = pstmt2.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting announcement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create or update a user->announcement read mark.
     *
     * Uses a MERGE/UPSERT pattern so calling this repeatedly is idempotent.
     *
     * @param announcementId the announcement id to mark as read
     * @param userId the user id who read it
     * @return true on success, false otherwise
     */
    public boolean markAnnouncementAsRead(int announcementId, int userId) {
        String sql = "MERGE INTO announcement_reads (announcement_id, user_id, read_date) " +
                     "KEY(announcement_id, user_id) VALUES (?, ?, CURRENT_TIMESTAMP())";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, announcementId);
            pstmt.setInt(2, userId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error marking announcement as read: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns whether a user has previously read the specified announcement.
     *
     * @param announcementId announcement id
     * @param userId user id
     * @return true if mark exists, false otherwise
     */
    public boolean hasUserReadAnnouncement(int announcementId, int userId) {
        String sql = "SELECT COUNT(*) FROM announcement_reads WHERE announcement_id = ? AND user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, announcementId);
            pstmt.setInt(2, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking announcement read status: " + e.getMessage());
        }
        return false;
    }
    
}
