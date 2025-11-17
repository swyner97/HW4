package pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import model.User;
import logic.*;

import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;

/**
 * InitialAccessPage builds and shows the initial access UI used for
 * login, sign-up and password reset flows.  The page composes three
 * main panes (login, sign up, reset) inside a centered card and wires
 * handlers to swap between them.
 *
 * <p>The class depends on a {@link databasePart1.DatabaseHelper} instance
 * that is supplied in the constructor for database operations (user lookup,
 * registration, password reset requests, etc.). UI actions will call the
 * database helper and display alerts / inline error messages accordingly.
 *
 * <p>Usage example:
 * <pre>
 *     InitialAccessPage page = new InitialAccessPage(databaseHelper);
 *     page.show(primaryStage);
 * </pre>
 *
 * @author Sarah Wyner
 * @since 1.0
 */


public class InitialAccessPage {

    private final DatabaseHelper databaseHelper;
    
    /**
     * Create an InitialAccessPage backed by the provided {@code DatabaseHelper}.
     *
     * @param databaseHelper the database helper to use for registration and login calls
     */

    public InitialAccessPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Show the initial access window on the given {@code Stage}. This builds the UI
     * (background, card layout, login/reset/signup panes) and displays the stage.
     *
     * @param primaryStage the JavaFX Stage to show the UI on
     */
    
