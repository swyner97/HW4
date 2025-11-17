package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.*;

import databasePart1.*;
import model.*;
import pages.*;
import logic.*;

/**
 * JUnit tests for verifying database operations related to questions, answers, and clarifications.
 * <p>
 * Tests cover insertion and search functionality implemented in the {@link DatabaseHelper} class,
 * and ensures correct implementation and retrieval of data.
 * 
 * @author Tirzha Rhys
 * @since 1.0
 * @see Question
 * @see Answer
 * @see Clarification
 */

public class DBHelperTest {
	private static DatabaseHelper dbHelper;
	private static List<Question> testQuestions = new ArrayList<>();
	
	//make sure some questions are in database at start for testing
/**
 * Initializes the test database before any tests are run.
 * <p>
 * This method connects to the database using {@link DatabaseHelper}, clears all existing data from 
 * the {@code questions}, {@code answers}, and {@code clarifications} tables to ensure a clean testing state.
 * This method inserts predefined set of six sample {@link Question} objects (including one marked as resolved).
 * </p>
 * 
 * <p>
 * This method also verifies that all six questions were inserted successfully and adds a shutdown hook to close
 * the database connection after the tests finish.
 * </p>
 * 
 * @throws SQLException
 * @see DatabaseHelper
 * @see Question
 * @see Answer
 * @see Clarification
 * @see DatabaseHelper#insertQuestion(Question)
 */
	@BeforeAll
	public static void setup() throws SQLException {
		dbHelper = new DatabaseHelper();
		dbHelper.connectToDatabase();
		
		 try (Statement stmt = dbHelper.getConnection().createStatement()) {
			 stmt.executeUpdate("DELETE FROM clarifications");
			 stmt.executeUpdate("DELETE FROM answers");
			 stmt.executeUpdate("DELETE FROM questions");
			 System.out.println("Cleared all existing questions and answers from table before test.");
		 }
		 
		//create different test questions
		testQuestions.clear();
		
		Question q1 = new Question(-1, -1, "testUser1", "JUnit", "I'm new to JUnit. How do I use it?");
		Question q2 = new Question(-1, -1, "testUser2", "Eclipse", "Where do I go in eclipse to see my github repo?");
		Question q3 = new Question(-1, -1, "studentA", "SQL", "Is there somewhere I can get a quick tutorial on using the SQL stuff?");
		Question q4 = new Question(-1, -1, "testUser2", "HW2", "Do we use JUnit for homework 2?");
		Question q5 = new Question(-1, -1, "studentB", "Office Hours", "When are office hours?");
		Question q6 = new Question(-1, -1, "testUser1", "cats", "Does anyone have funny pictures of their cats?");
		
		q5.markResolved();
		
		dbHelper.insertQuestion(q1);
		dbHelper.insertQuestion(q2);
		dbHelper.insertQuestion(q3);
		dbHelper.insertQuestion(q4);
		dbHelper.insertQuestion(q5);
		dbHelper.insertQuestion(q6);
		
		testQuestions.add(q1);
		testQuestions.add(q2);
		testQuestions.add(q3);
		testQuestions.add(q4);
		testQuestions.add(q5);
		testQuestions.add(q6);
		
		List<Question> questions = dbHelper.loadAllQs();
		assertFalse(questions.isEmpty());
		assertEquals(6, questions.size(), "Six questions inserted");
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			dbHelper.closeConnection();
		}));
	}
	
