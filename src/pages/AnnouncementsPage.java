package pages;
import javafx.stage.Stage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import logic.*;
import model.*;

public class AnnouncementsPage {
    private Stage stage;

    public void show(Stage stage, model.User user) {
        this.stage = stage;
        stage.setTitle("Announcements");

        BorderPane main = new BorderPane();

        // navigation bar (keeps your existing site-wide nav)
        NavigationBar nav = new NavigationBar();
        main.setTop(nav);

        // light page background to make white cards readable
        VBox pageContainer = new VBox();
        pageContainer.setStyle("-fx-background-color: #F5F7FA;");
        pageContainer.setPadding(new Insets(20));

        VBox content = new VBox(14);
        content.setPadding(new Insets(12));
        content.setMaxWidth(980);
        content.setStyle("-fx-alignment: top-left;");

        Label heading = new Label("Announcements");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: 600; -fx-text-fill: #222;");
        content.getChildren().add(heading);

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 8, 0));
        content.getChildren().add(sep);

        // Load announcements (you can switch to getActiveAnnouncements() if desired)
        List<Announcement> announcements = StatusData.databaseHelper.getAllAnnouncements();

        if (announcements == null || announcements.isEmpty()) {
            VBox noneBox = new VBox(8);
            noneBox.setAlignment(Pos.CENTER);
            noneBox.setPadding(new Insets(48, 0, 48, 0));

            Label none = new Label("There are no announcements at this time.");
            none.setStyle("-fx-font-size: 15px; -fx-text-fill: #666;");

            noneBox.getChildren().add(none);
            content.getChildren().add(noneBox);
        } else {
            // container to center and limit width of cards
            VBox cardsContainer = new VBox(12);
            cardsContainer.setPadding(new Insets(4, 0, 20, 0));

            for (Announcement a : announcements) {
                VBox card = new VBox(8);
                card.setPadding(new Insets(14));
                card.setMaxWidth(820);
                card.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-radius: 10;" +
                    "-fx-border-color: transparent;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
                );

                // Title + priority badge
                HBox titleRow = new HBox(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);

                Label title = new Label(a.getTitle());
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1f2937;");
                title.setWrapText(true);

                Label badge = new Label(a.getPriority().getDisplayName());
                // use slightly muted variant of priority color for badge background (safer contrast)
                String priorityColor = safeBadgeColor(a.getPriority().getColor());
                badge.setStyle("-fx-background-color: " + priorityColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 6;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                titleRow.getChildren().addAll(title, spacer, badge);

                // Meta line: date and display type
                String dateStr = a.getStartDate() != null ? formatDate(a.getStartDate()) : "";
                Label meta = new Label((a.getDisplayType() != null ? a.getDisplayType().getDisplayName() + " • " : "") + dateStr);
                meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

                // Body as wrapped label (clean, no focus)
                Label body = new Label(a.getContent() == null ? "" : a.getContent());
                body.setWrapText(true);
                body.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
                body.setMaxWidth(780);

                card.getChildren().addAll(titleRow, meta, body);
                cardsContainer.getChildren().add(card);
            }

            content.getChildren().add(cardsContainer);
        }

        // center content and limit width for better reading on wide displays
        HBox centerWrap = new HBox();
        centerWrap.setAlignment(Pos.TOP_CENTER);
        centerWrap.getChildren().add(content);
        pageContainer.getChildren().add(centerWrap);

        ScrollPane sp = new ScrollPane(pageContainer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        main.setCenter(sp);

        Scene scene = new Scene(main, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    private String formatDate(String iso) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(iso);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            return ldt.format(fmt);
        } catch (Exception e) {
            return iso != null ? iso : "";
        }
    }

    /**
     * Adjust badge color to ensure readable contrast on small pill.
     * If the provided color is very light, fall back to a darker tint.
     */
    private String safeBadgeColor(String colorHex) {
        if (colorHex == null || colorHex.isEmpty()) return "#6b7280"; // neutral gray
        try {
            // normalize hex and compute luminance
            String hc = colorHex.trim();
            if (hc.startsWith("#")) hc = hc.substring(1);
            if (hc.length() == 3) {
                hc = "" + hc.charAt(0) + hc.charAt(0) + hc.charAt(1) + hc.charAt(1) + hc.charAt(2) + hc.charAt(2);
            }
            int r = Integer.parseInt(hc.substring(0, 2), 16);
            int g = Integer.parseInt(hc.substring(2, 4), 16);
            int b = Integer.parseInt(hc.substring(4, 6), 16);
            double luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
            // if too light, darken; otherwise keep original
            if (luminance > 0.7) {
                return "#374151"; // fallback dark gray
            } else {
                return "#" + hc;
            }
        } catch (Exception ex) {
            return "#6b7280";
        }
    }
}
