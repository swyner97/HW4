//package application;
//
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//import javafx.geometry.Pos;
//
//import java.sql.SQLException;
//import java.util.List;
//
//
//import databasePart1.*;
//
//public class SetupAccountPage {
//
//    private final DatabaseHelper databaseHelper;
//
//    public SetupAccountPage(DatabaseHelper databaseHelper) {
//        this.databaseHelper = databaseHelper;
//    }
//
//    public void show(Stage primaryStage) {
//    	// --- Back button ---
//        Button backBtn = new Button("Back");
//        backBtn.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
//
//        HBox topBar = new HBox(backBtn);
//        topBar.setStyle("-fx-padding: 10; -fx-alignment: top-left;");
//
//        // --- Input fields ---
//        TextField userNameField = new TextField();
//        userNameField.setPromptText("Enter Username");
//        userNameField.setMaxWidth(250);
//
//        // Password field with preview functionality
//        PasswordField passwordField = new PasswordField();
//        passwordField.setPromptText("Enter Password");
//        passwordField.setMaxWidth(250);
//
//        TextField visiblePasswordField = new TextField();
//        visiblePasswordField.setPromptText("Enter Password");
//        visiblePasswordField.setMaxWidth(250);
//        visiblePasswordField.setManaged(false);
//        visiblePasswordField.setVisible(false);
//
//        // Bind the text properties so they stay synchronized
//        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
//
//        // Toggle button for showing/hiding password
//        Button showPasswordButton = new Button("👁");
//        showPasswordButton.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
//        showPasswordButton.setTooltip(new Tooltip("Show/Hide Password"));
//
//        showPasswordButton.setOnAction(e -> {
//            if (passwordField.isVisible()) {
//                // Show password
//                passwordField.setVisible(false);
//                passwordField.setManaged(false);
//                visiblePasswordField.setVisible(true);
//                visiblePasswordField.setManaged(true);
//                showPasswordButton.setText("👁‍🗨");
//            } else {
//                // Hide password
//                visiblePasswordField.setVisible(false);
//                visiblePasswordField.setManaged(false);
//                passwordField.setVisible(true);
//                passwordField.setManaged(true);
//                showPasswordButton.setText("👁");
//            }
//        });
//
//        // Stack both password fields in the same position
//        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
//        passwordStack.setMaxWidth(250);
//
//        // Password box with field and toggle button
//        HBox passwordBox = new HBox(5);
//        passwordBox.setAlignment(Pos.CENTER);
//        passwordBox.getChildren().addAll(passwordStack, showPasswordButton);
//
//        TextField emailField = new TextField();
//        emailField.setPromptText("Enter E-mail address");
//        emailField.setMaxWidth(250);
//
//        TextField inviteCodeField = new TextField();
//        inviteCodeField.setPromptText("Enter Invitation Code");
//        inviteCodeField.setMaxWidth(250);
//
//        VBox inputBox = new VBox(10, userNameField, passwordBox, emailField, inviteCodeField);
//        inputBox.setStyle("-fx-alignment: center;");
//
//        Label errorLabel = new Label();
//        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
//
//        // --- Setup button ---
//        Button setupButton = new Button("Setup");
//        setupButton.setOnAction(a -> handleSetup(userNameField, passwordField, emailField, inviteCodeField, errorLabel, primaryStage));
//
//        VBox setupBox = new VBox(10, inputBox, setupButton, errorLabel);
//        setupBox.setStyle("-fx-alignment: center;");
//
//        // --- Requirements labels ---
//        Label requirementsHeader = new Label("Username and Password Requirements:");
//        requirementsHeader.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-text-fill: green;");
//        Label usernameRules = new Label("- Username must start with a letter and be 4–16 characters.");
//        Label passwordRules1 = new Label("- Password must be at least 8 characters.");
//        Label passwordRules2 = new Label("- Must contain: uppercase, lowercase, digit, special character.");
//        usernameRules.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
//        passwordRules1.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
//        passwordRules2.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
//
//        VBox requirementsBox = new VBox(5, requirementsHeader, usernameRules, passwordRules1, passwordRules2);
//        requirementsBox.setStyle("-fx-alignment: center-left; -fx-padding: 10;");
//
//        // --- Main layout ---
//        BorderPane mainLayout = new BorderPane();
//        mainLayout.setTop(topBar);
//        mainLayout.setCenter(setupBox);
//        mainLayout.setBottom(requirementsBox);
//        mainLayout.setStyle("-fx-padding: 20;");
//
//        primaryStage.setScene(new Scene(mainLayout, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT));
//        primaryStage.setTitle("Account Setup");
//        primaryStage.show();
//    }
//
//    private void handleSetup(TextField userNameField, PasswordField passwordField, TextField emailField, TextField inviteCodeField, Label errorLabel, Stage primaryStage) {
//        try {
//            String userName = userNameField.getText();
//            String userNameError = UserNameRecognizer.checkForValidUserName(userName);
//            if (!userNameError.isEmpty()) {
//                errorLabel.setText(userNameError);
//                return;
//            }
//
//            String password = passwordField.getText();
//            String passwordError = PasswordRecognizer.evaluatePassword(password);
//            if (!passwordError.isEmpty()) {
//                errorLabel.setText(passwordError);
//                return;
//            }
//
//            String email = emailField.getText();
//            String emailError = EmailRecognizer.validate(email);
//            if (!emailError.isEmpty()) {
//                errorLabel.setText(emailError);
//                return;
//            }
//
//            String code = inviteCodeField.getText();
//            List<String> rolesForCode = databaseHelper.allCodeRoles(code);
//            
//            if (!databaseHelper.doesUserExist(userName)) {
//                if (databaseHelper.validateInvitationCode(code)) {
//                    User user = User.createUser(userName, password, "user");
//                    user.setName(userName);
//                    user.setEmail(email);
//                    databaseHelper.register(user);
//                    System.out.println("DEBUG: checking userId: " + user.getId());
//                    StatusData.currUser = user;
//                    StatusData.currUser.setName(userName);
//                    //this is to put the user role into addUserRoles in database!
//                    databaseHelper.addUserRoles(
//                    	    StatusData.currUser.getUserName(),
//                    	    StatusData.currUser.getRole().name()
//                    	);
//
//                    //connect invitation code to username so database can store it:
//                    for (String role : rolesForCode) {
//                        databaseHelper.addUserRoles(user.getUserName(), role);
//                    }
//                    //debugging purposes
//                    System.out.println("Invitation Roles Given:" + databaseHelper.allCodeRoles(code) + "\nAll UserRole" + 
//                    databaseHelper.allUserRoles(user.getUserName()));
//                    
//                    List<String> roleList = StatusData.databaseHelper.allUserRoles(userName);
//                	roleList.remove("user");
//                	if(roleList.size() == 1) {
//                		//StatusData.currUser = User.createUser(userName, password, StatusData.databaseHelper.allUserRoles(userName).get(0));
//                		//StatusData.databaseHelper.loadUserDetails(StatusData.currUser);
//                		String finalRole = roleList.get(0);
//                		User dbUser = databaseHelper.getUserByName(userName);
//                		if (dbUser != null) {
//                			dbUser.setRole(finalRole);
//                			StatusData.currUser = dbUser;
//                		} else {
//                			StatusData.currUser.setRole(finalRole);
//                		}
//                		new WelcomeLoginPage().show(primaryStage, StatusData.currUser);
//                	} else {
//                		new RoleSelectionPage(userName, password).show(StatusData.primaryStage);
//                	}
//                } else {
//                    errorLabel.setText("Please enter a valid invitation code");
//                }
//            } else {
//                errorLabel.setText("This Username is taken! Please choose another.");
//            }
//        } catch (SQLException e) {
//            System.err.println("Database error: " + e.getMessage());
//            e.printStackTrace();
//            errorLabel.setText("Database error occurred.");
//        }
//    }
//}
//package application;
//
//package pages;
//
//
