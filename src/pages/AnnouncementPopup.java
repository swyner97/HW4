package pages;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import logic.StatusData;
import model.Announcement;
import model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Announcement Popup with dynamic per-card dismissal (fade out + remove)
 */
public class AnnouncementPopup {
	
	// add inside AnnouncementPopup class (private static helper)
	private static Button styledButton(String text, String color) {
	    Button btn = new Button(text);
	    String baseStyle = "-fx-background-color: " + color + "; " +
	                       "-fx-text-fill: white; " +
	                       "-fx-font-weight: bold; " +
	                       "-fx-background-radius: 6; " +
	                       "-fx-padding: 6 14;";
	    btn.setStyle(baseStyle);

	    // simple hover effect (slightly reduce opacity on hover)
	    btn.setOnMouseEntered(e -> btn.setStyle(
	        "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 14; -fx-opacity: 0.90;"
	    ));
	    btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

	    return btn;
	}


    public static void showIfNeeded(Stage ownerStage, User user) {
        List<Announcement> unreadAnnouncements;
        try {
            unreadAnnouncements = StatusData.databaseHelper.getActiveAnnouncementsForUser(user.getId());
            if (unreadAnnouncements == null) unreadAnnouncements = new ArrayList<>();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        if (unreadAnnouncements.isEmpty()) return;

        // make final for lambda capture
        final List<Announcement> announcementsToShow = unreadAnnouncements;
        
        

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(ownerStage);
        popup.setTitle("System Announcements");

        BorderPane mainPane = new BorderPane();

        // Header
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #2C3E50; -fx-padding: 16; -fx-border-radius: 6 6 0 0;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label headerLabel = new Label("📢 " + announcementsToShow.size() + " New Announcement(s)");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        header.getChildren().add(headerLabel);
        mainPane.setTop(header);

        // Content area & container for cards
        VBox contentRoot = new VBox();
        contentRoot.setStyle("-fx-background-color: #F8F9FA;");
        contentRoot.setPadding(new Insets(12));

        VBox cardsContainer = new VBox(12);
        cardsContainer.setPadding(new Insets(16));
        cardsContainer.setFillWidth(true);

        // Populate cards; capture cardsContainer as final for handler closures
        for (Announcement a : announcementsToShow) {
            VBox card = createCard(a, user, popup, cardsContainer);
            cardsContainer.getChildren().add(card);
        }

        contentRoot.getChildren().add(cardsContainer);

        ScrollPane scroll = new ScrollPane(contentRoot);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        mainPane.setCenter(scroll);

        // Footer
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(12));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #F0F0F0; -fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        Button viewAllBtn = styledButton("View All", "#6A1B9A");
        Button markAllBtn = styledButton("Mark All Read", "#388E3C");
        Button closeBtn = styledButton("Close", "#757575");

        viewAllBtn.setOnAction(e -> {
            popup.close();
            new AnnouncementsPage().show(ownerStage, user);
        });

        markAllBtn.setOnAction(e -> {
            // mark all SHOW_ONCE as read and fade out each card
            List<javafx.scene.Node> nodes = new ArrayList<>(cardsContainer.getChildren());
            for (javafx.scene.Node node : nodes) {
                if (!(node instanceof VBox)) continue;
                // find announcement id stored in userData
                Object ud = node.getUserData();
                if (ud instanceof Integer) {
                    int annId = (Integer) ud;
                    // find corresponding announcement object (optional)
                    try {
                        StatusData.databaseHelper.markAnnouncementAsRead(annId, user.getId());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                fadeAndRemove(node, cardsContainer, popup);
            }
        });

        closeBtn.setOnAction(e -> popup.close());

        footer.getChildren().addAll(viewAllBtn, markAllBtn, closeBtn);
        mainPane.setBottom(footer);

        Scene scene = new Scene(mainPane, 640, 520);
        popup.setScene(scene);
        popup.showAndWait();
    }

    /**
     * Create a single announcement card. The returned VBox's userData is set to the announcementId
     * so handlers can identify which announcement to mark as read.
     */
    private static VBox createCard(Announcement announcement, User user, Stage popupStage, VBox container) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white;" +
                      "-fx-background-radius: 8;" +
                      "-fx-border-color: #E6E6E6;" +
                      "-fx-border-radius: 8;" +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");
        // store announcement id for handlers
        card.setUserData(announcement.getAnnouncementId());

        // Title row: title + priority badge + spacer + dismiss button
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(announcement.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222;");
        title.setWrapText(true);

        Label badge = new Label(announcement.getPriority() != null ? announcement.getPriority().getDisplayName() : "Info");
        badge.setStyle("-fx-background-color: " + safeBadgeColor(announcement.getPriority() != null ? announcement.getPriority().getColor() : "#6b7280") +
                       "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button dismissBtn = new Button("Mark Read");
        dismissBtn.setStyle("-fx-background-color: #00796B; -fx-text-fill: white; -fx-background-radius: 6;");
        // disable if not SHOW_ONCE
        if (announcement.getDisplayType() != Announcement.DisplayType.SHOW_ONCE) {
            dismissBtn.setDisable(true);
            dismissBtn.setOpacity(0.7);
            dismissBtn.setTooltip(new Tooltip("This announcement will be shown until it expires."));
        } else {
            dismissBtn.setOnAction(e -> {
                // mark rendered announcement as read in DB, then fade + remove card
                try {
                    StatusData.databaseHelper.markAnnouncementAsRead(announcement.getAnnouncementId(), user.getId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                fadeAndRemove(card, container, popupStage);
            });
        }

        titleRow.getChildren().addAll(title, spacer, badge, dismissBtn);

        Label meta = new Label((announcement.getDisplayType() != null ? announcement.getDisplayType().getDisplayName() + " • " : "") + formatDate(announcement.getStartDate()));
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Label body = new Label(announcement.getContent() != null ? announcement.getContent() : "");
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        body.setMaxWidth(560);

        card.getChildren().addAll(titleRow, meta, body);
        return card;
    }

    /**
     * Fade out a node, then remove it from container. If container becomes empty, close popupStage.
     */
    private static void fadeAndRemove(javafx.scene.Node node, VBox container, Stage popupStage) {
        FadeTransition ft = new FadeTransition(Duration.millis(320), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(ev -> {
            Platform.runLater(() -> {
                container.getChildren().remove(node);
                if (container.getChildren().isEmpty()) {
                    // Close the popup if no more announcements remain
                    popupStage.close();
                }
            });
        });
        ft.play();
    }

    private static String formatDate(String isoDate) {
        if (isoDate == null) return "";
        try {
            LocalDateTime ldt = LocalDateTime.parse(isoDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            return ldt.format(formatter);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private static String safeBadgeColor(String colorHex) {
        if (colorHex == null || colorHex.isEmpty()) return "#6b7280";
        try {
            String hc = colorHex.trim();
            if (hc.startsWith("#")) hc = hc.substring(1);
            if (hc.length() == 3) {
                hc = "" + hc.charAt(0) + hc.charAt(0) + hc.charAt(1) + hc.charAt(1) + hc.charAt(2) + hc.charAt(2);
            }
            int r = Integer.parseInt(hc.substring(0, 2), 16);
            int g = Integer.parseInt(hc.substring(2, 4), 16);
            int b = Integer.parseInt(hc.substring(4, 6), 16);
            double luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
            if (luminance > 0.7) {
                return "#374151";
            } else {
                return "#" + hc;
            }
        } catch (Exception ex) {
            return "#6b7280";
        }
    }
}
