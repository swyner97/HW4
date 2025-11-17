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

import java.util.List;

public class StaffFAQMgmtPage {
    private Stage stage;
    private User user;
    private TableView<FAQ> faqTable;

    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;

        stage.setTitle("FAQ Management");

        BorderPane mainPane = new BorderPane();

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("FAQ Management");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subHeader = new Label("Manage questions marked as FAQs");
        subHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        VBox header = new VBox(5, headerLabel, subHeader);
        header.setAlignment(Pos.CENTER_LEFT);

        // Table
        faqTable = new TableView<>();
        faqTable.setPrefHeight(400);

        TableColumn<FAQ, Integer> idCol = new TableColumn<>("FAQ ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("faqId"));
        idCol.setPrefWidth(70);

        TableColumn<FAQ, Integer> qIdCol = new TableColumn<>("Question ID");
        qIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        qIdCol.setPrefWidth(100);

        TableColumn<FAQ, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("questionTitle"));
        titleCol.setPrefWidth(300);

        TableColumn<FAQ, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<FAQ, String> dateCol = new TableColumn<>("Date Added");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateMarked"));
        dateCol.setPrefWidth(150);

        // Actions column
        TableColumn<FAQ, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button removeBtn = new Button("Remove");
            private final HBox actionBox = new HBox(5, editBtn, removeBtn);

            {
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                removeBtn.setStyle("-fx-background-color: #e91e63; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    FAQ faq = getTableView().getItems().get(getIndex());
                    showEditFAQDialog(faq);
                });

                removeBtn.setOnAction(event -> {
                    FAQ faq = getTableView().getItems().get(getIndex());
                    removeFAQ(faq);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        faqTable.getColumns().addAll(idCol, qIdCol, titleCol, categoryCol, dateCol, actionsCol);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> loadFAQs());

        Button viewAllQsBtn = new Button("View All Questions");
        viewAllQsBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        viewAllQsBtn.setOnAction(e -> {
            new WelcomeLoginPage().show(stage, user);
        });

        buttonBox.getChildren().addAll(refreshBtn, viewAllQsBtn);

        content.getChildren().addAll(header, faqTable, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        mainPane.setCenter(scrollPane);

        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();

        loadFAQs();
    }

    private void loadFAQs() {
        List<FAQ> faqs = StatusData.databaseHelper.getAllFAQs();
        faqTable.getItems().setAll(faqs);
    }

    private void showEditFAQDialog(FAQ faq) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit FAQ");
        dialog.initOwner(stage);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Edit FAQ #" + faq.getFaqId());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "General", "Technical", "Account", "Features", "Troubleshooting", "Other"
        );
        categoryCombo.setValue(faq.getCategory());
        categoryCombo.setEditable(true);
        categoryCombo.setPrefWidth(300);

        Label displayTitleLabel = new Label("Display Title:");
        TextField displayTitleField = new TextField(faq.getDisplayTitle());
        displayTitleField.setPrefWidth(300);

        Label notesLabel = new Label("Staff Notes:");
        TextArea notesArea = new TextArea(faq.getStaffNotes());
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        notesArea.setPrefWidth(300);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        layout.getChildren().addAll(
            titleLabel,
            categoryLabel, categoryCombo,
            displayTitleLabel, displayTitleField,
            notesLabel, notesArea,
            buttonBox
        );

        Scene scene = new Scene(layout, 400, 400);
        dialog.setScene(scene);

        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            faq.setCategory(categoryCombo.getValue());
            faq.setDisplayTitle(displayTitleField.getText());
            faq.setStaffNotes(notesArea.getText());

            boolean success = StatusData.databaseHelper.updateFAQ(faq);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "FAQ updated successfully!");
                loadFAQs();
                dialog.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update FAQ.");
            }
        });

        dialog.show();
    }

    private void removeFAQ(FAQ faq) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Remove FAQ");
        confirmAlert.setHeaderText("Remove FAQ #" + faq.getFaqId());
        confirmAlert.setContentText("Are you sure you want to remove this question from the FAQ page?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean removed = StatusData.databaseHelper.removeQuestionFromFAQ(faq.getQuestionId());
                if (removed) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "FAQ removed successfully.");
                    loadFAQs();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove FAQ.");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}