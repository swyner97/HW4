
package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.*;

import model.*;
import pages.*;
import logic.*;
import databasePart1.*;

/**
 * JUnit tests for validating the private messaging function between users.
 * These tests verify core messaging actions: sending, replying to, and retrieving messages,
 * based on Team Project Phase 3 (TP3) requirements.
 * 
 * @author Tirzha Rhys
 * @version Team Project - Phase 3
 * @see model.Messages
 * @see databasePar1.DatabaseHelper
 */
public class PrivateMessageTests {
	
	private static DatabaseHelper dbHelper;
	private static final int senderId = 1;
	private static final int receiverId = 2;
	private static final int userId = 3;
	//private static final int reviewerId = 4;
	//private static final int instructorId = 5;
	//private static final int studentId = 6;
	//private static final int adminId = 7;
	
	/**
	 * This setup initializes the {@link DatabaseHelper} instance before any tests run.
	 * It ensures that a valid database connection exists for message-related testing.
	 * 
	 * @throws Exception if the database connection fails to initialize.
	 * @see DatabaseHelper#connectToDatabase()
	 */
	
	@BeforeAll
	public static void setup() throws Exception {
		dbHelper = new DatabaseHelper();
		dbHelper.connectToDatabase();
	}
	
	/**
	 * This test sends a message between users and verifies that it is stored correctly in the database.
	 * <p>
	 * It confirms that the message can be retrieved from the recipient's inbox using {@link DatabaseHelper#getMessagesForUser(int)}.
	 * </p>
	 * 
	 * @see DatabaseHelper#sendMessage(Messages)
	 * @see DatabaseHelper#getMessagesForUser(int)
	 */
	
	 @Test
	 @DisplayName("Send Message Storage")
	 public void testSendMessageStoresCorrectly() {
	 	Messages msg = new Messages(senderId, receiverId, "JUnit message storage");
	 	dbHelper.sendMessage(msg);
	 	
	 	List<Messages> received = dbHelper.getMessagesBetweenUsers(senderId, receiverId);
	 	boolean found = received.stream().anyMatch(m -> m.getMessage().equals("JUnit message storage"));
	 	received.forEach(m -> System.out.println("Message found: '" + m.getMessage() + "'"));
	 	assertTrue(found, "Message should appear in the recipient's inbox.");
	 }
	 
	 /**
	  * The message retrieval test verifies that messages sent to a user are only retrievable by that user.
	  * It also verifies that senders have access to their sent messages in their sent boxes, and that no
	  * other users have access to the messages via their inbox or sent box.
	  * <p>
	  * This ensures message privacy and access control are properly enforced.
	  * </p>
	  * 
	  * @see DatabaseHelper#getMessagesForUser(int)
	  */
	 @Test
	 @DisplayName("Inbox Retrieval - Correct Recipient Only")
	 public void testMessageRetrievalByRecipientId() {
		 Messages msg = new Messages(senderId, receiverId, "Confidential message to receiver");
		 dbHelper.sendMessage(msg);
		 
		 List<Messages> receiverInbox = dbHelper.getMessagesForUser(receiverId);
		 List<Messages> senderInbox = dbHelper.getMessagesForUser(senderId);
		 List<Messages> senderSentBox = dbHelper.getSentMessagesForUser(senderId);
		 List<Messages> userInbox = dbHelper.getMessagesForUser(userId);
		 List<Messages> userSentBox = dbHelper.getSentMessagesForUser(userId);
		 
		 assertTrue(receiverInbox.stream().anyMatch(m -> m.getMessage().equals("Confidential message to receiver")),
				 "Receiver should receive message.");
		 assertFalse(senderInbox.stream().anyMatch(m -> m.getMessage().equals("Confidential message to receiver")),
				 "Sender should not see message in their inbox.");
		 assertTrue(senderSentBox.stream().anyMatch(m -> m.getMessage().equals("Confidential message to receiver")),
				 "Sender should see message in sent box.");
		 assertFalse(userInbox.stream().anyMatch(m -> m.getMessage().equals("Confidential message to receiver")),
				 "Other user should not see message in their inbox.");
		 assertFalse(userSentBox.stream().anyMatch(m -> m.getMessage().equals("Confidential message to receiver")),
				 "Other user should not see message in their sent box."); 
	 }
	 
	 /**
	  * This test specifically tests that a sender can view their own sent messages using {@link DatabaseHelper#getSentMessagesForUser(int)}.
	  * 
	  * @see Databasehelper#getSentMessagesForUser(int)
	  */
	 @Test
	 @DisplayName("Sent Messages - Retrieve by Sender") 
	 public void testMessageRetrievalBySenderId() {
		 Messages msg = new Messages(senderId, receiverId, "Message from sender sent to receiver");
		 dbHelper.sendMessage(msg);
		 
		 List<Messages> sent = dbHelper.getSentMessagesForUser(senderId);
		 assertTrue(sent.stream().anyMatch(m -> m.getMessage().equals("Message from sender sent to receiver")),
				 "Sender should see their sent message.");
	 }
	 
