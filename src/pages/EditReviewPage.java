package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.*;
import model.*;

import javafx.geometry.*;

/**
 * The EditReviewPage class allows users to edit their reviews.
 */
public class EditReviewPage {

    private final ReviewPage reviewPage;
    private final Review editingReview;
    private final Reviews reviews;

    private Stage stage;
    private User user;

    private TextField titleField;
    private TextArea descriptionArea;

    // Constructor when coming from ReviewPage
    public EditReviewPage(ReviewPage reviewPage, Review review, Reviews reviews) {
        this.reviewPage = reviewPage;
        this.editingReview = review;
        this.reviews = reviews;
    }

    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;

        stage.setTitle("Edit Review");

        BorderPane mainPane = new BorderPane();

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Center content
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setMaxWidth(700);
        content.setStyle("-fx-alignment: center;");

        // Page title
        Label pageTitle = new Label("Edit Review");
        pageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Form container
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle(
            "-fx-background-color: #f5f5f5; " +
            "-fx-border-color: #ddd; -fx-border-width: 1; " +
            "-fx-border-radius: 5; -fx-background-radius: 5;"
        );

        // Review info
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);

        Label idLabel = new Label("Review ID:");
        idLabel.setStyle("-fx-font-weight: bold;");
        Label idValue = new Label(String.valueOf(editingReview.getReviewId()));

        Label authorLabel = new Label("Author:");
        authorLabel.setStyle("-fx-font-weight: bold;");
        Label authorValue = new Label(editingReview.getAuthor());

        Label answerIdLabel = new Label("Answer ID:");
        answerIdLabel.setStyle("-fx-font-weight: bold;");
        Label answerIdValue = new Label(String.valueOf(editingReview.getAnswerId()));

        infoGrid.add(idLabel, 0, 0);
        infoGrid.add(idValue, 1, 0);
        infoGrid.add(authorLabel, 0, 1);
        infoGrid.add(authorValue, 1, 1);
        infoGrid.add(answerIdLabel, 0, 2);
        infoGrid.add(answerIdValue, 1, 2);

        Separator separator = new Separator();

        // Edit form
        GridPane editGrid = new GridPane();
        editGrid.setHgap(10);
        editGrid.setVgap(15);

        Label descLabel = new Label("Review Content:");
        descLabel.setStyle("-fx-font-weight: bold;");
        descriptionArea = new TextArea(editingReview.getContent());
        descriptionArea.setPromptText("Enter your review...");
        descriptionArea.setPrefRowCount(10);
        descriptionArea.setPrefWidth(500);
        descriptionArea.setWrapText(true);

        editGrid.add(descLabel, 0, 0);
        editGrid.add(descriptionArea, 0, 1);

        formBox.getChildren().addAll(infoGrid, separator, editGrid);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-padding: 10 30; -fx-font-size: 14px;");
        cancelButton.setOnAction(e -> goBackToReviewPage());

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle(
            "-fx-background-color: #2196f3; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 10 30; -fx-font-size: 14px;"
        );
        saveButton.setOnAction(e -> saveChanges());

        // Only add delete button if user is the author
        boolean isAuthor = editingReview.getAuthor().equals(user.getName());

        if (isAuthor) {
            Button deleteButton = new Button("Delete Review");
            deleteButton.setStyle(
                "-fx-background-color: #f44336; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 30; -fx-font-size: 14px;"
            );
            deleteButton.setOnAction(e -> deleteReview());
            buttonBox.getChildren().addAll(cancelButton, deleteButton, saveButton);
        } else {
            buttonBox.getChildren().addAll(cancelButton, saveButton);
        }
        
        content.getChildren().addAll(pageTitle, formBox, buttonBox);

        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setCenter(content);
        mainPane.setCenter(centerWrapper);

        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    private void saveChanges() {
        String newContent = descriptionArea.getText().trim();

        if (newContent.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter content for your review.");
            return;
        }

        User.Role role = user.getRole(); 

        boolean isStudent = role == User.Role.STUDENT;
        boolean isAuthor = editingReview.getAuthor().equals(user.getName());

        if (isStudent && !isAuthor) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                      "Students can only edit their own reviews.");
            return;
        }

        Result result = reviews.update(
            editingReview.getReviewId(),
            editingReview.getAnswerId(),
            user,
            newContent
        );

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Review updated successfully!");
            goBackToReviewPage();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }

    private void goBackToReviewPage() {
        // Get the answer associated with this review
        Answers answersManager = new Answers(StatusData.databaseHelper);
        Answer answer = answersManager.read(editingReview.getAnswerId());
        
        if (answer != null && reviewPage != null) {
            reviewPage.showForAnswer(stage, user, answer);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not return to answer page.");
        }
    }

    private void deleteReview() {
        User.Role role = user.getRole();

        boolean isStudent = role == User.Role.STUDENT;
        boolean isAuthor = editingReview.getAuthor().equals(user.getName());

        if (isStudent && !isAuthor) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                      "Students can only delete their own reviews.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Review?");
        confirmAlert.setContentText("Are you sure you want to delete this review? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Result result = reviews.delete(editingReview.getReviewId(), user);

                if (result.isSuccess()) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Review deleted successfully!");
                    goBackToReviewPage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
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