    public void show(Stage primaryStage) {

        HBox mainContainer = new HBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));

        try {
            Image image = new Image(getClass().getResourceAsStream("src/6073083.jpg"));
            BackgroundImage backgroundImage = new BackgroundImage(image,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    new BackgroundSize(100, 100, true, true, false, true));
            mainContainer.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            mainContainer.setStyle("-fx-background-color: #6A9FBD;");
        }

        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        card.setMaxWidth(400);
        card.setMinWidth(400);

        // Build the three panes
        VBox loginPane = buildLoginPane(card, primaryStage);
        VBox resetPane = buildResetPane(card);
        VBox signUpPane = buildSignUpPane(card, primaryStage);

        // Store all panes in card properties
        card.getProperties().put("loginPane", loginPane);
        card.getProperties().put("resetPane", resetPane);
        card.getProperties().put("signUpPane", signUpPane);

        // Wire handlers BEFORE copying children
        wireLoginHandlers(card, loginPane);
        wireSignUpHandlers(card, signUpPane);

        // Initially show login pane
        card.getChildren().setAll(loginPane);  // Changed from loginPane.getChildren()

        mainContainer.getChildren().add(card);

        Scene scene = new Scene(mainContainer, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    // Wire handlers after all panes are created
    private void wireLoginHandlers(VBox card, VBox loginPane) {
        // Find the "Forgot Password?" hyperlink
        Hyperlink forgotPassword = findNodeByText(loginPane, Hyperlink.class, "Forgot Password?");
        if (forgotPassword != null) {
            forgotPassword.setOnAction(e -> {
                VBox rp = (VBox) card.getProperties().get("resetPane");
                if (rp != null) showReset(card, rp);
            });
        }

        // Find the "SIGN UP" hyperlink
        Hyperlink signUpHyperlink = findNodeByText(loginPane, Hyperlink.class, "SIGN UP");
        if (signUpHyperlink != null) {
            signUpHyperlink.setOnAction(e -> {
                VBox sp = (VBox) card.getProperties().get("signUpPane");
                if (sp != null) showSignUp(card, sp);
            });
        }
    }

    private void wireSignUpHandlers(VBox card, VBox signUpPane) {
        // Find the "LOGIN" hyperlink in sign up pane
        Hyperlink loginHyperlink = findNodeByText(signUpPane, Hyperlink.class, "LOGIN");
        if (loginHyperlink != null) {
            loginHyperlink.setOnAction(e -> {
                VBox lp = (VBox) card.getProperties().get("loginPane");
                if (lp != null) showLogin(card, lp);
            });
        }
    }

    // Helper to find a node by text
    @SuppressWarnings("unchecked")
    private <T> T findNodeByText(javafx.scene.Parent parent, Class<T> type, String text) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (type.isInstance(node)) {
                if (node instanceof Hyperlink && ((Hyperlink) node).getText().equals(text)) {
                    return (T) node;
                } else if (node instanceof Label && ((Label) node).getText().equals(text)) {
                    return (T) node;
                }
            }
            if (node instanceof javafx.scene.Parent) {
                T result = findNodeByText((javafx.scene.Parent) node, type, text);
                if (result != null) return result;
            }
        }
        return null;
    }

    // -- helpers to swap panes --
    private void showLogin(VBox card, VBox loginPane) {
        card.getChildren().setAll(loginPane);  // Changed from loginPane.getChildren()
    }

    private void showReset(VBox card, VBox resetPane) {
        card.getChildren().setAll(resetPane);  // Changed from resetPane.getChildren()
    }

    private void showSignUp(VBox card, VBox signUpPane) {
        card.getChildren().setAll(signUpPane);  // Changed from signUpPane.getChildren()
    }

    // ---- LOGIN PANE ----
    private VBox buildLoginPane(VBox card, Stage primaryStage) {
        VBox login = new VBox(15);
        login.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = new Label("LOGIN");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#555555"));

        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("System", 14));
        usernameLabel.setTextFill(Color.web("#555555"));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 10;");
        usernameField.setPrefHeight(40);
        VBox usernameBox = new VBox(5, usernameLabel, usernameField);

        // Password with show/hide
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("System", 14));
        passwordLabel.setTextFill(Color.web("#555555"));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 10;");
        passwordField.setPrefHeight(40);

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter password");
        visiblePasswordField.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 10;");
        visiblePasswordField.setPrefHeight(40);
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        Button showPasswordButton = new Button("👁");
        showPasswordButton.setStyle("-fx-font-size: 14px; -fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: transparent;");
        showPasswordButton.setOnAction(e -> {
            if (passwordField.isVisible()) {
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);
                showPasswordButton.setText("👁‍🗨");
            } else {
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                showPasswordButton.setText("👁");
            }
        });

        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        HBox passwordInputBox = new HBox(5, passwordStack, showPasswordButton);
        passwordInputBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passwordStack, Priority.ALWAYS);

        VBox passwordBox = new VBox(5, passwordLabel, passwordInputBox);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        errorLabel.setWrapText(true);

        // Login button
        Button loginBtn = new Button("LOGIN");
        loginBtn.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 25; -fx-cursor: hand;");
        loginBtn.setPrefHeight(45);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin(usernameField, passwordField, errorLabel, primaryStage));

        // Forgot password hyperlink (handler wired later)
        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        forgotPassword.setStyle("-fx-text-fill: #999999; -fx-underline: false;");
        forgotPassword.setAlignment(Pos.CENTER_RIGHT);
        forgotPassword.setMaxWidth(Double.MAX_VALUE);

        // OR separator
        HBox orSeparator = createSeparator("OR");

        // Sign up link (handler wired later)
        HBox signUpLink = new HBox(5);
        signUpLink.setAlignment(Pos.CENTER);
        Label needAccount = new Label("Need an account?");
        needAccount.setTextFill(Color.web("#888888"));
        Hyperlink signUpHyperlink = new Hyperlink("SIGN UP");
        signUpHyperlink.setStyle("-fx-text-fill: #2c3e50; -fx-underline: true;");
        signUpLink.getChildren().addAll(needAccount, signUpHyperlink);
        
     // --- DEV quick-login (persistent dev users) ---
        if (StatusData.DEV_MODE) {
            MenuButton devLogin = new MenuButton("DEV: Quick Login");
            devLogin.setStyle("-fx-background-color: #ffeb3b; -fx-text-fill: #000; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 6;");
            devLogin.setTooltip(new Tooltip("Developer-only: create/login as a dev user with appropriate DB roles"));

            for (User.Role role : User.Role.values()) {
                if (role == User.Role.UNKNOWN) continue;
                MenuItem mi = new MenuItem(role.name());
                mi.setOnAction(ev -> {
                    // run DB-backed dev login so permissions exist
                    String devUserName = "dev_" + role.name().toLowerCase();
                    try {
                        // try to find existing DB user
                        User dbUser = databaseHelper.getUserByName(devUserName);

                        if (dbUser == null) {
                            // create a persistent dev user in DB with a simple password (only for dev)
                            User toCreate = User.createUser(devUserName, "devpass", role, role.name() + " (dev)", devUserName + "@dev.local", null);
                            // register will set generated id into user
                            databaseHelper.register(toCreate);
                            // ensure role row exists in UserRoles
                            databaseHelper.addUserRoles(toCreate.getUserName(), role);
                            // load details to get id/email/etc.
                            databaseHelper.loadUserDetails(toCreate);
                            dbUser = toCreate;
                            System.out.println("Created dev user: " + devUserName + " role: " + role);
                        } else {
                            // ensure DB role row exists (idempotent)
                            databaseHelper.addUserRoles(dbUser.getUserName(), role);
                            // ensure User object has the role
                            dbUser.setRole(role);
                            databaseHelper.loadUserDetails(dbUser);
                            System.out.println("Found existing dev user: " + devUserName + " - ensured role: " + role);
                        }

                        // set current user and navigate
                        StatusData.currUser = dbUser;
                        Stage window = (Stage) devLogin.getScene().getWindow();
                        new WelcomeLoginPage().show(window, dbUser);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alert a = new Alert(Alert.AlertType.ERROR, "DEV login failed: " + ex.getMessage(), ButtonType.OK);
                        a.showAndWait();
                    }
                });
                devLogin.getItems().add(mi);
            }

            // put under login button
            VBox.setMargin(devLogin, new Insets(6, 0, 0, 0));
            login.getChildren().add(devLogin);
        }



        // assemble login pane
        login.getChildren().addAll(title, usernameBox, passwordBox, errorLabel, loginBtn, forgotPassword, orSeparator, signUpLink);

        return login;
    }

    // ---- RESET PANE ----
    public VBox buildResetPane(VBox card) {
        VBox reset = new VBox(10);
        reset.setAlignment(Pos.TOP_CENTER);

        Label header = new Label("Reset Password");
        header.setFont(Font.font("System", FontWeight.BOLD, 20));
        header.setTextFill(Color.web("#555555"));

        Label userLabel = new Label("Please enter your username");
        userLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
        TextField unField = new TextField();
        unField.setPromptText("Enter your username");
        unField.setPrefHeight(36);

        Label emailLabel = new Label("Please enter your email");
        emailLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefHeight(36);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px");
        messageLabel.setWrapText(true);

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-cursor: hand; -fx-background-radius: 20;");
        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-cursor: hand; -fx-background-radius: 20;");
        buttonRow.getChildren().addAll(backButton, submitButton);

        VBox resetBox = new VBox(8,
                new Label("Confirm your account:"),
                userLabel, unField,
                emailLabel, emailField,
                buttonRow,
                messageLabel
        );
        resetBox.setAlignment(Pos.CENTER_LEFT);

        reset.getChildren().addAll(header, resetBox);

        // Submit logic
        submitButton.setOnAction(ev -> {
            String enteredUser = unField.getText();
            String enteredEmail = emailField.getText();

            if (enteredUser == null || enteredUser.trim().isEmpty() ||
                enteredEmail == null || enteredEmail.trim().isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Both fields must be filled.");
                return;
            }

            try {
                String role = databaseHelper.getUserRole(enteredUser);
                if (role == null) {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Username does not exist.");
                    return;
                }

                User user = User.createUser(enteredUser, "", role);
                databaseHelper.loadUserDetails(user);

                if (enteredEmail.equals(user.getEmail())) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    boolean ok = databaseHelper.requestedPw(enteredUser, enteredEmail);
                    if (ok) {
                        messageLabel.setText("Check your email for a one-time reset code.");
                    } else {
                        messageLabel.setText("Database error: request failed.");
                    }
                } else {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Email does not match username.");
                }
            } catch (Exception ex) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Back button restores login pane
        backButton.setOnAction(ev -> {
            VBox loginPane = (VBox) card.getProperties().get("loginPane");
            if (loginPane != null) showLogin(card, loginPane);
        });

        return reset;
    }

    // ---- SIGN UP PANE ----
    private VBox buildSignUpPane(VBox card, Stage primaryStage) {
        VBox signUp = new VBox(15);
        signUp.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("SIGN UP");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#555555"));

        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("System", 14));
        usernameLabel.setTextFill(Color.web("#555555"));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 10;");
        usernameField.setPrefHeight(40);
        VBox usernameBox = new VBox(5, usernameLabel, usernameField);

        // Password (with show/hide)
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("System", 14));
        passwordLabel.setTextFill(Color.web("#555555"));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 10;");
        passwordField.setPrefHeight(40);
        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter password");
        visiblePasswordField.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 10;");
        visiblePasswordField.setPrefHeight(40);
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
        Button showPasswordButton = new Button("👁");
        showPasswordButton.setStyle("-fx-font-size: 14px; -fx-cursor: hand; -fx-background-color: transparent; -fx-border-color: transparent;");
        showPasswordButton.setOnAction(e -> {
            if (passwordField.isVisible()) {
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);
                showPasswordButton.setText("👁‍🗨");
            } else {
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                showPasswordButton.setText("👁");
            }
        });
        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        HBox passwordInputBox = new HBox(5, passwordStack, showPasswordButton);
        passwordInputBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passwordStack, Priority.ALWAYS);
        VBox passwordBox = new VBox(5, passwordLabel, passwordInputBox);

        // Invitation Code
        Label inviteLabel = new Label("Invitation Code");
        inviteLabel.setFont(Font.font("System", 14));
        inviteLabel.setTextFill(Color.web("#555555"));
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setStyle("-fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 5; -fx-padding: 10;");
        VBox inviteBox = new VBox(5, inviteLabel, inviteCodeField);

        // Error & requirements labels
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(320);

        Label requirementsLabel = new Label(
                "Requirements:\n" +
                        "• Username: 4-16 chars, starts with letter\n" +
                        "• Password: 8+ chars, upper, lower, digit, special"
        );
        requirementsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        requirementsLabel.setWrapText(true);

        // Sign Up button
        Button signUpBtn = new Button("SIGN UP");
        signUpBtn.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 25; -fx-cursor: hand;");
        signUpBtn.setPrefHeight(45);
        signUpBtn.setMaxWidth(Double.MAX_VALUE);
        signUpBtn.setOnAction(e -> handleSignUp(usernameField, passwordField, inviteCodeField, errorLabel, signUpBtn, primaryStage));

        // OR separator
        HBox orSeparator = createSeparator("OR");

        // Login link (handler wired later)
        HBox loginLink = new HBox(5);
        loginLink.setAlignment(Pos.CENTER);
        Label alreadyUser = new Label("Already a user?");
        alreadyUser.setTextFill(Color.web("#888888"));
        Hyperlink loginHyperlink = new Hyperlink("LOGIN");
        loginHyperlink.setStyle("-fx-text-fill: #2c3e50; -fx-underline: true;");
        loginLink.getChildren().addAll(alreadyUser, loginHyperlink);

        signUp.getChildren().addAll(title, usernameBox, passwordBox, inviteBox, requirementsLabel, errorLabel, signUpBtn, orSeparator, loginLink);

        return signUp;
    }
    
    /**
     * Create a horizontal separator with a centered label.
     *
     * @param text the label text to show between two lines (often "OR")
     * @return an HBox containing two lines and the centered label
     */

    private HBox createSeparator(String text) {
        HBox separator = new HBox(10);
        separator.setAlignment(Pos.CENTER);

        Region leftLine = new Region();
        HBox.setHgrow(leftLine, Priority.ALWAYS);
        leftLine.setStyle("-fx-background-color: #DDDDDD; -fx-pref-height: 1;");

        Label orLabel = new Label(text);
        orLabel.setTextFill(Color.web("#999999"));
        orLabel.setStyle("-fx-font-size: 12px;");

        Region rightLine = new Region();
        HBox.setHgrow(rightLine, Priority.ALWAYS);
        rightLine.setStyle("-fx-background-color: #DDDDDD; -fx-pref-height: 1;");

        separator.getChildren().addAll(leftLine, orLabel, rightLine);
        return separator;
    }
    
    /**
    * Handle the sign-up flow: validate input, create a {@link model.User},
    * register it in the database and navigate to the welcome page on success.
    *
    * The method updates the provided {@code errorLabel} with validation or
    * error messages and disables the {@code signUpBtn} while performing work.
    *
    * @param usernameField the username input field
    * @param passwordField the password input field
    * @param inviteCodeField invitation code input field (may be optional)
    * @param errorLabel label used to display inline error messages
    * @param signUpBtn the sign-up button (disabled while processing)
    * @param primaryStage the stage used to show the welcome page on success
    */

    // ----- SIGN UP HANDLER -----
    public void handleSignUp(TextField usernameField, PasswordField passwordField, TextField inviteCodeField, Label errorLabel, Button signUpBtn, Stage primaryStage) {
        String userName = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        // Validate username
        String userNameError = UserNameRecognizer.checkForValidUserName(userName);
        if (!userNameError.isEmpty()) {
            errorLabel.setText(userNameError);
            return;
        }

        // Validate password
        String passwordError = PasswordRecognizer.evaluatePassword(password);
        if (!passwordError.isEmpty()) {
            errorLabel.setText(passwordError);
            return;
        }

        signUpBtn.setDisable(true);
        errorLabel.setText("");

        try {
            // Create user with STUDENT role by default
            User user = User.createUser(userName, password, User.Role.STUDENT, userName, "", null);
            StatusData.currUser = user;
            
            

            // Register in DB
            databaseHelper.register(user);
            databaseHelper.addUserRoles(user.getUserName(), user.getRole());
            
            // Show announcement popup if there are unread announcements
            AnnouncementPopup.showIfNeeded(primaryStage, user);


            Alert success = new Alert(Alert.AlertType.INFORMATION, "Account created successfully!", ButtonType.OK);
            success.showAndWait();

            new WelcomeLoginPage().show(primaryStage, user);

        } catch (SQLException e) {
            String errorMessage = "Database error occurred during registration: " + e.getMessage();
            System.err.println(errorMessage);
            errorLabel.setText("Registration failed. Please try again later.");
            signUpBtn.setDisable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Unexpected error: " + ex.getMessage());
            signUpBtn.setDisable(false);
        }
    }
    
    /**
     * Handle login attempts. This method checks the username/password with the
     * database helper and will navigate to the welcome page, a role selection
     * page, or a password reset flow depending on the result.
     *
     * @param userNameField username input field
     * @param passwordField password input field
     * @param errorLabel label to display login errors
     * @param primaryStage the stage to navigate on successful login
     */

    public void handleLogin(TextField userNameField, PasswordField passwordField, Label errorLabel, Stage primaryStage) {
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
                List<String> roleList = databaseHelper.allUserRoles(userName);
                roleList.remove("user");
                if (roleList.size() == 1) {
                    String finalRole = roleList.get(0);

                    User dbUser = databaseHelper.getUserByName(userName);
                    if (dbUser == null) {
                        errorLabel.setText("Error loading user from database");
                        return;
                    }

                    dbUser.setRole(finalRole);
                    StatusData.currUser = dbUser;

                    new WelcomeLoginPage().show(primaryStage, StatusData.currUser);
                } else {
                    new RoleSelectionPage(userName, password).show(primaryStage);
                }

            } else if ("temp".equals(loginResult)) {
                User user = User.createUser(userName, password, role);
                StatusData.currUser = user;
                errorLabel.setText("Please reset your password.");
                databaseHelper.loadUserDetails(user);
                ResetPasswordPage resetPasswordPage = new ResetPasswordPage(databaseHelper, user);
                resetPasswordPage.show(primaryStage, "Please reset your temporary password");

            } else {
                errorLabel.setText("Login failed.");
            }

        } catch (Exception ex) {
            errorLabel.setText("Database error during login.");
            ex.printStackTrace();
        }
    }
}