package pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.StatusData;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Staff Announcement Management Page
 * Allows staff to create, edit, and delete announcements
 */
public class StaffAnnouncementPage {
    private Stage stage;
    private User user;
    private TableView<Announcement> announcementTable;

    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;

        stage.setTitle("Announcement Management");

        BorderPane mainPane = new BorderPane();

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("Announcement Management");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subHeader = new Label("Create and manage system-wide announcements");
        subHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        VBox header = new VBox(5, headerLabel, subHeader);
        header.setAlignment(Pos.CENTER_LEFT);

        // Create Announcement Button
        Button createBtn = new Button("+ Create New Announcement");
        createBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                          "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");
        createBtn.setOnAction(e -> showCreateAnnouncementDialog());

        HBox createBox = new HBox(createBtn);
        createBox.setAlignment(Pos.CENTER_RIGHT);

        // Table
        announcementTable = new TableView<>();
        announcementTable.setPrefHeight(400);

        TableColumn<Announcement, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("announcementId"));
        idCol.setPrefWidth(50);

        TableColumn<Announcement, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        TableColumn<Announcement, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getPriority().getDisplayName()
            ));
        priorityCol.setPrefWidth(100);
        
        // Style priority column with colors
        priorityCol.setCellFactory(column -> new TableCell<Announcement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setGraphic(null);
                
                if (!empty && getTableRow() != null) {
                    Announcement announcement = getTableView().getItems().get(getIndex());
                    if (announcement != null) {
                        String color = announcement.getPriority().getColor();
                        setStyle("-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<Announcement, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue().isActive();
            return new javafx.beans.property.SimpleStringProperty(
                active ? "Active" : "Inactive"
            );
        });
        statusCol.setPrefWidth(80);

        TableColumn<Announcement, String> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getStartDate();
            return new javafx.beans.property.SimpleStringProperty(
                date != null ? formatDate(date) : "N/A"
            );
        });
        startCol.setPrefWidth(120);

        TableColumn<Announcement, String> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getEndDate();
            return new javafx.beans.property.SimpleStringProperty(
                date != null ? formatDate(date) : "Never"
            );
        });
        endCol.setPrefWidth(120);

        TableColumn<Announcement, String> typeCol = new TableColumn<>("Display Type");
        typeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDisplayType().getDisplayName()
            ));
        typeCol.setPrefWidth(150);

        // Actions column
        TableColumn<Announcement, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actionBox = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Announcement announcement = getTableView().getItems().get(getIndex());
                    showEditAnnouncementDialog(announcement);
                });

                deleteBtn.setOnAction(event -> {
                    Announcement announcement = getTableView().getItems().get(getIndex());
                    deleteAnnouncement(announcement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        announcementTable.getColumns().addAll(
            idCol, titleCol, priorityCol, statusCol, startCol, endCol, typeCol, actionsCol
        );

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> loadAnnouncements());

        Button viewAllBtn = new Button("View All Announcements (User View)");
        viewAllBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-weight: bold;");
        viewAllBtn.setOnAction(e -> {
            AnnouncementsPage announcementsPage = new AnnouncementsPage();
            announcementsPage.show(stage, user);
        });

        buttonBox.getChildren().addAll(refreshBtn, viewAllBtn);

        content.getChildren().addAll(header, createBox, announcementTable, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        mainPane.setCenter(scrollPane);

        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();

        loadAnnouncements();
    }

    private void loadAnnouncements() {
        List<Announcement> announcements = StatusData.databaseHelper.getAllAnnouncements();
        announcementTable.getItems().setAll(announcements);
    }

    private void showCreateAnnouncementDialog() {
        showAnnouncementDialog(null);
    }

    private void showEditAnnouncementDialog(Announcement announcement) {
        showAnnouncementDialog(announcement);
    }

    private void showAnnouncementDialog(Announcement existingAnnouncement) {
        boolean isEdit = (existingAnnouncement != null);
        
        Stage dialog = new Stage();
        dialog.setTitle(isEdit ? "Edit Announcement" : "Create New Announcement");
        dialog.initOwner(stage);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label(isEdit ? "Edit Announcement" : "Create New Announcement");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Title field
        Label announcementTitleLabel = new Label("Title:*");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter announcement title...");
        titleField.setPrefWidth(450);
        if (isEdit) titleField.setText(existingAnnouncement.getTitle());

        // Content area
        Label contentLabel = new Label("Content:*");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter announcement content...");
        contentArea.setPrefRowCount(6);
        contentArea.setWrapText(true);
        contentArea.setPrefWidth(450);
        if (isEdit) contentArea.setText(existingAnnouncement.getContent());

        // Priority selection
        Label priorityLabel = new Label("Priority:*");
        ComboBox<Announcement.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Announcement.Priority.values());
        priorityCombo.setValue(isEdit ? existingAnnouncement.getPriority() : Announcement.Priority.NORMAL);
        priorityCombo.setPrefWidth(200);
        
        // Custom cell factory to show colors
        priorityCombo.setCellFactory(param -> new ListCell<Announcement.Priority>() {
            @Override
            protected void updateItem(Announcement.Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-background-color: " + item.getColor() + "; -fx-text-fill: white;");
                }
            }
        });

        // Start date
        Label startDateLabel = new Label("Start Date:*");
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now());
        startDatePicker.setPrefWidth(200);
        if (isEdit && existingAnnouncement.getStartDate() != null) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(existingAnnouncement.getStartDate());
                startDatePicker.setValue(ldt.toLocalDate());
            } catch (Exception e) {
                // Use default
            }
        }

        // End date
        Label endDateLabel = new Label("End Date (optional - leave blank for no expiration):");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("No expiration");
        endDatePicker.setPrefWidth(200);
        if (isEdit && existingAnnouncement.getEndDate() != null) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(existingAnnouncement.getEndDate());
                endDatePicker.setValue(ldt.toLocalDate());
            } catch (Exception e) {
                // Leave empty
            }
        }

        // Display type
        Label displayTypeLabel = new Label("Display Type:*");
        ComboBox<Announcement.DisplayType> displayTypeCombo = new ComboBox<>();
        displayTypeCombo.getItems().addAll(Announcement.DisplayType.values());
        displayTypeCombo.setValue(isEdit ? existingAnnouncement.getDisplayType() : Announcement.DisplayType.SHOW_ONCE);
        displayTypeCombo.setPrefWidth(300);
        
        displayTypeCombo.setCellFactory(param -> new ListCell<Announcement.DisplayType>() {
            @Override
            protected void updateItem(Announcement.DisplayType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        displayTypeCombo.setButtonCell(new ListCell<Announcement.DisplayType>() {
            @Override
            protected void updateItem(Announcement.DisplayType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        // Help text
        Label helpLabel = new Label("* Show Once: Users see it once, then it's dismissed\n" +
                                   "* Show Always: Users see it every time they log in until it expires");
        helpLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-style: italic;");
        helpLabel.setWrapText(true);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        Button saveBtn = new Button(isEdit ? "Save Changes" : "Create Announcement");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        layout.getChildren().addAll(
            titleLabel,
            announcementTitleLabel, titleField,
            contentLabel, contentArea,
            priorityLabel, priorityCombo,
            startDateLabel, startDatePicker,
            endDateLabel, endDatePicker,
            displayTypeLabel, displayTypeCombo,
            helpLabel,
            buttonBox
        );

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        Scene scene = new Scene(scrollPane, 550, 650);
        dialog.setScene(scene);

        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            Announcement.Priority priority = priorityCombo.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            Announcement.DisplayType displayType = displayTypeCombo.getValue();

            // Validation
            if (title.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Title", "Please enter a title.");
                return;
            }

            if (content.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Content", "Please enter content.");
                return;
            }

            if (priority == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Priority", "Please select a priority.");
                return;
            }

            if (startDate == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Start Date", "Please select a start date.");
                return;
            }

            if (displayType == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Display Type", "Please select a display type.");
                return;
            }

            // Validate dates
            if (endDate != null && endDate.isBefore(startDate)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Dates", 
                         "End date cannot be before start date.");
                return;
            }

            // Convert dates to ISO format
            String startDateStr = startDate.atStartOfDay().toString();
            String endDateStr = endDate != null ? endDate.atTime(23, 59, 59).toString() : null;

            if (isEdit) {
                // Update existing announcement
                existingAnnouncement.setTitle(title);
                existingAnnouncement.setContent(content);
                existingAnnouncement.setPriority(priority);
                existingAnnouncement.setStartDate(startDateStr);
                existingAnnouncement.setEndDate(endDateStr);
                existingAnnouncement.setDisplayType(displayType);

                boolean success = StatusData.databaseHelper.updateAnnouncement(existingAnnouncement);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Announcement updated successfully!");
                    loadAnnouncements();
                    dialog.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update announcement.");
                }
            } else {
                // Create new announcement
                Announcement newAnnouncement = new Announcement(
                    title, content, priority, startDateStr, endDateStr, displayType, user.getId()
                );

                boolean success = StatusData.databaseHelper.createAnnouncement(newAnnouncement);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                             "Announcement created successfully!");
                    loadAnnouncements();
                    dialog.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create announcement.");
                }
            }
        });

        dialog.show();
    }

    private void deleteAnnouncement(Announcement announcement) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Announcement");
        confirmAlert.setHeaderText("Delete: " + announcement.getTitle());
        confirmAlert.setContentText("Are you sure you want to delete this announcement? This cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = StatusData.databaseHelper.deleteAnnouncement(
                    announcement.getAnnouncementId()
                );
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                             "Announcement deleted successfully.");
                    loadAnnouncements();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete announcement.");
                }
            }
        });
    }

    private String formatDate(String isoDate) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(isoDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            return ldt.format(formatter);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}