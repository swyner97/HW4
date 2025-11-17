package model;

import java.sql.SQLException;
import java.util.List;
import javafx.scene.control.ToolBar;

import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import logic.*;
import pages.*;

import model.User.Role;

public class NavigationBar extends ToolBar {
    public NavigationBar() {
        // Set styling for the navigation bar
        this.setStyle("-fx-background-color: #2c3e50; -fx-padding: 5;");
        
        User currentUser = StatusData.currUser;
        
        // ==================== Main Navigation ====================
        
        // Home Button
        Button homeButton = createStyledButton("🏠 Home");
        homeButton.setOnAction(_ -> {
            new WelcomeLoginPage().show(StatusData.primaryStage, StatusData.currUser);
        });
        this.getItems().add(homeButton);
        
        this.getItems().add(new Separator(Orientation.VERTICAL));
        
        // Browse Menu (consolidated search + FAQ)
        MenuButton browseMenu = createStyledMenuButton("📚 Browse");
        
        MenuItem viewFAQ = new MenuItem("❓ View FAQ");
        viewFAQ.setOnAction(_ -> {
            FAQPage faqPage = new FAQPage();
            try {
                faqPage.show(StatusData.primaryStage, StatusData.currUser);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        
        MenuItem searchQs = new MenuItem("🔍 Search Questions");
        searchQs.setOnAction(_ -> {
            SearchPage searchQsPage = new SearchPage();
            searchQsPage.showSearchWindow();
        });

        MenuItem searchAs = new MenuItem("🔍 Search Answers");
        searchAs.setOnAction(_ -> {
            SearchAsPage searchAsPage = new SearchAsPage();
            searchAsPage.showSearchWindow();
        });
        
        MenuItem generalSearch = new MenuItem("🔍 Advanced Search");
        generalSearch.setOnAction(_ -> {
            UserQAMenu qaMenuPage = new UserQAMenu();
            qaMenuPage.start(StatusData.primaryStage);
        });

        browseMenu.getItems().addAll(viewFAQ, new SeparatorMenuItem(), 
                                     searchQs, searchAs, generalSearch);
        this.getItems().add(browseMenu);
        
        this.getItems().add(new Separator(Orientation.VERTICAL));
        
        // My Activity Menu
        MenuButton myActivityMenu = createStyledMenuButton("📝 My Activity");
        
        MenuItem myQA = new MenuItem("💬 My Q&A");
        myQA.setOnAction(_ -> {
            MyQAPage myQAPage = new MyQAPage();
            myQAPage.show(StatusData.primaryStage, currentUser);
        });
        
        MenuItem myPosts = new MenuItem("📄 My Posts");
        myPosts.setOnAction(_ -> {
            MyPostsPage postsPage = new MyPostsPage();
            Stage postsStage = new Stage();
            postsPage.start(postsStage);
        });
        
        MenuItem myMessages = new MenuItem("✉️ Messages");
        myMessages.setOnAction(_ -> {
            MessagingPage messagingPage = new MessagingPage();
            messagingPage.show(StatusData.primaryStage);
        });
        
        MenuItem myReviews = new MenuItem("⭐ My Reviews");
        myReviews.setOnAction(_ -> {
            ReviewPage myReviewsPage = new ReviewPage(this);
            myReviewsPage.show(StatusData.primaryStage, currentUser);
        });
        
        MenuItem trustedReviewers = new MenuItem("👥 Trusted Reviewers");
        trustedReviewers.setOnAction(_ -> {
            TrustedReviewersPage trustedPage = new TrustedReviewersPage();
            trustedPage.show(StatusData.primaryStage);
        });
        
        myActivityMenu.getItems().addAll(myQA, myPosts, new SeparatorMenuItem(), 
                                        myMessages, new SeparatorMenuItem(),
                                        myReviews, trustedReviewers);
        this.getItems().add(myActivityMenu);
        
        this.getItems().add(new Separator(Orientation.VERTICAL));
        
        // ==================== ROLE-SPECIFIC TOOLS ====================
        
        // Staff Tools
        if (currentUser.getRole() == User.Role.STAFF || currentUser.getRole() == User.Role.ADMIN) {
            MenuButton staffMenu = createStyledMenuButton("🛠️ Staff Tools");
            staffMenu.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                              "-fx-background-radius: 3;");
            staffMenu.setOnMouseEntered(e -> 
                staffMenu.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; " +
                                  "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                  "-fx-background-radius: 3;"));
            staffMenu.setOnMouseExited(e -> 
                staffMenu.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; " +
                                  "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                  "-fx-background-radius: 3;"));
            
            MenuItem manageFAQs = new MenuItem("❓ Manage FAQs");
            manageFAQs.setOnAction(_ -> {
                StaffFAQMgmtPage faqMgmt = new StaffFAQMgmtPage();
                faqMgmt.show(StatusData.primaryStage, StatusData.currUser);
            });
            
            MenuItem manageAnnouncements = new MenuItem("📢 Manage Announcements");
            manageAnnouncements.setOnAction(_ -> {
                StaffAnnouncementPage announcePage = new StaffAnnouncementPage();
                announcePage.show(StatusData.primaryStage, StatusData.currUser);
            });
            
            MenuItem viewAnnouncements = new MenuItem("📋 View All Announcements");
            viewAnnouncements.setOnAction(_ -> {
                AnnouncementsPage announcementsPage = new AnnouncementsPage();
                announcementsPage.show(StatusData.primaryStage, StatusData.currUser);
            });
            
            staffMenu.getItems().addAll(manageFAQs, manageAnnouncements, 
                                       new SeparatorMenuItem(), viewAnnouncements);
            this.getItems().add(staffMenu);
            this.getItems().add(new Separator(Orientation.VERTICAL));
        }
        
        // Instructor Tools
        if (currentUser.getRole() == User.Role.INSTRUCTOR) {
            MenuButton instructorMenu = createStyledMenuButton("👨‍🏫 Instructor");
            instructorMenu.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                                   "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                   "-fx-background-radius: 3;");
            instructorMenu.setOnMouseEntered(e -> 
                instructorMenu.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                       "-fx-background-radius: 3;"));
            instructorMenu.setOnMouseExited(e -> 
                instructorMenu.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                       "-fx-background-radius: 3;"));
            
            MenuItem instructorHome = new MenuItem("🏠 Instructor Home");
            instructorHome.setOnAction(_ -> {
                new InstructorHomePage().show(StatusData.primaryStage);
            });
            
            instructorMenu.getItems().add(instructorHome);
            this.getItems().add(instructorMenu);
            this.getItems().add(new Separator(Orientation.VERTICAL));
        }
        
        // Admin Tools
        if (currentUser.getRole() == User.Role.ADMIN) {
            MenuButton adminMenu = createStyledMenuButton("⚙️ Admin");
            adminMenu.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                              "-fx-background-radius: 3;");
            adminMenu.setOnMouseEntered(e -> 
                adminMenu.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                  "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                  "-fx-background-radius: 3;"));
            adminMenu.setOnMouseExited(e -> 
                adminMenu.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                                  "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                  "-fx-background-radius: 3;"));
            
            MenuItem adminHome = new MenuItem("🏠 Admin Home");
            adminHome.setOnAction(_ -> {
                new AdminHomePage().show(StatusData.primaryStage);
            });
            
            MenuItem invitations = new MenuItem("✉️ Manage Invitations");
            invitations.setOnAction(_ -> {
                new InvitationPage().show(StatusData.databaseHelper, StatusData.primaryStage);
            });
            
            adminMenu.getItems().addAll(adminHome, invitations);
            this.getItems().add(adminMenu);
            this.getItems().add(new Separator(Orientation.VERTICAL));
        }
        
        // Request Reviewer Access (for students)
        try {
            List<String> userRoles = StatusData.databaseHelper.allUserRoles(currentUser.getUserName());
            if (!userRoles.contains(Role.REVIEWER.toString()) && 
                !userRoles.contains(Role.INSTRUCTOR.toString()) &&
                !userRoles.contains(Role.STAFF.toString()) &&
                !userRoles.contains(Role.ADMIN.toString())) {
                
                Button requestButton = createStyledButton("✨ Become a Reviewer");
                requestButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                        "-fx-background-radius: 3;");
                requestButton.setOnMouseEntered(e -> 
                    requestButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                                         "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                         "-fx-background-radius: 3;"));
                requestButton.setOnMouseExited(e -> 
                    requestButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                            "-fx-background-radius: 3;"));
                
                requestButton.setOnAction(e -> {
                    try {
                        StatusData.databaseHelper.reviewerRequest(currentUser.getUserName());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Request Sent");
                        alert.setHeaderText(null);
                        alert.setContentText("Your request to become a reviewer has been sent to the instructors!");
                        alert.showAndWait();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to send request. Please try again.");
                        alert.showAndWait();
                    }
                });
                
                this.getItems().add(requestButton);
                this.getItems().add(new Separator(Orientation.VERTICAL));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // ==================== User & System ====================
        
        // Spacer to push right-side items to the edge
        Region spacer = new Region();
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        spacer.setMaxWidth(Double.MAX_VALUE);
        this.getItems().add(spacer);
        
        // Announcements notification (if any unread)
        try {
            List<Announcement> unreadAnnouncements = 
                StatusData.databaseHelper.getActiveAnnouncementsForUser(currentUser.getId());
            
            if (!unreadAnnouncements.isEmpty()) {
                Button announcementNotif = new Button("📢 " + unreadAnnouncements.size());
                announcementNotif.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; " +
                                          "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                                          "-fx-background-radius: 15;");
                announcementNotif.setTooltip(new Tooltip("You have " + unreadAnnouncements.size() + " new announcement(s)"));
                announcementNotif.setOnAction(_ -> {
                    AnnouncementPopup.showIfNeeded(StatusData.primaryStage, currentUser);
                });
                this.getItems().add(announcementNotif);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // User Profile Menu
        MenuButton userMenu = createStyledMenuButton("👤 " + currentUser.getUserName());
        
        MenuItem profile = new MenuItem("👤 My Profile");
        profile.setOnAction(_ -> {
            new ProfilePage().show(StatusData.primaryStage, currentUser);
        });
        
        MenuItem viewAnnouncements = new MenuItem("📢 View Announcements");
        viewAnnouncements.setOnAction(_ -> {
            AnnouncementsPage announcementsPage = new AnnouncementsPage();
            announcementsPage.show(StatusData.primaryStage, currentUser);
        });
        
        MenuItem logout = new MenuItem("🚪 Logout");
        logout.setOnAction(_ -> {
            try {
                StatusData.databaseHelper.closeConnection();
                StatusData.databaseHelper.connectToDatabase();
                StatusData.currUser = null;
            } catch (SQLException e) { }
            new InitialAccessPage(StatusData.databaseHelper).show(StatusData.primaryStage);
        });
        
        userMenu.getItems().addAll(profile, new SeparatorMenuItem(), 
                                   viewAnnouncements, new SeparatorMenuItem(), 
                                   logout);
        this.getItems().add(userMenu);
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
    
    // Helper method to create styled menu buttons
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