	 /**
	  * This test verifies that a private message can be successfully sent to the author of a specific {@link Question}.
	  * <p>
	  * This test verifies that the author of the question received the message in their inbox using
	  * {@link databasePart1.DatabaseHelper#getMessagesForUser(int)}. It also verifies that the sender can view it in
	  * their sent messages using {@link databasePart1.DatabaseHelper#getSentMessagesForUser(int)}.
	  * </p>
	  * 
	  * @throws SQLException if a database access error occurs while sending or retrieving the message.
	  * @see model.Messages
	  * @see application.Question
	  * @see databasePart1.DatabaseHelper#sendMessage(Messages)
	  */
	@Test
	@DisplayName("Send private message to question author")
	public void testMessageToAuthor () throws SQLException {
		//simulate an existing question with known author
		Question question = new Question(1001, receiverId, "AuthorUser", "TestTitle", "Test description");
		
		//Send message from sender to question author
		Messages msg = new Messages(senderId, question.getUserId(), "Hello, I have a question about your post.");
		dbHelper.sendMessage(msg);
		
		//Verify message shows in author's inbox
		List<Messages> received = dbHelper.getMessagesForUser(question.getUserId());
		boolean found = received.stream().anyMatch(m -> m.getMessage().equals(msg.getMessage()));
		
		assertTrue(found, "Question author should receive private message.");
		
		//Message is in sent box of sender
		List<Messages> sent = dbHelper.getSentMessagesForUser(senderId);
		boolean foundSent = sent.stream().anyMatch(m -> m.getMessage().equals(msg.getMessage()));
		
		assertTrue(found, "Message sender should see sent message in sent box.");		
	}
	
	/**
	 * This test specifically tests that a private message can be sent to the author of an {@link Answer}.
	 * <p>
	 * This test ensures that the message is delivered to the answer author's inbox via {@link 
	 * databasePar1.DatabaseHelper#getMessagesForUser(int)} and is recorded in the sender's sent messages using
	 * {@link databasePart1.DatabaseHelper#getSentMessagesForUser(int)}.
	 * </p>
	 * 
	 * @throws SQLException if a database error occurs during message sending or retrieving
	 * @see model.Messages
	 * @see application.Answer
	 * @see databasePart1.DatabaseHelper#sendMessage(Messages)
	 * 
	 */
	@Test
	@DisplayName("Send private message to answer author")
	public void testMessageToAnswerAuthor() throws SQLException {
	    // Simulate answer with known author
	    Answer answer = new Answer(2001, receiverId, 1001, "AuthorUser", "This is the answer body.");

	    // Send message from sender to answer author
	    Messages msg = new Messages(senderId, answer.getUserId(), "Thanks for your answer!");
	    dbHelper.sendMessage(msg);

	    // Verify message shows up in the answer author's inbox
	    List<Messages> inbox = dbHelper.getMessagesForUser(answer.getUserId());
	    boolean found = inbox.stream().anyMatch(m -> m.getMessage().equals(msg.getMessage()));

	    assertTrue(found, "Answer author should receive the private message.");
	    
	    //Message is in sent box of sender
  		List<Messages> sent = dbHelper.getSentMessagesForUser(senderId);
  		boolean foundSent = sent.stream().anyMatch(m -> m.getMessage().equals(msg.getMessage()));
  		
  		assertTrue(found, "Message sender should see sent message in sent box.");		
	}
	
	/**
	 * 
	 */
	@Test
	@DisplayName("Message Read/Unread Status Handling")
	public void testMessageStatusUpdates() throws SQLException {
		//send a message
		Messages msg = new Messages(senderId, receiverId, "Mark me as read!");
		dbHelper.sendMessage(msg);
		
		//verify message appears as unread
		List<Messages> inbox = dbHelper.getMessagesForUser(receiverId);
		Optional<Messages> receivedMsg = inbox.stream().filter(m -> m.getMessage().equals("Mark me as read!")).findFirst();
		
		assertTrue(receivedMsg.isPresent(), "Message should appear in recipient's inbox.");
		assertFalse(receivedMsg.get().isRead(), "Message should be marked initially as unread.");
		
		//Mark msg as read
		dbHelper.markMessagesAsRead(receivedMsg.get().getId());
		
		//Retrieve again and verify read status
		List<Messages> updatedInbox = dbHelper.getMessagesForUser(receiverId);
		Optional<Messages> updatedMsg = updatedInbox.stream().filter(m -> m.getId() == receivedMsg.get().getId()).findFirst();
		
		assertTrue(updatedMsg.isPresent(), "Messageshould still exist in inbox.");
		assertTrue(updatedMsg.get().isRead(), "Message should now be marked as read.");
		
	}
}
