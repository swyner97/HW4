package pages;

import databasePart1.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.StatusData;
import model.NavigationBar;
import model.User;
import logic.UserNameRecognizer;
import logic.EmailRecognizer;
import logic.PasswordRecognizer;

import java.sql.SQLException;

public class ProfilePage {
    
	private final DatabaseHelper databaseHelper = StatusData.databaseHelper;
	private final User currentUser = StatusData.currUser;

    private TextField pName;
    private TextField pUserName;
    private TextField pEmail;
    private TextField pPhone;
    private ComboBox<String> pRole;
    private TextArea pBio;
    private PasswordField currentPwField, newPwField, confirmPwField;
    private Label errorLabel;  
    
    String oldUserName = StatusData.currUser.getUserName();
    String oldEmail = StatusData.currUser.getEmail();
    
    public void show(Stage stage, User user) {
        user = StatusData.currUser;
        
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
        
        // Use StatusData constants for window size
        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();

        // Load current user's profile data
        loadUserProfile();
    }
    private VBox createProfileForm() {
        VBox formBox = new VBox(10);
        formBox.setMaxWidth(500);
        formBox.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Profile & Account Information");
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
        
        // User Name
        Label userLabel = new Label("Username:");
        pUserName = new TextField(currentUser.getUserName());
        pUserName.setPrefWidth(300);
        grid.add(userLabel, 0, 0);
        grid.add(pUserName, 1, 0);
        
        // Name
        Label nameLabel = new Label("Name:");
        pName = new TextField(currentUser.getName());
        pName.setPrefWidth(300);
        grid.add(nameLabel, 0, 1);
        grid.add(pName, 1, 1);
        
        // Email
        Label emailLabel = new Label("Email:");
        pEmail = new TextField(currentUser.getEmail());
        pEmail.setPrefWidth(300);
        grid.add(emailLabel, 0, 2);
        grid.add(pEmail, 1, 2);
        
        // Phone
        Label phoneLabel = new Label("Phone:");
        pPhone = new TextField(currentUser.getPhone());
        pPhone.setPrefWidth(300);
        grid.add(phoneLabel, 0, 3);
        grid.add(pPhone, 1, 3);
        
        // Role (read-only)
        Label roleLabel = new Label("Role:");
        pRole = new ComboBox<>();
        pRole.getItems().addAll("user", "admin");
        pRole.setDisable(true);
        pRole.setPrefWidth(300);
        grid.add(roleLabel, 0, 4);
        grid.add(pRole, 1, 4);
        
        // Bio
        Label bioLabel = new Label("Bio:");
        pBio = new TextArea(currentUser.getBio());
        pBio.setPrefRowCount(4);
        pBio.setPrefWidth(300);
        pBio.setWrapText(true);
        grid.add(bioLabel, 0, 5);
        grid.add(pBio, 1, 5);
        
        //Password fields
        Label currentPwLabel = new Label("Current Password");
        currentPwField = new PasswordField();
        currentPwField.setPromptText("Enter Current Password");
        grid.add(currentPwLabel, 0, 6);
        grid.add(currentPwField, 1, 6);

        Label newPwLabel = new Label("New Password");
        newPwField = new PasswordField();
        newPwField.setPromptText("Enter New Password");
        grid.add(newPwLabel, 0, 7);
        grid.add(newPwField, 1, 7);
        
        Label confirmPwLabel = new Label("Confirm New Password");
        confirmPwField = new PasswordField();
        confirmPwField.setPromptText("Confirm New Password");
        grid.add(confirmPwLabel, 0, 8);
        grid.add(confirmPwField, 1, 8);
        
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
        grid.add(errorLabel, 1, 9);
        
        // ScrollPane to display the error labels with the room they need
        ScrollPane errorScrollPane = new ScrollPane(errorLabel);
        errorScrollPane.setPrefWidth(300);
        errorScrollPane.setPrefViewportHeight(100);
        errorScrollPane.setStyle("-fx-background-color:transparent; -fx-padding: 5;");
        
        formBox.getChildren().addAll(titleLabel, grid);
        return formBox;
    }
    
