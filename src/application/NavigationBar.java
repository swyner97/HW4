package application;

import javafx.scene.control.ToolBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.geometry.Orientation;
import javafx.scene.layout.Region;

import java.sql.SQLException;

import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class NavigationBar extends ToolBar {
    public NavigationBar() {
        // Set styling for the navigation bar
        this.setStyle("-fx-background-color: #2c3e50; -fx-padding: 5;");
        
        // Home/Welcome Button
        Button welcomePageButton = createStyledButton("🏠 Home");
        welcomePageButton.setOnAction(_ -> {
            new WelcomeLoginPage().show(StatusData.primaryStage, StatusData.currUser);
        });
        this.getItems().add(welcomePageButton);
        
        this.getItems().add(new Separator(Orientation.VERTICAL));
        
        // Search Section
        MenuButton searchMenu = createStyledMenuButton("🔍 Search");
        
        MenuItem searchGeneral = new MenuItem("General Search");
        searchGeneral.setOnAction(_ -> {
            UserQAMenu qaMenuPage = new UserQAMenu();
            qaMenuPage.start(StatusData.primaryStage);
        });
        
        MenuItem searchQs = new MenuItem("Search Questions");
        searchQs.setOnAction(_ -> {
            SearchPage searchQsPage = new SearchPage();
            searchQsPage.showSearchWindow();
        });

        MenuItem searchAs = new MenuItem("Search Answers");
        searchAs.setOnAction(_ -> {
            SearchAsPage searchAsPage = new SearchAsPage();
            searchAsPage.showSearchWindow();
        });

        MenuItem searchMyPosts = new MenuItem("My Posts");
        searchMyPosts.setOnAction(_ -> {
            MyPostsPage postsPage = new MyPostsPage();
            Stage postsStage = new Stage();
            postsPage.start(postsStage);
        });

        searchMenu.getItems().addAll(searchGeneral, searchQs, searchAs, searchMyPosts);
        this.getItems().add(searchMenu);
        
        this.getItems().add(new Separator(Orientation.VERTICAL));


        if (StatusData.currUser.getRole().equals("admin")) {
            Button adminHomePageButton = createStyledButton("⚙️ Admin Home");
            adminHomePageButton.setOnAction(_ -> {
                new AdminHomePage().show(StatusData.primaryStage);
            });
            this.getItems().add(adminHomePageButton);
            
            Button invitationPageButton = createStyledButton("✉️ Invitations");
            invitationPageButton.setOnAction(_ -> {
                new InvitationPage().show(StatusData.databaseHelper, StatusData.primaryStage);
            });
            this.getItems().add(invitationPageButton);
        }
        
        // Spacer to push remaining items to the right
        Region spacer = new Region();
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        spacer.setMaxWidth(Double.MAX_VALUE);
        this.getItems().add(spacer);
        
        User currentUser = StatusData.currUser;

        MenuButton userMenu = createStyledMenuButton(currentUser.getUserName());

        // Profile menu item
        MenuItem profilePageButton = new MenuItem("👤 Profile");
        profilePageButton.setOnAction(_ -> {
            new ProfilePage().show(StatusData.primaryStage, currentUser);
        });

        // My Q&A menu item
        MenuItem myQA = new MenuItem("💬 My Q&A");
        myQA.setOnAction(_ -> {
            MyQAPage myQAPage = new MyQAPage();
            myQAPage.show(StatusData.primaryStage, currentUser);
        });

        userMenu.getItems().addAll(profilePageButton, new SeparatorMenuItem(), myQA);

        this.getItems().add(userMenu);
        this.getItems().add(new Separator(Orientation.VERTICAL));

        // Logout Button (right side)
        Button logOutButton = createStyledButton("🚪 Logout");
        logOutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                             "-fx-background-radius: 3;");
        logOutButton.setOnMouseEntered(e -> 
            logOutButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                 "-fx-background-radius: 3;"));
        logOutButton.setOnMouseExited(e -> 
            logOutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                 "-fx-background-radius: 3;"));
        logOutButton.setOnAction(_ -> {
            try {
                StatusData.databaseHelper.closeConnection();
                StatusData.databaseHelper.connectToDatabase();
                StatusData.currUser = null;
            } catch (SQLException e) { }
            new SetupLoginSelectionPage(StatusData.databaseHelper).show(StatusData.primaryStage);
        });
        this.getItems().add(logOutButton);
    }
    
    // Helper method to create styled buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                       "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                       "-fx-background-radius: 3;");
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                           "-fx-background-radius: 3;"));
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                           "-fx-background-radius: 3;"));
        return button;
    }
    
    // Helper method to create styled menu buttons - NOW WITH WHITE TEXT
    private MenuButton createStyledMenuButton(String text) {
        MenuButton menuButton = new MenuButton(text);
        menuButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                           "-fx-background-radius: 3;");
        menuButton.setOnMouseEntered(e -> 
            menuButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                               "-fx-background-radius: 3;"));
        menuButton.setOnMouseExited(e -> 
            menuButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                               "-fx-background-radius: 3;"));
        return menuButton;
    }
}