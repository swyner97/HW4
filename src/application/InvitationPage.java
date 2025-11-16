package application;

import java.sql.SQLException;
import databasePart1.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * InvitePage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */
public class InvitationPage {

    /**
     * Displays the Invite Page in the provided primary stage.
     *
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(DatabaseHelper databaseHelper, Stage primaryStage) {
        BorderPane borderPane = new BorderPane();

        NavigationBar navBar = new NavigationBar();
        borderPane.setTop(navBar);

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setMaxWidth(400);

        // Title label
        Label userLabel = new Label("Invite New User");
        userLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Role selection section
        Label roleLabel = new Label("Assign one or more roles:");
        roleLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10 0 5 0;");

        // Role checkboxes 
        VBox roleBox = new VBox(8);
        roleBox.setAlignment(Pos.CENTER_LEFT);
        roleBox.setPadding(new Insets(0, 0, 10, 20));
        
        CheckBox studentRole = new CheckBox("Student");
        CheckBox instructorRole = new CheckBox("Instructor");
        CheckBox reviewerRole = new CheckBox("Reviewer");
        CheckBox adminRole = new CheckBox("Admin");
        
        roleBox.getChildren().addAll(studentRole, instructorRole, reviewerRole, adminRole);

        // Button to generate the invitation code
        Button showCodeButton = new Button("Generate Invitation Code");
        showCodeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");

        // Label to display the generated invitation code
        Label inviteCodeLabel = new Label("");
        inviteCodeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                                 "-fx-text-fill: #2e7d32; -fx-padding: 10 0 0 0;");
        inviteCodeLabel.setWrapText(true);

        showCodeButton.setOnAction(a -> {
            // Generate the invitation code using the databaseHelper and set it to the label
            String invitationCode = databaseHelper.generateInvitationCode();
            checkedBox(studentRole, databaseHelper, "student", invitationCode);
            checkedBox(instructorRole, databaseHelper, "instructor", invitationCode);
            checkedBox(reviewerRole, databaseHelper, "reviewer", invitationCode);
            checkedBox(adminRole, databaseHelper, "admin", invitationCode);
            inviteCodeLabel.setText("Invitation Code: " + invitationCode);
            
            // Debugging
            try {
                System.out.println(databaseHelper.allCodeRoles(invitationCode));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        layout.getChildren().addAll(userLabel, roleLabel, roleBox, showCodeButton, inviteCodeLabel);
        
        BorderPane.setAlignment(layout, Pos.CENTER);
        borderPane.setCenter(layout);
        
        Scene inviteScene = new Scene(borderPane, 800, 400);

        primaryStage.setScene(inviteScene);
        primaryStage.setTitle("Invite Page");
    }

    /**
     * Method to prevent repeated checked box code
     */
    public void checkedBox(CheckBox checkBox, DatabaseHelper databaseHelper, String role, String invitationCode) {
        if (checkBox.isSelected()) {
            try {
                databaseHelper.addRoleVIACode(invitationCode, role);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}