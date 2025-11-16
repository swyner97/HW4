package application;

import java.sql.SQLException;
import databasePart1.*;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        //  Label for GUI error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        
        //  Labels for username and password requirements
        Label requirementsHeader = new Label("Username and Password Requirements:");
        Label usernameRules = new Label("- Username must start with a letter and be 4–16 characters.");
        Label passwordRules1 = new Label("- Password must be at least 8 characters.");
        Label passwordRules2 = new Label("- Must contain: uppercase, lowercase, digit, special character.");

        requirementsHeader.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-text-fill: green;");
        usernameRules.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
        passwordRules1.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
        passwordRules2.setStyle("-fx-font-size: 12; -fx-text-fill: green;");

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            
            //Check validity of user name based on FSM
            String userNameError = UserNameRecognizer.checkForValidUserName(userName);
            if (!userNameError.isEmpty()) {
            	System.out.println(userNameError);
            	errorLabel.setText(userNameError);
            	return;
            }
            
            String password = passwordField.getText();
            
            //Check validity of password based on FSM
            String passwordError = PasswordRecognizer.evaluatePassword(password);
            if (!passwordError.isEmpty()) {
            	System.out.println(passwordError);
            	errorLabel.setText(passwordError);
            	return;
            }
            
            try {
            	// Create a new User object with admin role and register in the database
            	User user=User.createUser(userName, password, "admin", "", "", null);
            	StatusData.currUser = user;
            	StatusData.currUser.setName(userName);
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");
                //this is to put the user role into addUserRoles in database!
                databaseHelper.addUserRoles(StatusData.currUser.getUserName(), StatusData.currUser.getRole());
                
                // Navigate to the Welcome Login Page
                new WelcomeLoginPage().show(primaryStage,user);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, userNameField, passwordField, errorLabel, setupButton, requirementsHeader, 
        		usernameRules, passwordRules1, passwordRules2);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