/**
 * Inserts an answer into a known question and verifies that it can be retrieved correctly from the database.
 * <p>
 * The test confirms that the inserted answer matches by author and content using {@link DatabaseHelper#insertAnswer(Answer)}
 * and {@link DatabaseHelper#loadAnswersForQs(int)}.
 * 
 * @throws SQLException if database error occurs.
 * @see DatabaseHelper#loadAllQs()
 * @see Answer
 * @see Question
 */
	//@Order
	@Test
	public void testInsertAnswerOnPreLoadedQuestion() throws SQLException {
		int questionId = dbHelper.loadAllQs().stream()
			    .filter(q -> q.getAuthor().equals("testUser1") && q.getTitle().equals("JUnit"))
			    .findFirst()
			    .map(Question::getQuestionId)
			    .orElseThrow(() -> new AssertionError("JUnit question not found"));
		
		try (PreparedStatement stmt = dbHelper.getConnection().prepareStatement(
		        "SELECT question_id FROM questions WHERE title = ? AND author = ?")) {
		    stmt.setString(1, "JUnit");
		    stmt.setString(2, "testUser1");
		    try (ResultSet rs = stmt.executeQuery()) {
		        if (rs.next()) {
		            questionId = rs.getInt("question_id");
		        }
		    }
		}
		assertTrue(questionId > 0, "Failed to retrieve JUnit question ID from database");
		
		Answer answer = new Answer(1, 1, questionId, "studentA", "Use @Test annotation.", LocalDateTime.now().toString().substring(0,19), false);
		dbHelper.insertAnswer(answer);
		
		List<Answer> answers = dbHelper.loadAnswersForQs(questionId);
		
		//Check at least on answer matches the inserted answer
		boolean found = answers.stream().anyMatch(a ->
			a.getAuthor().equals("studentA") &&
			a.getContent().equals("Use @Test annotation.")
		);
		
		assertTrue(found, "Inserted answer not found in answers list.");
	}
	
/**
 * Inserts a clarification into a specific question and verifies it is properly stored in the database.
 * <p>
 * Uses {@link DatabaseHelper#insertClarification(Clarification)} and 
 * {@link DatabaseHelper#loadClarificationsforQ(int)} to confirm that the clarification is successfully stored and can be retrieved.
 * 
 * @throws SQLException if database error occurs.
 * @see Clarification
 * @see DatabaseHelper#searchQuestions(String, String, String)
 * @see Question
 */
	//@Order
	@Test
	public void testInsertClarification() throws SQLException {
		List<Question> results = dbHelper.searchQuestions("Eclipse", null, "testUser2");
		assertFalse(results.isEmpty());
		int questionId = results.get(0).getQuestionId();
		
		//Clarification clarification = new Clarification(0, questionId, 0, recipientId, "studentA", "Do you mean to import it or just to look at it?", LocalDateTime.now().toString().substring(0,19));
		//dbHelper.insertClarification(clarification);
		
		List<Clarification> clarifications = dbHelper.loadClarificationsforQ(questionId);
		
		boolean found = clarifications.stream().anyMatch(c ->
			c.getAuthor().equals("studentA") &&
			c.getContent().equals("Do you mean to import it or just to look at it?")
		);
		
		assertTrue(found, "Clarification not found in clarifications");
	}
	
/**
 * These next three tests verify the search functionality.
 * <p>
 * Each test applies a different filter (keyword, author, or status) to confirm that
 * {@link DatabaseHelper#searchQuestions(String, String, String)} correctly returns the expected questions.
 */
	
	
/**
 * Searches for questions in the database using a keyword and verifies the number of expected results.
 * <p>
 * Ensures the {@code searchQuestions} method returns questions containing the term 'JUnit'.
 * 
 * @throws SQLException if database error occurs.
 * @see Question
 * @see DatabaseHelper#searchQuestions(String, String, String)
 */
	//@Order
	@Test
	public void testSearchByKeyword() throws SQLException {
		List<Question> results = dbHelper.searchQuestions("JUnit", null, null);
		assertFalse(results.isEmpty(), "Expected 2 search results, but got none.");
		assertEquals(2, results.size(), "Expected 2 questions matching keyword 'JUnit'");
	}
	
/**
 * Searches for questions authored by a specific user and verifies all returned results match the author.
 * <p>
 * This test confirms the accuracy of the {@code searchQuestions} filter by author.
 * 
 * @throws SQLException if database error occurs.
 * @see Question
 * @see DatabaseHelper#searchQuestions(String, String, String)
 */
	//@Order
	@Test
	public void testSearchByAuthor() throws SQLException {
		List<Question> results = dbHelper.searchQuestions(null, null, "testUser2");
		assertFalse(results.isEmpty());
		assertEquals(2, results.size(), "Expected 2 questions by 'testUser2'");
	}
	
