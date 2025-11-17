package pages;

import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.StatusData;
import model.NavigationBar;
import model.User;

/**
 * AdminHomePage represents the admin interface.
 * It allows admins to manage user accounts, reset passwords,
 * and modify user roles (Admin, Student, Reviewer, Instructor, Staff).
 *
 * NOTE: Students are now allowed to make edits in the system —
 * so "Student" is no longer a restricted or limited role.
 */
@SuppressWarnings("unchecked")
public class AdminHomePage {

    private final DatabaseHelper databaseHelper;
    private final User currentUser;

    public AdminHomePage() {
        this(StatusData.databaseHelper, StatusData.currUser);
    }

    public AdminHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        show(primaryStage, currentUser.getUserName());
    }

    public void show(Stage primaryStage, String loggedInAdminUserName) {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(new NavigationBar());

        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button updateAccountButton = new Button("Update Account");
        updateAccountButton.setOnAction(e ->
            new UpdateAccountPage(databaseHelper, currentUser).show(primaryStage)
        );

        // 🔹 OTP reset section
        Label resetLabel = new Label("Reset password for user:");
        resetLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-font-style: italic;");

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter username");
        Button otpButton = new Button("Generate One-Time Password");
        otpButton.setOnAction(e -> {
            String userName = userNameField.getText().trim();
            if (userName.isEmpty()) {
                resultLabel.setText("⚠️ Please enter a username.");
                return;
            }

            String otp = databaseHelper.generatePassword(userName);
            if (otp != null) {
                resultLabel.setText("✅ OTP for " + userName + ": " + otp);
            } else {
                resultLabel.setText("❌ Error: user not found or update failed.");
            }
        });

        VBox otpSection = new VBox(5, resetLabel, userNameField, otpButton, resultLabel);

        // 🔹 User Table setup
        TableView<User> userTable = new TableView<>();
        userTable.setEditable(true);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> userNameCol = new TableColumn<>("Username");
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userNameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    User user = getTableView().getItems().get(getIndex());
                    // Highlight users needing password reset
                    if ("PENDING".equalsIgnoreCase(user.getTempPw())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> pwCol = new TableColumn<>("Password");
        pwCol.setCellValueFactory(new PropertyValueFactory<>("password"));

        TableColumn<User, String> roleCol = new TableColumn<>("Current Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Show all roles a user currently has
        TableColumn<User, String> allRolesCol = new TableColumn<>("All Roles");
        allRolesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            List<String> roles = databaseHelper.allUserRoles(user.getUserName());
                            setText(String.join(", ", roles));
                        } catch (SQLException e) {
                            setText("Error loading roles");
                        }
                    }
                }
            }
        });
        
        // ✅ Allow admin to assign or remove any roles, including student
        TableColumn<User, User.Role> addRolesCol = new TableColumn<>("Add Role");
        addRolesCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        ObservableList<User.Role> rolesList = FXCollections.observableArrayList(
                User.Role.ADMIN,
                User.Role.STUDENT,
                User.Role.REVIEWER,
                User.Role.INSTRUCTOR,
                User.Role.STAFF
        );
        addRolesCol.setCellFactory(ComboBoxTableCell.forTableColumn(rolesList));
        addRolesCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            User.Role newRole = event.getNewValue();
            try {
            	  databaseHelper.addUserRoles(user.getUserName(), user.getRole());
                System.out.println("Added role '" + newRole + "' for user: " + user.getUserName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        TableColumn<User, User.Role> delRolesCol = new TableColumn<>("Delete Role");
        delRolesCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        delRolesCol.setCellFactory(ComboBoxTableCell.forTableColumn(rolesList));
        delRolesCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            User.Role roleToDelete = event.getNewValue();
            try {
                databaseHelper.deleteUserRole(user.getUserName(), user.getRole());
                System.out.println("Removed role '" + roleToDelete + "' for user: " + user.getUserName());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        userTable.getColumns().addAll(
            idCol, userNameCol, emailCol, pwCol, roleCol, allRolesCol, addRolesCol, delRolesCol
        );

        try {
            ObservableList<User> users = databaseHelper.getAllUsers();
            userTable.setItems(users);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        layout.getChildren().addAll(adminLabel, updateAccountButton, otpSection, userTable);
        borderPane.setCenter(layout);

        Scene adminScene = new Scene(borderPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
        primaryStage.show();
    }
}
