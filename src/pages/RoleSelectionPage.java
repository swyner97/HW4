package pages;

import java.sql.SQLException;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.StatusData;
import model.User;
import model.User.Role;

/**
 * RoleSelectionPage lets users choose which role to act as if they have multiple.
 * Once selected, it creates a User object with that role and opens the WelcomeLoginPage.
 */
public class RoleSelectionPage {
    private final String userName;
    private final String password;

    public RoleSelectionPage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 40;");

        Label prompt = new Label("Please choose the role you wish to act as:");
        prompt.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.getChildren().add(prompt);

        try {
            List<String> roleStrings = StatusData.databaseHelper.allUserRoles(userName);

            // Remove the default "user" role if it exists
            roleStrings.removeIf(role -> role.equalsIgnoreCase("user"));

            if (roleStrings.isEmpty()) {
                Label noRoles = new Label("No roles found for this user.");
                noRoles.setStyle("-fx-text-fill: red;");
                layout.getChildren().add(noRoles);
            }

            for (String roleStr : roleStrings) {
                // Convert string to Role enum
                User.Role role = User.Role.fromString(roleStr);
                
                // Skip UNKNOWN roles
                if (role == User.Role.UNKNOWN) {
                    continue;
                }
                
                String displayName = roleStr.substring(0, 1).toUpperCase() + roleStr.substring(1).toLowerCase();
                Button roleButton = new Button(displayName);
                roleButton.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-background-color: #2196f3; -fx-text-fill: white; " +
                    "-fx-padding: 8 20; -fx-cursor: hand;"
                );

                roleButton.setOnAction(_ -> {
                    try {
                        User dbUser = StatusData.databaseHelper.getUserByName(userName);
                        if (dbUser == null) {
                            System.err.println("RoleSelectionPage: could not load user " + userName);
                            return;
                        }

                        // Set role using the enum (not string)
                        dbUser.setRole(role);
                        StatusData.currUser = dbUser;

                        // Debug print to verify the selected role
                        System.out.println("Selected role: " + StatusData.currUser.getRoleName() + ", id=" + StatusData.currUser.getId());

                        new WelcomeLoginPage().show(primaryStage, StatusData.currUser);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                layout.getChildren().add(roleButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading roles: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            layout.getChildren().add(errorLabel);
        }

        Scene roleScene = new Scene(layout, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        primaryStage.setScene(roleScene);
        primaryStage.setTitle("Role Selection");
        primaryStage.show();
    }
}