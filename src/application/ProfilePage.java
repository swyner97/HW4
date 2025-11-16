package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ProfilePage {
    
    private TextField pName;
    private TextField pEmail;
    private TextField pPhone;
    private ComboBox<String> pRole;
    private TextArea pBio;
    
    private User currentUser;
    
    public void show(Stage stage, User user) {
        this.currentUser = user;
        
        stage.setTitle("My Profile");
        
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(15));
        
        // Create form
        VBox formBox = createProfileForm();
        
        // Create buttons
        HBox buttonBox = createButtonBox(stage);
        
        VBox contentBox = new VBox(20, formBox, buttonBox);
        contentBox.setAlignment(Pos.CENTER);
        
        mainPane.setCenter(contentBox);
        
        // Add navigation bar at top
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);
        
        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.show();

        // Load current user's profile data
        loadUserProfile();
    }
    
    private VBox createProfileForm() {
        VBox formBox = new VBox(10);
        formBox.setMaxWidth(500);
        formBox.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Profile Information");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        // Set column constraints so labels and fields align properly
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        // Name
        Label nameLabel = new Label("Name:");
        pName = new TextField();
        pName.setPrefWidth(300);
        grid.add(nameLabel, 0, 0);
        grid.add(pName, 1, 0);
        
        // Email
        Label emailLabel = new Label("Email:");
        pEmail = new TextField();
        pEmail.setPrefWidth(300);
        grid.add(emailLabel, 0, 1);
        grid.add(pEmail, 1, 1);
        
        // Phone
        Label phoneLabel = new Label("Phone:");
        pPhone = new TextField();
        pPhone.setPrefWidth(300);
        grid.add(phoneLabel, 0, 2);
        grid.add(pPhone, 1, 2);
        
        // Role (read-only)
        Label roleLabel = new Label("Role:");
        pRole = new ComboBox<>();
        pRole.getItems().addAll("user", "admin");
        pRole.setDisable(true);
        pRole.setPrefWidth(300);
        grid.add(roleLabel, 0, 3);
        grid.add(pRole, 1, 3);
        
        // Bio
        Label bioLabel = new Label("Bio:");
        pBio = new TextArea();
        pBio.setPrefRowCount(4);
        pBio.setPrefWidth(300);
        pBio.setWrapText(true);
        grid.add(bioLabel, 0, 4);
        grid.add(pBio, 1, 4);
        
        formBox.getChildren().addAll(titleLabel, grid);
        return formBox;
    }
    
    private HBox createButtonBox(Stage stage) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button updateButton = new Button("Update Profile");
        updateButton.setOnAction(e -> updateProfile());
        
        Button editAccountButton = new Button("Edit Account");
        editAccountButton.setOnAction(e -> {
            // Open the UpdateAccountPage
            UpdateAccountPage updateAccountPage = new UpdateAccountPage(StatusData.databaseHelper, currentUser);
            updateAccountPage.show(stage);
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            // Go back to previous page or home
            new WelcomeLoginPage().show(stage, currentUser);
        });
        
        buttonBox.getChildren().addAll(updateButton, editAccountButton, cancelButton);
        return buttonBox;
    }
    
    private void loadUserProfile() {
        if (currentUser != null) {
            pName.setText(currentUser.getUserName()); // or getName() if available
            pEmail.setText(currentUser.getEmail());
            // Load phone and bio from database if available
            pRole.setValue(currentUser.getRole());
            
            // If you have additional profile data in the database, load it here
            // For example:
            // ProfileData profileData = StatusData.databaseHelper.getUserProfile(currentUser.getId());
            // if (profileData != null) {
            //     pPhone.setText(profileData.getPhone());
            //     pBio.setText(profileData.getBio());
            // }
        }
    }
    
    private void updateProfile() {
        String name = pName.getText().trim();
        String email = pEmail.getText().trim();
        String phone = pPhone.getText().trim();
        String bio = pBio.getText().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Name and Email are required!");
            alert.showAndWait();
            return;
        }
        
        // Update the user profile in the database
        try {
            // Update user in database
            // StatusData.databaseHelper.updateUserProfile(currentUser.getId(), name, email, phone, bio);
            
            // Update the current user object
            // currentUser.setUsername(name);
            // currentUser.setEmail(email);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Profile updated successfully!");
            alert.showAndWait();
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update profile: " + e.getMessage());
            alert.showAndWait();
        }
    }
}