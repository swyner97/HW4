package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.PasswordRecognizer;
import logic.StatusData;
import model.NavigationBar;
import model.User;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;

public class ResetPasswordPage {
	private final DatabaseHelper databaseHelper;
	private final User currentUser;
	
	public ResetPasswordPage(DatabaseHelper databaseHelper, User currentUser) {
		this.databaseHelper = databaseHelper;
		this.currentUser = currentUser;
	}
	
	public void show(Stage primaryStage, String userName) {
		//Integrating Navigation Bar
    	BorderPane borderPane = new BorderPane();
    	NavigationBar navigationBar = new NavigationBar();
    	borderPane.setTop(navigationBar);

		PasswordField newPwField = new PasswordField();
		newPwField.setPromptText("Enter new password");
		newPwField.setMaxWidth(250);
		
		PasswordField confirmPwField = new PasswordField();
		confirmPwField.setPromptText("Confirm new password");
		confirmPwField.setMaxWidth(250);
		
		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
		errorLabel.setWrapText(true);
		
		Label requirementsHeader = new Label("Username and Password Requirements:");
		Label passwordRules1 = new Label("- Password must be at least 8 characters.");
        Label passwordRules2 = new Label("- Must contain: uppercase, lowercase, digit, special character.");
	        
		requirementsHeader.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-text-fill: green;");
		passwordRules1.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
        passwordRules1.setWrapText(true);
        passwordRules2.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
        passwordRules2.setWrapText(true);
        
        Button backToLoginButton = new Button("Go to Login");
		backToLoginButton.setOnAction(e -> {
			new InitialAccessPage(databaseHelper).show(primaryStage);
		});
		backToLoginButton.setVisible(false);
		
		Button submitButton = new Button("Reset Password");
		submitButton.setOnAction(e -> {
			String newPw = newPwField.getText();
			String confirmPw = confirmPwField.getText();
			
			if (!newPw.isEmpty()) {
				if (!newPw.equals(confirmPw)) {
					errorLabel.setText("New passwords do not match");
					return;
				}
				
				// Validate new password based on FSM
				String passwordError = PasswordRecognizer.evaluatePassword(newPw);
				if (!passwordError.isEmpty()) {
					System.out.println(passwordError);
					errorLabel.setText(passwordError);
					return;
				}
				
				errorLabel.setStyle("-fx-text-fill: green,");
				errorLabel.setText("Password change successful. Please login.");
				currentUser.setPassword(newPw);		
				
				backToLoginButton.setVisible(true);
				
				try {
					databaseHelper.updateUser(currentUser, userName);
				}
				catch (SQLException ex) {
					ex.printStackTrace();
					errorLabel.setText("An error occured while updating your password.");
				}
				
				// *******DEBUG: Delete when ready********
				String updatedPassword = currentUser.getPassword();
				System.out.println("DEBUG: Password in database for user '" + userName + "' is now: " + updatedPassword);
			}
		});
		
		VBox layout = new VBox(10);
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
		layout.getChildren().addAll(newPwField, confirmPwField, submitButton,
				errorLabel, backToLoginButton,
				requirementsHeader, passwordRules1, passwordRules2
		);
		borderPane.setCenter(layout);
        
        Scene scene = new Scene(borderPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
       
        primaryStage.setScene(scene);
		primaryStage.setTitle("Change Your Password");
		primaryStage.show();
	}

}