/**
 * Searches for questions based on their status of resolved/unresolved and verifies correct classification.
 * <p>
 * Confirms that resolved and unresolved questions are correctly filtered and counted.
 * 
 * @throws SQLException if database error occurs.
 * @see Question
 * @see DatabaseHelper#searchQuestions(String, String, String)
 */
	//@Order
	@Test
	public void testSearchByStatus() throws SQLException {
		List<Question> resolvedQs = dbHelper.searchQuestions(null, "Resolved", null);
		List<Question> unresolved = dbHelper.searchQuestions(null, "Unresolved", null);
		assertFalse(resolvedQs.isEmpty());
		assertEquals(1, resolvedQs.size(), "Expected one question set to resolved");
		assertFalse(unresolved.isEmpty());
		assertEquals(5, unresolved.size(), "Expected 5 unresolved questions");	

	}
	
	//@Order
	/*@Test
	public void testGetFollowUp() throws SQLException {
		Question base = new Question(-1, -1, "testUser3", "Original question", "What is a follow-up?");
		dbHelper.insertQuestion(base);
		int questionId = base.getQuestionId();
		
		Clarification clarify = new Clarification(0, questionId, 0, "studentC", "Do you mean in terms of the project or what?", LocalDateTime.now().toString());
		
		Question followUp1 = new Question(-1, -1, "testUser3", "follow-up q", "What is the project you're referring to?");
		followUp1.setFollowUp(questionId);
		dbHelper.insertQuestion(followUp1);
		
		Clarification clarify2 = new Clarification(0, questionId, 0, "testUser3", "follow up 2", "nevermind. I am in the wrong class.");
	}*/
	

}






