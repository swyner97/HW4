package application;
import org.junit.jupiter.api.*;

import javafx.stage.Stage;
import pages.*;
import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;

import static org.junit.jupiter.api.Assertions.*;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InitialAccessPageTest {
    private static boolean jfxInitialized = false;
    private InitialAccessPage initialAccessPage;
    private DatabaseHelper mockDatabaseHelper;
    private Stage mockStage;

    @BeforeAll  // Changed from @BeforeClass
    public static void initJFX() {
        if (!jfxInitialized) {
            System.out.println("Initializing JavaFX...");
            new JFXPanel();
            jfxInitialized = true;
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("JavaFX initialized");
        }
    }

    @BeforeEach  // Changed from @Before
    public void setUp() throws Exception {
    	
        final CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            mockDatabaseHelper = new MockDatabaseHelper();
            initialAccessPage = new InitialAccessPage(mockDatabaseHelper);
            mockStage = new Stage();
            latch.countDown();
        });
        
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testShow() throws Exception {
        System.out.println("TEST: show()");
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        
        Platform.runLater(() -> {
            try {
                initialAccessPage.show(mockStage);
                result[0] = mockStage.isShowing();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        
        latch.await(5, TimeUnit.SECONDS);
        assertTrue(result[0], "Stage should be showing");  // Changed order
    }

    @Test
    public void testLoginButton() throws Exception {
        System.out.println("TEST: login button");
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] errorText = {null};
        
        Platform.runLater(() -> {
            try {
                TextField mockUsername = new TextField("testUser");
                PasswordField mockPassword = new PasswordField();
                mockPassword.setText("testPass123!");
                Label mockError = new Label();

                initialAccessPage.handleLogin(mockUsername, mockPassword, mockError, mockStage);
                errorText[0] = mockError.getText();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        
        latch.await(5, TimeUnit.SECONDS);
        assertNotNull(errorText[0], "Error text should not be null");  // Changed order
    }

    @Test
    public void testSignUpButton() throws Exception {
        System.out.println("TEST: sign up button");
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] disabled = {true};
        
        Platform.runLater(() -> {
            try {
                TextField mockUsername = new TextField("newUser");
                PasswordField mockPassword = new PasswordField();
                mockPassword.setText("testPass123!");
                TextField mockInvite = new TextField("INVITE123");
                Label mockError = new Label();
                Button mockButton = new Button();

                initialAccessPage.handleSignUp(mockUsername, mockPassword, mockInvite,
                    mockError, mockButton, mockStage);

                disabled[0] = mockButton.isDisabled();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        
        latch.await(5, TimeUnit.SECONDS);
        assertFalse(disabled[0], "Button should not be disabled");  // Changed order
    }

    @Test
    public void testPasswordVisibilityToggle() throws Exception {
        System.out.println("TEST: password visibility toggle");
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] results = new String[2];
        
        Platform.runLater(() -> {
            try {
                TextField visibleField = new TextField();
                PasswordField hiddenField = new PasswordField();

                hiddenField.textProperty().bindBidirectional(visibleField.textProperty());

                hiddenField.setText("password123");
                results[0] = visibleField.getText();

                visibleField.setText("newpass456");
                results[1] = hiddenField.getText();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        
        latch.await(5, TimeUnit.SECONDS);
        assertEquals("password123", results[0], "Visible field should match");  // Changed order
        assertEquals("newpass456", results[1], "Hidden field should match");  // Changed order
    }

    private class MockDatabaseHelper extends DatabaseHelper {
        public MockDatabaseHelper() {
            super();
        }

        @Override
        public String getUserRole(String username) {
            return "STUDENT";
        }

        @Override
        public String loginWithOTPcheck(String username, String password, String role) {
            return "normal";
        }
    }
}