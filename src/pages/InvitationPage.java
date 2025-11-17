package pages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import databasePart1.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.*;
import model.*;
import model.User.Role;
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
        CheckBox staffRole = new CheckBox("Staff");
        
        roleBox.getChildren().addAll(studentRole, instructorRole, reviewerRole, adminRole, staffRole);

        // Button to generate the invitation code
        Button showCodeButton = new Button("Generate Invitation Code");
        showCodeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");

        // Label to display the generated invitation code
        Label inviteCodeLabel = new Label("");
        inviteCodeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                                 "-fx-text-fill: #2e7d32; -fx-padding: 10 0 0 0;");
        inviteCodeLabel.setWrapText(true);

        showCodeButton.setOnAction(a -> {
            // collect selected roles
            List<Role> selectedRoles = new ArrayList<>();
            if (studentRole.isSelected()) selectedRoles.add(Role.STUDENT);
            if (instructorRole.isSelected()) selectedRoles.add(Role.INSTRUCTOR);
            if (reviewerRole.isSelected()) selectedRoles.add(Role.REVIEWER);
            if (adminRole.isSelected()) selectedRoles.add(Role.ADMIN);
            if (staffRole.isSelected()) selectedRoles.add(Role.STAFF);

            if (selectedRoles.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one role to assign to the invitation code.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            // Generate invitation code (databaseHelper should ensure uniqueness)
            String invitationCode = databaseHelper.generateInvitationCode();

            // persist roles for the code
            for (Role role : selectedRoles) {
                try {
                    databaseHelper.addRoleVIACode(invitationCode, role);
                } catch (SQLException e) {
                    e.printStackTrace();
                    // If error, show user-friendly alert and stop
                    Alert err = new Alert(Alert.AlertType.ERROR, "Failed to save role " + role + " for code " + invitationCode);
                    err.showAndWait();
                    return;
                }
            }

            inviteCodeLabel.setText("Invitation Code: " + invitationCode);

            // Add copy-to-clipboard button (inline)
            Button copyBtn = new Button("Copy Code");
            copyBtn.setOnAction(ev -> {
                final javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
                final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(invitationCode);
                cb.setContent(content);
            });

            // show roles assigned (nice UX)
            Label assignedRolesLabel = new Label("Assigned roles: " + selectedRoles.stream().map(Enum::name).collect(Collectors.joining(", ")));
            assignedRolesLabel.setWrapText(true);

            // add to layout (remove previous copy/role labels first)
            layout.getChildren().removeIf(node -> node.getId() != null && (node.getId().equals("inviteCopy") || node.getId().equals("assignedRoles")));
            copyBtn.setId("inviteCopy");
            assignedRolesLabel.setId("assignedRoles");
            layout.getChildren().addAll(assignedRolesLabel, copyBtn);

            // Debugging: print to console the roles saved
            try {
                System.out.println("Code roles from DB: " + databaseHelper.allCodeRoles(invitationCode));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });


        layout.getChildren().addAll(userLabel, roleLabel, roleBox, showCodeButton, inviteCodeLabel);
        
        BorderPane.setAlignment(layout, Pos.CENTER);
        borderPane.setCenter(layout);
        
        Scene inviteScene = new Scene(borderPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);

        primaryStage.setScene(inviteScene);
        primaryStage.setTitle("Invite Page");
    }

    /**
     * Method to prevent repeated checked box code
     */
    public void checkedBox(CheckBox checkBox, DatabaseHelper databaseHelper, Role role, String invitationCode) {
        if (checkBox.isSelected()) {
            try {
                databaseHelper.addRoleVIACode(invitationCode, role);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}