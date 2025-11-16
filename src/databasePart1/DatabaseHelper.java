package databasePart1;

import java.sql.*;
import application.Clarification;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import application.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.SQLException;

import application.Answer;
import application.Question;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
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
            }
            catch (SQLException e) {
            	System.err.println("Table creation failed: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
        
        
    }

    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "role VARCHAR(20),"
                + "name VARCHAR(255), "
                + "email VARCHAR(255),"
                + "temp_password VARCHAR(255))";
        statement.execute(userTable);

        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);
        
     // separate table for user having more than one more
        String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles("
        		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
        		+ "userName VARCHAR(255) NOT NULL, "
        		+ "role VARCHAR(255) NOT NULL)";
        statement.execute(userRolesTable);
        
        // separate table for invitation code
        String codeRolesTable = "CREATE TABLE IF NOT EXISTS CodeRoles("
        		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
        		+ "code VARCHAR(10),"
        		+ "initialRole VARCHAR(255))";
        statement.execute(codeRolesTable);

        // Adding tables for Questions and Answers
        /*String dropQuestions = "DROP TABLE IF EXISTS questions";
        statement.executeUpdate(dropQuestions);*/
        
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
        		+ "question_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "author VARCHAR(50),"
        		+ "title VARCHAR(200),"
        		+ "description VARCHAR(5000),"
        		+ "timestamp VARCHAR(20),"
        		+ "status VARCHAR(20),"
        		+ "follow_up INT NULL,"
        		+ "FOREIGN KEY (follow_up) REFERENCES questions(question_id))";
        
        String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
        		+ "answer_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "user_id INT,"
        		+ "question_id INT,"
        		+ "author VARCHAR(50),"
        		+ "content VARCHAR(2000),"
        		+ "timestamp VARCHAR(20),"
        		+ "is_solution BOOLEAN,"
        		+ "FOREIGN KEY (question_id) REFERENCES questions(question_id))";
        
        statement.execute(questionsTable);
        statement.execute(answersTable);
        
        // adding table to store clarifications 
        String clarificationsTable = "CREATE TABLE IF NOT EXISTS clarifications ("
        		+ "clarification_id INT AUTO_INCREMENT PRIMARY KEY,"
        		+ "question_id INT,"
        		+ "answer_id INT,"
        		+ "author VARCHAR(50),"
        		+ "content VARCHAR(2000),"
        		+ "timestamp VARCHAR(20),"
        		+ "FOREIGN KEY (question_id) REFERENCES questions(question_id),"
        		+ "FOREIGN KEY (answer_id) REFERENCES answers(answer_id))";

        statement.execute(clarificationsTable);  
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
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.executeUpdate();
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
		String updateUser = "UPDATE cse360users SET userName=?, password=?, "
				+ "role=?, name=?, email=? WHERE userName=?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateUser)) {
			pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getRole());
	        pstmt.setString(4, user.getName());
	        pstmt.setString(5, user.getEmail());
	        pstmt.setString(6, oldUserName); // Uses current username as identifier
	        pstmt.executeUpdate();
		}
	}


    public void loadUserDetails(User user) {
        String query = "SELECT name, email FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        String sql = "SELECT username, email, phone, bio, role FROM cse360users WHERE id = ?";
        Map<String, String> profile = new HashMap<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                profile.put("username", rs.getString("username"));
                profile.put("email", rs.getString("email"));
                profile.put("phone", rs.getString("phone"));
                profile.put("bio", rs.getString("bio"));
                profile.put("role", rs.getString("role"));
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
		}
		catch (SQLException e) {
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
    
    public void addUserRoles(String userName, String newRole) throws SQLException {
        // check if user already has this role
        String check = "SELECT COUNT(*) FROM UserRoles WHERE userName = ? AND role = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(check)) {
            checkStmt.setString(1, userName);
            checkStmt.setString(2, newRole);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) { // only insert if it doesn’t exist
                String insertRole = "INSERT INTO UserRoles (userName, role) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertRole)) {
                    pstmt.setString(1, userName);
                    pstmt.setString(2, newRole);
                    pstmt.executeUpdate();
                }
            }
        }
    }
    
    //where invitation codes will assigned
    public void addRoleVIACode(String code, String initialRole) throws SQLException{
        String query = "INSERT INTO CodeRoles (code, initialRole) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.setString(2, initialRole);
            pstmt.executeUpdate();
            System.out.println("Inserting: " + initialRole);
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
    
    
    public void deleteUserRole (String userName, String role) throws SQLException{
    	String query = "DELETE FROM UserRoles WHERE userName =? and role = ?";
    	try (PreparedStatement pstmt = connection.prepareStatement(query)){
    		pstmt.setString(1, userName);
    		pstmt.setString(2, role);
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
                String tempPw = rs.getString("temp_password");

                users.add(User.createUser(id, userName, password, role, name, email, tempPw));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
 //======================Start: Questions and Answers========================================   
    // Store questions and answers
    public void insertQuestion(Question question) throws SQLException {
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
    		else {
    			pstmt.setNull(6, Types.INTEGER);
    		}
    		
    		int rowsInserted = pstmt.executeUpdate();
    		System.out.println("Rows inserted: " + rowsInserted);
    	}
    	catch (SQLException e) {
    		System.err.println("insertQuestion failed: " + e.getMessage());
    		throw e;
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
    			if (rs.wasNull()) { followUp = 0; }
    			
    			Question q = new Question(id, -1, author, title, description);
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
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    		System.err.println("Error loading answers: " + e.getMessage());
    	}
    	return answers;
    }
    	
    //Load answers for questions
    private List<Answer> loadAnswersForQs(int questionId) {
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
    	}
    	catch (SQLException e) {
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
    			if (rs.wasNull()) { followUp = 0; }
    			
    			Question q = new Question(id, -1, auth, title, description);
    			q.setTimestamp(time);
    			q.setStatus(stat);
    			q.setFollowUp(followUp);
    			q.setAnswers(loadAnswersForQs(id));
    			result.add(q);
    		}
    	}
    	catch (SQLException e) {
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
    	}
    	catch (SQLException e) {
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



    
    //onetime use delete
   /* public void deleteOneTime() {
    	String sql = "DELETE FROM questions WHERE author IN ('Meg', 'Charles', 'James', 'AviUser', 'User1')";
    	 try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    	        int rowsDeleted = pstmt.executeUpdate();
    	        System.out.println("Deleted " + rowsDeleted + " hardcoded questions.");
    	    } catch (SQLException e) {
    	        System.err.println("Failed to delete hardcoded questions: " + e.getMessage());
    	    }
    	}*/
  
    
    
//======================End: Questions and Answers======================================== 
    
//======================Begin: Clarification functions====================================
    //Insert Clarifictaion
    public void insertClarification(Clarification clarification) throws SQLException {
    	String sql = "INSERT INTO clarifications (question_id, answer_id, author, content, timestamp) VALUES (?, ?, ?, ?, ?)";
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setObject(1, clarification.getQuestionId() > 0 ? clarification.getQuestionId() : null);
    		pstmt.setObject(2, clarification.getAnswerId() > 0 ? clarification.getAnswerId() : null);
    		pstmt.setString(3, clarification.getAuthor());
    		pstmt.setString(4, clarification.getContent());
    		pstmt.setString(5, clarification.getTimestamp());
    		pstmt.executeUpdate();
    	}
    }
    
    // Load clarifications logic
    private List<Clarification> loadClarifications(String type, int id) throws SQLException {
    	List<Clarification> clarifications = new ArrayList<>();
    	String sql;
    	 if ("question_id".equals(type)) {
	        sql = "SELECT * FROM clarifications WHERE question_id = ?";
	    } else if ("answer_id".equals(type)) {
	        sql = "SELECT * FROM clarifications WHERE answer_id = ?";
	    } else {
	        throw new IllegalArgumentException("Invalid clarification type: " + type);
	    }
    	 
    	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
    		pstmt.setInt(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			clarifications.add(new Clarification (
    					rs.getInt("clarification_id"),
    					rs.getInt("question_id"),
    					rs.getInt("answer_id"),
    					rs.getString("author"),
    					rs.getString("content"),
    					rs.getString("timestamp")
    			));
    		}
    	}
    	return clarifications;
    }
    
    //Load clarifications for question
    public List<Clarification> loadClarificationsforQ(int questionId) throws SQLException {
    	return loadClarifications("question_id", questionId);
    }
    
    //Load clarifications for answer
    public List<Clarification> loadClarificationsforA(int answerId) throws SQLException {
    	return loadClarifications("answer_id", answerId);
    }

    public void closeConnection() {
        try { if (statement != null) statement.close(); } catch(SQLException se) { se.printStackTrace(); }
        try { if (connection != null) connection.close(); } catch(SQLException se) { se.printStackTrace(); }
    }
}
