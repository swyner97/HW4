package pages;

import databasePart1.DatabaseHelper;
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
import logic.PasswordRecognizer;
import logic.StatusData;
import logic.UserNameRecognizer;
import model.User;

import java.sql.SQLException;

public class AdminSetupPage {

    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {

        HBox mainContainer = new HBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));

        try {
            Image image = new Image(getClass().getResourceAsStream("/application/img/uyfydu4.jpg"));
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

        // Build admin setup pane (looks identical style-wise to InitialAccessPage panes)
        VBox adminPane = buildAdminPane(card, primaryStage);

        // store in properties in case later needed
        card.getProperties().put("adminPane", adminPane);

        card.getChildren().setAll(adminPane);

        mainContainer.getChildren().add(card);

        Scene scene = new Scene(mainContainer, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }

    private VBox buildAdminPane(VBox card, Stage primaryStage) {
        VBox pane = new VBox(15);
        pane.setAlignment(Pos.TOP_CENTER);

        // Title
        Label title = new Label("ADMIN SETUP");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#555555"));

        // Username
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("System", 14));
        usernameLabel.setTextFill(Color.web("#555555"));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter admin username");
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

        // Requirements & Error label
        Label requirementsLabel = new Label(
                "Requirements:\n" +
                        "• Username: 4-16 chars, starts with letter\n" +
                        "• Password: 8+ chars, upper, lower, digit, special"
        );
        requirementsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        requirementsLabel.setWrapText(true);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(320);

        // Setup button
        Button setupBtn = new Button("CREATE ADMIN");
        setupBtn.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 25; -fx-cursor: hand;");
        setupBtn.setPrefHeight(45);
        setupBtn.setMaxWidth(Double.MAX_VALUE);
        setupBtn.setOnAction(e -> handleSetup(usernameField, passwordField, errorLabel, setupBtn, primaryStage));

        // Back to login link (in case you want to go back to initial access)
        HBox loginLink = new HBox(5);
        loginLink.setAlignment(Pos.CENTER);
        Label alreadyUser = new Label("Already have an account?");
        alreadyUser.setTextFill(Color.web("#888888"));
        Hyperlink loginHyperlink = new Hyperlink("Go to Login");
        loginHyperlink.setStyle("-fx-text-fill: #2c3e50; -fx-underline: true;");
        loginHyperlink.setOnAction(ev -> {
            VBox loginPane = (VBox) card.getProperties().get("loginPane");
            if (loginPane != null) {
                card.getChildren().setAll(loginPane);
            }
        });
        loginLink.getChildren().addAll(alreadyUser, loginHyperlink);

        pane.getChildren().addAll(title, usernameBox, passwordBox, requirementsLabel, errorLabel, setupBtn, loginLink);

        return pane;
    }

    private void handleSetup(TextField usernameField, PasswordField passwordField, Label errorLabel, Button setupBtn, Stage primaryStage) {
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

        setupBtn.setDisable(true);
        errorLabel.setText("");

        try {
            // create user with ADMIN enum role if your User model supports it
            User user;
            try {
                user = User.createUser(userName, password, User.Role.ADMIN, userName, "", null);
            } catch (NoSuchMethodError | ClassCastException ex) {
                // fallback if createUser expects role as String
                user = User.createUser(userName, password, "admin", userName, "", null);
            }

            user.setName(userName);
            StatusData.currUser = user;

            // Register in DB
            databaseHelper.register(user);

           
            databaseHelper.addUserRoles(user.getUserName(), user.getRole());


            Alert success = new Alert(Alert.AlertType.INFORMATION, "Administrator account created.", ButtonType.OK);
            success.showAndWait();

            new WelcomeLoginPage().show(primaryStage, user);

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error: " + e.getMessage());
            setupBtn.setDisable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Unexpected error: " + ex.getMessage());
            setupBtn.setDisable(false);
        }
    }
}
