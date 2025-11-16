package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * UpdateAccountPage class handles the account information update/change process for established users.
 * Users can change or update their userName, password, email, or name.
 */
public class UpdateAccountPage {
	
	private final DatabaseHelper databaseHelper;
    private final User currentUser;
     
    // DatabaseHelper to handle database operations.
    public UpdateAccountPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    /**
     * Displays the Update Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    
    public void show(Stage primaryStage) {
    	
    	//Integrating Navigation BAr
    	BorderPane borderPane = new BorderPane();
    	NavigationBar navigationBar = new NavigationBar();
    	borderPane.setTop(navigationBar);
    	
    	// Input fields for userName, name, email, password
        TextField userNameField = new TextField(currentUser.getUserName());
        userNameField.setMaxWidth(250);
        
        TextField nameField = new TextField(currentUser.getName());
        if (currentUser.getName() != null && !currentUser.getName().isEmpty()) {
        	nameField.setText(currentUser.getName());
        }
        else {
        	nameField.setPromptText("Enter Name");
        }
        nameField.setMaxWidth(250);
        
        TextField emailField = new TextField(currentUser.getEmail());
        if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
        	emailField.setText(currentUser.getEmail());
        }
        else {
        	emailField.setPromptText("Enter Email");
        }
        emailField.setMaxWidth(250);
        
        PasswordField currentPwField = new PasswordField();
        currentPwField.setPromptText("Current Password");
        currentPwField.setMaxWidth(250);

        PasswordField newPwField = new PasswordField();
        newPwField.setPromptText("Enter New Password");
        newPwField.setMaxWidth(250);
        
        PasswordField confirmPwField = new PasswordField();
        confirmPwField.setPromptText("Confirm New Password");
        confirmPwField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);
        
        // ScrollPane to display the error labels with the room they need
        ScrollPane errorScrollPane = new ScrollPane(errorLabel);
        errorScrollPane.setPrefWidth(300);
        errorScrollPane.setPrefViewportHeight(100);
        errorScrollPane.setStyle("-fx-background-color:transparent; -fx-padding: 5;");
        
        // Labels for username and password requirements
        
        Label requirementsHeader = new Label("Username and Password Requirements:");
        Label usernameRules = new Label("- Username must start with a letter and be 4–16 characters.");
        Label passwordRules1 = new Label("- Password must be at least 8 characters.");
        Label passwordRules2 = new Label("- Must contain: uppercase, lowercase, digit, special character.");

        requirementsHeader.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-text-fill: green;");
        usernameRules.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
        usernameRules.setWrapText(true);
        passwordRules1.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
        passwordRules1.setWrapText(true);
        passwordRules2.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
        passwordRules2.setWrapText(true);
        
     // Keep a copy of the old username/email before it changes
    	String oldUserName = currentUser.getUserName();  
    	String oldEmail = currentUser.getEmail();
        Button saveButton = new Button("Save Changes");
        
        saveButton.setOnAction(a -> {
        	      	
        	// Retrieve user input
            String newUserName = userNameField.getText();
            String name = nameField.getText();
            String newEmail = emailField.getText();
            String currentPw = currentPwField.getText();
            String newPw = newPwField.getText();
            String confirmPw = confirmPwField.getText();
            
            if (!newUserName.equals(oldUserName)) {
            	
            	String userNameError = UserNameRecognizer.checkForValidUserName(newUserName);
            	if (!userNameError.isEmpty()) {
            		System.out.println(userNameError);
            		errorLabel.setText(userNameError);
            		return;
            	}
            	currentUser.setUserName(newUserName);
            }
            
            //Check current password
            if (!currentPw.equals(currentUser.getPassword())) {
            	errorLabel.setText("Current password is incorrect");
            	return;
            }
            
            //Validate new password inputs are equal
            if (!newPw.isEmpty()) {
            	if (!newPw.equals(confirmPw)) {
            		errorLabel.setText("New passwords do not match");
            		return;
            	}
            	//Validate new password based on FSM
                String passwordError = PasswordRecognizer.evaluatePassword(newPw);
                if (!passwordError.isEmpty()) {
                	System.out.println(passwordError);
                	errorLabel.setText(passwordError);
                	return;
                }
                currentUser.setPassword(newPw);
            }
            
            // Validate email
            if (!newEmail.isEmpty()) {
            	if (!newEmail.equals(oldEmail)) {
            		String emailError = EmailRecognizer.validate(newEmail);
            		if (!emailError.isEmpty()) {
            			System.out.println(emailError);
            			errorLabel.setText(emailError);
            			return;
            		}
            		currentUser.setEmail(newEmail);
            	}
            }
            
            
            // Update name
            currentUser.setName(name);
            
            try {
            	// Check if the user already exists
            	if(!newUserName.equals(oldUserName) && databaseHelper.doesUserExist(newUserName)) {
            		errorLabel.setText("This userName is already taken. Please choose another.");
            		return;
            	}
            	
            	// Save Updates
            	// Use oldUserName for WHERE in database so correct row in DB table updates
            	databaseHelper.updateUser(currentUser, oldUserName);
            	errorLabel.setStyle("-fx-text-fill: green");
            	errorLabel.setText("Profile update successfully!");
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                errorLabel.setText("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        VBox reqsBox = new VBox(5, requirementsHeader, usernameRules, passwordRules1, passwordRules2);
        reqsBox.setStyle("-fx-alignment: top-left;");
        
        // Horizontal layout for req and error labels
        HBox bottomBox = new HBox(20, reqsBox, errorScrollPane);
        bottomBox.setStyle("-fx-alignment: center");
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(
        		userNameField, nameField, emailField, 
        		currentPwField, newPwField, confirmPwField,
        		saveButton, bottomBox
        		//requirementsHeader, usernameRules, passwordRules1, passwordRules2
        );
        borderPane.setCenter(layout);
        
        Scene scene = new Scene(borderPane, 800, 400);
        primaryStage.setTitle("Update Account");
        primaryStage.setScene(scene);
    }
}