    private HBox createButtonBox(Stage stage) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button updateButton = new Button("Update Profile");
        updateButton.setOnAction(e -> {
        	updateProfile();
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            // Go back to previous page or home
            new WelcomeLoginPage().show(stage, currentUser);
        });
        
        buttonBox.getChildren().addAll(updateButton, cancelButton);
        return buttonBox;
    }
    
    private void loadUserProfile() {
    	
        String oldUserName = currentUser.getUserName();  
    	String oldEmail = currentUser.getEmail();
        
        if (currentUser != null) {
        	pUserName.setText(oldUserName);
        	
        	if (currentUser.getName() != null && !currentUser.getName().isEmpty()) {
        		pName.setText(currentUser.getName()); // or getName() if available
        	}
        	else {
            	pName.setPromptText("Enter Name");
            }
        	
            pEmail.setText(oldEmail);
            // Load phone and bio from database if available
            pRole.setValue(currentUser.getRole().name());

            
            if (currentUser.getPhone() != null && !currentUser.getPhone().isEmpty()) {
            	pPhone.setText(currentUser.getPhone());
            }
            if (currentUser.getBio() != null && !currentUser.getBio().isEmpty()) {
            	pBio.setText(currentUser.getBio());
            }
        }
       
    }
    
    private void updateProfile() {

    	String newUserName = pUserName.getText().trim();
        String name = pName.getText().trim();
        String email = pEmail.getText().trim();
        String phone = pPhone.getText().trim();
        String bio = pBio.getText().trim();
        String currentPw = currentPwField.getText().trim();
        String newPw = newPwField.getText().trim();
        String confirmPw = confirmPwField.getText().trim();
        
        if (name.isEmpty() || email.isEmpty() || newUserName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Name, Username and Email are required!");
            alert.showAndWait();
            return;
        }
        
        if (!newUserName.equals(oldUserName)) {
         	
         	String userNameError = UserNameRecognizer.checkForValidUserName(newUserName);
         	if (!userNameError.isEmpty()) {
         		System.out.println(userNameError);
         		errorLabel.setText(userNameError);
         		return;
         	}
         	currentUser.setUserName(newUserName);
         }
         
        //Verify password for security
        if (!currentPw.equals(currentUser.getPassword())) {
            showError("Current password is incorrect.");
            return;
        }

        if (!newPw.isEmpty()) {
            if (!newPw.equals(confirmPw)) {
                showError("New passwords do not match.");
                return;
            }
            String pwError = PasswordRecognizer.evaluatePassword(newPw);
            if (!pwError.isEmpty()) {
                showError(pwError);
                return;
            }
            currentUser.setPassword(newPw);
        }
        
     // Validate email
        if (!email.isEmpty()) {
        	if (!email.equals(oldEmail)) {
        		String emailError = EmailRecognizer.validate(email);
        		if (!emailError.isEmpty()) {
        			System.out.println(emailError);
        			errorLabel.setText(emailError);
        			return;
        		}
        		currentUser.setEmail(email);
        	}
        }
        
        
        // Update name/phone/bio
        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setBio(bio);
        
        // Update the user profile in the database
        try {
        	//Check if the username already exists uniqueness
        	 if (!newUserName.equals(oldUserName) && databaseHelper.doesUserExist(newUserName)) {
                 showError("This username is already taken. Please choose another.");
                 return;
             }
            
            // Update user in database
        	databaseHelper.updateUser(currentUser, oldUserName);
            databaseHelper.updateFullProfile(currentUser);
            var profileData = databaseHelper.getUserProfile(currentUser.getId());
            System.out.println("After update phone = " + profileData.get("phone") + ", bio = " + profileData.get("bio"));

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
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }
}