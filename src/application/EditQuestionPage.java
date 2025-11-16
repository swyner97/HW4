package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import databasePart1.*;

/**
 * The EditQuestionPage class allows users to edit their questions.
 */
public class EditQuestionPage {

    private final WelcomeLoginPage welcomePage;
    private final MyQAPage userPage;
    private final Question originalQuestion;
    private final Questions questions;

    private Stage stage;
    private User user;

    private TextField titleField;
    private TextArea descriptionArea;

 // Constructor when coming from WelcomeLoginPage
    public EditQuestionPage(WelcomeLoginPage welcomePage, Question question, Questions questions) {
        this.welcomePage = welcomePage;
        this.userPage = null; // not used in this context
        this.originalQuestion = question;
        this.questions = questions;
    }

    // Constructor when coming from MyQAPage
    public EditQuestionPage(MyQAPage userPage, Question question, Questions questions) {
        this.welcomePage = null; // not used in this context
        this.userPage = userPage;
        this.originalQuestion = question;
        this.questions = questions;
    }
    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;

        stage.setTitle("Edit Question");

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
        Label pageTitle = new Label("Edit Question");
        pageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Form container
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Question ID and Author info
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);

        Label idLabel = new Label("Question ID:");
        idLabel.setStyle("-fx-font-weight: bold;");
        Label idValue = new Label(String.valueOf(originalQuestion.getQuestionId()));

        Label authorLabel = new Label("Author:");
        authorLabel.setStyle("-fx-font-weight: bold;");
        Label authorValue = new Label(originalQuestion.getAuthor());

        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        Label statusValue = new Label(originalQuestion.getStatus());

        infoGrid.add(idLabel, 0, 0);
        infoGrid.add(idValue, 1, 0);
        infoGrid.add(authorLabel, 0, 1);
        infoGrid.add(authorValue, 1, 1);
        infoGrid.add(statusLabel, 0, 2);
        infoGrid.add(statusValue, 1, 2);

        Separator separator = new Separator();

        // Edit form
        GridPane editGrid = new GridPane();
        editGrid.setHgap(10);
        editGrid.setVgap(15);

        Label titleLabel = new Label("Title:");
        titleLabel.setStyle("-fx-font-weight: bold;");
        titleField = new TextField(originalQuestion.getTitle());
        titleField.setPromptText("Enter question title...");
        titleField.setPrefWidth(500);

        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold;");
        descriptionArea = new TextArea(originalQuestion.getDescription());
        descriptionArea.setPromptText("Enter question description...");
        descriptionArea.setPrefRowCount(10);
        descriptionArea.setPrefWidth(500);
        descriptionArea.setWrapText(true);

        editGrid.add(titleLabel, 0, 0);
        editGrid.add(titleField, 0, 1);
        editGrid.add(descLabel, 0, 2);
        editGrid.add(descriptionArea, 0, 3);

        formBox.getChildren().addAll(infoGrid, separator, editGrid);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-padding: 10 30; -fx-font-size: 14px;");
        cancelButton.setOnAction(e -> goBackToWelcomePage());

        Button deleteButton = new Button("Delete Question");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-font-size: 14px;");
        deleteButton.setOnAction(e -> deleteQuestion(originalQuestion));

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-font-size: 14px;");
        saveButton.setOnAction(e -> saveChanges());

        buttonBox.getChildren().addAll(cancelButton, deleteButton, saveButton);

        content.getChildren().addAll(pageTitle, formBox, buttonBox);

        // Wrapper to center the content
        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setCenter(content);

        mainPane.setCenter(centerWrapper);

        Scene scene = new Scene(mainPane, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void saveChanges() {
        String newTitle = titleField.getText().trim();
        String newDescription = descriptionArea.getText().trim();

        if (newTitle.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a title for your question.");
            return;
        }

        if (newDescription.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a description for your question.");
            return;
        }

        Result result = questions.update(
                originalQuestion.getQuestionId(),
                originalQuestion.getUserId(), 
                newTitle,
                newDescription,
                originalQuestion.getStatus(),
                originalQuestion.getTags(), user
        );

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Question updated successfully!");
            // Reload the questions table before going back
            welcomePage.reloadQuestions();
            goBackToWelcomePage();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }

    private void goBackToWelcomePage() {
        welcomePage.show(stage, user);
    }


    private void deleteQuestion(Question question) {
        // Check if the user has permission to delete this question
        if (user.getRole().equalsIgnoreCase("student") &&
            !question.getAuthor().equals(user.getName())) {
            
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                      "Students can only delete their own questions.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Question?");
        confirmAlert.setContentText("Are you sure you want to delete this question? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Result result = questions.delete(question.getQuestionId(),question.getUserId(), user);

                if (result.isSuccess()) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Question deleted successfully!");
                    welcomePage.reloadQuestions();
                    goBackToWelcomePage();
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