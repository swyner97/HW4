package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.sql.SQLException;
import java.util.List;

import databasePart1.*;

public class UserLoginPage {

    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // --- Back button ---
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));

        HBox topBar = new HBox(backBtn);
        topBar.setStyle("-fx-padding: 10; -fx-alignment: top-left;");

        // --- Username field ---
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        // --- Password field with preview functionality ---
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter Password");
        visiblePasswordField.setMaxWidth(250);
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        // Bind the text properties so they stay synchronized
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        // Toggle button for showing/hiding password
        Button showPasswordButton = new Button("👁");
        showPasswordButton.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
        showPasswordButton.setTooltip(new Tooltip("Show/Hide Password"));

        showPasswordButton.setOnAction(e -> {
            if (passwordField.isVisible()) {
                // Show password
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);
                showPasswordButton.setText("👁‍🗨");
            } else {
                // Hide password
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                showPasswordButton.setText("👁");
            }
        });

        // Stack both password fields in the same position
        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        passwordStack.setMaxWidth(250);

        // Password box with field and toggle button
        HBox passwordBox = new HBox(5);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.getChildren().addAll(passwordStack, showPasswordButton);

        VBox inputBox = new VBox(10, userNameField, passwordBox);
        inputBox.setStyle("-fx-alignment: center;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // --- Login and Forgot Password buttons ---
        Button loginButton = new Button("Login");
        loginButton.setOnAction(a -> handleLogin(userNameField, passwordField, errorLabel, primaryStage));

        Button resetPwButton = new Button("Forgot Password?");
        resetPwButton.setOnAction(e -> openPasswordResetPopup());

        HBox buttonBox = new HBox(10, loginButton, resetPwButton);
        buttonBox.setStyle("-fx-alignment: center;");

        VBox centerBox = new VBox(15, inputBox, buttonBox, errorLabel);
        centerBox.setStyle("-fx-alignment: center;");

        // --- Main layout ---
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topBar);
        mainLayout.setCenter(centerBox);
        mainLayout.setStyle("-fx-padding: 20;");

        primaryStage.setScene(new Scene(mainLayout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
    
    	// --- METHODS ---

    private void handleLogin(TextField userNameField, PasswordField passwordField, Label errorLabel, Stage primaryStage) {
        String userName = userNameField.getText();
        String password = passwordField.getText();

        try {
            String role = databaseHelper.getUserRole(userName);
            if (role == null) {
                errorLabel.setText("User account doesn't exist.");
                return;
            }

            String loginResult = databaseHelper.loginWithOTPcheck(userName, password, role);

            if ("normal".equals(loginResult)) {
            	List<String> roleList = StatusData.databaseHelper.allUserRoles(userName);
            	roleList.remove("user");
            	if(roleList.size() == 1) {
            		StatusData.currUser = User.createUser(userName, password, StatusData.databaseHelper.allUserRoles(userName).get(0));
            		StatusData.databaseHelper.loadUserDetails(StatusData.currUser);
            		new WelcomeLoginPage().show(primaryStage, StatusData.currUser);
            	} else {
            		new RoleSelectionPage(userName, password).show(StatusData.primaryStage);
            	}

            } else if ("temp".equals(loginResult)) {
                User user = User.createUser(userName, password, role);
                StatusData.currUser = user;
                errorLabel.setText("Please reset your password.");
                ResetPasswordPage resetPasswordPage = new ResetPasswordPage(databaseHelper, user);
                resetPasswordPage.show(StatusData.primaryStage, userName);

            } else {
                errorLabel.setText("Invalid username or password.");
            }
        } catch (SQLException ex) {
            errorLabel.setText("Database error during login.");
            ex.printStackTrace();
        }
    }

    private void openPasswordResetPopup() {
        Stage popup = new Stage();
        popup.setTitle("Password Reset");

        Label label1 = new Label("Please enter your username");
        label1.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");

        TextField unField = new TextField();
        unField.setPromptText("Enter your username");

        Label label2 = new Label("Please enter your email");
        label2.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(ev -> {
            String enteredUser = unField.getText();
            String enteredEmail = emailField.getText();

            if (enteredUser.isEmpty() || enteredEmail.isEmpty()) {
                messageLabel.setText("Both fields must be filled.");
                return;
            }

            String role = databaseHelper.getUserRole(enteredUser);
            if (role == null) {
                messageLabel.setText("Username does not exist.");
                return;
            }

            User user = User.createUser(enteredUser, "", role);
            databaseHelper.loadUserDetails(user);

            if (enteredEmail.equals(user.getEmail())) {
                messageLabel.setStyle("-fx-text-fill: green;");
                if (databaseHelper.requestedPw(enteredUser, enteredEmail)) {
                    messageLabel.setText("Check your email for a one-time reset code.");
                } else {
                    messageLabel.setText("Database error: request failed.");
                }
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Email does not match username.");
            }
        });

        VBox layout = new VBox(10,
                new Label("Confirm your account:"),
                label1, unField,
                label2, emailField,
                submitButton, messageLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center-left;");

        popup.setScene(new Scene(layout, 400, 250));
        popup.show();
    }
}