/*package application.JUnitTests;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.*;

import application.*;
import databasePart1.*;
import model.*;
import pages.*;
import logic.*;

public class DBHelperTest {
	private static DatabaseHelper dbHelper;
	private static List<Question> testQuestions = new ArrayList<>();
	
	//make sure some questions are in database at start for testing
	@BeforeAll
	public static void setup() throws SQLException {
		dbHelper = new DatabaseHelper();
		dbHelper.connectToDatabase();
		
		 try (Statement stmt = dbHelper.getConnection().createStatement()) {
			 stmt.executeUpdate("DELETE FROM clarifications");
			 stmt.executeUpdate("DELETE FROM answers");
			 stmt.executeUpdate("DELETE FROM questions");
			 System.out.println("Cleared all existing questions and answers from table before test.");
		 }
		 
		//create different test questions
		testQuestions.clear();
		
		Question q1 = new Question(10, 10, "testUser1", "JUnit", "I'm new to JUnit. How do I use it?");
		Question q2 = new Question(11, 11, "testUser2", "Eclipse", "Where do I go in eclipse to see my github repo?");
		Question q3 = new Question(12, 12, "studentA", "SQL", "Is there somewhere I can get a quick tutorial on using the SQL stuff?");
		Question q4 = new Question(13, 11, "testUser2", "HW2", "Do we use JUnit for homework 2?");
		Question q5 = new Question(14, 13, "studentB", "Office Hours", "When are office hours?");
		Question q6 = new Question(15, 10, "testUser1", "cats", "Does anyone have funny pictures of their cats?");
		
		q5.markResolved();
		
		dbHelper.insertQuestion(q1);
		dbHelper.insertQuestion(q2);
		dbHelper.insertQuestion(q3);
		dbHelper.insertQuestion(q4);
		dbHelper.insertQuestion(q5);
		dbHelper.insertQuestion(q6);
		
		testQuestions.add(q1);
		testQuestions.add(q2);
		testQuestions.add(q3);
		testQuestions.add(q4);
		testQuestions.add(q5);
		testQuestions.add(q6);
		
		List<Question> questions = dbHelper.loadAllQs();
		assertFalse(questions.isEmpty());
		assertEquals(6, questions.size(), "Six questions inserted");
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			dbHelper.closeConnection();
		}));
	}
	
	//@Order
	@Test
	public void testInsertAnswerOnPreLoadedQuestion() throws SQLException {
		int questionId = dbHelper.loadAllQs().stream()
			    .filter(q -> q.getAuthor().equals("testUser1") && q.getTitle().equals("JUnit"))
			    .findFirst()
			    .map(Question::getQuestionId)
			    .orElseThrow(() -> new AssertionError("JUnit question not found"));
		
		try (PreparedStatement stmt = dbHelper.getConnection().prepareStatement(
		        "SELECT question_id FROM questions WHERE title = ? AND author = ?")) {
		    stmt.setString(1, "JUnit");
		    stmt.setString(2, "testUser1");
		    try (ResultSet rs = stmt.executeQuery()) {
		        if (rs.next()) {
		            questionId = rs.getInt("question_id");
		        }
		    }
		}
		assertTrue(questionId > 0, "Failed to retrieve JUnit question ID from database");
		
		Answer answer = new Answer(1, 1, questionId, "studentA", "Use @Test annotation.", LocalDateTime.now().toString().substring(0,19), false);
		dbHelper.insertAnswer(answer);
		
		List<Answer> answers = dbHelper.loadAnswersForQs(questionId);
		
		//Check at least on answer matches the inserted answer
		boolean found = answers.stream().anyMatch(a ->
			a.getAuthor().equals("studentA") &&
			a.getContent().equals("Use @Test annotation.")
		);
		
		assertTrue(found, "Inserted answer not found in answers list.");
	}
	
	/*
	//@Order
	@Test
	public void testInsertClarification() throws SQLException {
		List<Question> results = dbHelper.searchQuestions("Eclipse", null, "testUser2");
		assertFalse(results.isEmpty());
		int questionId = results.get(0).getQuestionId();
		
		Clarification clarification = new Clarification(0, questionId, 0, 0, "studentA", "Do you mean to import it or just to look at it?", LocalDateTime.now(), false);
		dbHelper.insertClarification(clarification);
		
		List<Clarification> clarifications = dbHelper.loadClarificationsforQ(questionId);
		
		boolean found = clarifications.stream().anyMatch(c ->
			c.getAuthor().equals("studentA") &&
			c.getContent().equals("Do you mean to import it or just to look at it?")
		);
		
		assertTrue(found, "Clarification not found in clarifications");
	}
	
	//@Order
	@Test
	public void testSearchByKeyword() throws SQLException {
		List<Question> results = dbHelper.searchQuestions("JUnit", null, null);
		assertFalse(results.isEmpty(), "Expected 2 search results, but got none.");
		assertEquals(2, results.size(), "Expected 2 questions matching keyword 'JUnit'");
	}
	
	//@Order
	@Test
	public void testSearchByAuthor() throws SQLException {
		List<Question> results = dbHelper.searchQuestions(null, null, "testUser2");
		assertFalse(results.isEmpty());
		assertEquals(2, results.size(), "Expected 2 questions by 'testUser2'");
	}
	
	//@Order
	@Test
	public void testSearchByStatus() throws SQLException {
		List<Question> resolvedQs = dbHelper.searchQuestions(null, "Resolved", null);
		List<Question> unresolved = dbHelper.searchQuestions(null, "Unresolved", null);
		assertFalse(resolvedQs.isEmpty());
		assertEquals(1, resolvedQs.size(), "Expected one question set to resolved");
		assertFalse(unresolved.isEmpty());
		assertEquals(5, unresolved.size(), "Expected 5 unresolved questions");	

	}
	
	
	//@Order
	@Test
	public void testGetFollowUp() throws SQLException {
		Question base = new Question(-1, -1, "testUser3", "Original question", "What is a follow-up?");
		dbHelper.insertQuestion(base);
		int questionId = base.getQuestionId();
		
		Clarification clarify = new Clarification(0, questionId, 0, 0, "studentC", "Do you mean in terms of the project or what?", LocalDateTime.now(), false);
		
		Question followUp1 = new Question(-1, -1, "testUser3", "follow-up q", "What is the project you're referring to?");
		followUp1.setFollowUp(questionId);
		dbHelper.insertQuestion(followUp1);
		
		//Clarification clarify2 = new Clarification(0, questionId, 0, 0, "testUser3", "follow up 2", "nevermind. I am in the wrong class.", LocalDateTime.now(), false);
	}
	

}*/
