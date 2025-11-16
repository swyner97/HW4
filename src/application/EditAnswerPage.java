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
 * The EditAnswerPage class allows users to edit their answers.
 */
public class EditAnswerPage {

    private final WelcomeLoginPage welcomePage;
    private final MyQAPage userPage;
    private final Answer editingAnswer;
    private final Answers answers;

    private Stage stage;
    private User user;

    private TextField titleField;
    private TextArea descriptionArea;

    // Constructor when coming from WelcomeLoginPage
    public EditAnswerPage(WelcomeLoginPage welcomePage, Answer answer, Answers answers) {
        this.welcomePage = welcomePage;
        this.userPage = null;
        this.editingAnswer = answer;
        this.answers = answers;
    }

    // Constructor when coming from MyQAPage
    public EditAnswerPage(MyQAPage userPage, Answer answer, Answers answers) {
        this.welcomePage = null;
        this.userPage = userPage;
        this.editingAnswer = answer;
        this.answers = answers;
    }

    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;

        stage.setTitle("Edit Answer");

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
        Label pageTitle = new Label("Edit Answer");
        pageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Form container
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle(
            "-fx-background-color: #f5f5f5; " +
            "-fx-border-color: #ddd; -fx-border-width: 1; " +
            "-fx-border-radius: 5; -fx-background-radius: 5;"
        );

        // Answer info
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);

        Label idLabel = new Label("Answer ID:");
        idLabel.setStyle("-fx-font-weight: bold;");
        Label idValue = new Label(String.valueOf(editingAnswer.getAnswerId()));

        Label authorLabel = new Label("Author:");
        authorLabel.setStyle("-fx-font-weight: bold;");
        Label authorValue = new Label(editingAnswer.getAuthor());

        infoGrid.add(idLabel, 0, 0);
        infoGrid.add(idValue, 1, 0);
        infoGrid.add(authorLabel, 0, 1);
        infoGrid.add(authorValue, 1, 1);

        Separator separator = new Separator();

        // Edit form
        GridPane editGrid = new GridPane();
        editGrid.setHgap(10);
        editGrid.setVgap(15);

        Label titleLabel = new Label("Title:");
        titleLabel.setStyle("-fx-font-weight: bold;");
        titleField = new TextField();
        titleField.setText(editingAnswer.getContent());
        titleField.setPromptText("Enter answer title...");
        titleField.setPrefWidth(500);

        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold;");
        descriptionArea = new TextArea(editingAnswer.getContent());
        descriptionArea.setPromptText("Enter answer description...");
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

        Button deleteButton = new Button("Delete Answer");
        deleteButton.setStyle(
            "-fx-background-color: #f44336; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 10 30; -fx-font-size: 14px;"
        );
        deleteButton.setOnAction(e -> deleteAnswer(editingAnswer));

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle(
            "-fx-background-color: #2196f3; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 10 30; -fx-font-size: 14px;"
        );
        saveButton.setOnAction(e -> saveChanges());

        buttonBox.getChildren().addAll(cancelButton, deleteButton, saveButton);
        content.getChildren().addAll(pageTitle, formBox, buttonBox);

        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setCenter(content);
        mainPane.setCenter(centerWrapper);

        Scene scene = new Scene(mainPane, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void saveChanges() {
        String newDescription = descriptionArea.getText().trim();

        if (newDescription.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a description for your answer.");
            return;
        }


        boolean isStudent = user.getRole().equalsIgnoreCase("student");
        boolean isAuthor = editingAnswer.getAuthor().equals(user.getUserName());
        boolean isAdmin = user.getRole().equalsIgnoreCase("admin");

        if (isStudent && !isAuthor) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                      "Students can only edit their own answers.");
            return;
        }

        Result result = answers.update(
            editingAnswer.getAnswerId(),
            editingAnswer.getQuestionId(),
            user,
            newDescription,
            editingAnswer.isSolution()
        );

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Answer updated successfully!");
            AnswersPage.reloadAnswers();
            goBackToWelcomePage();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }

    private void goBackToWelcomePage() {
        if (welcomePage != null)
            welcomePage.show(stage, user);
        else if (userPage != null)
            userPage.show(stage, user);
    }

    private void deleteAnswer(Answer answer) {

        boolean isStudent = user.getRole().equalsIgnoreCase("student");
        boolean isAuthor = answer.getAuthor().equals(user.getUserName());
        boolean isAdmin = user.getRole().equalsIgnoreCase("admin");

        if (isStudent && !isAuthor) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                      "Students can only delete their own answers.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Answer?");
        confirmAlert.setContentText("Are you sure you want to delete this answer? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Result result = answers.delete(answer.getAnswerId(), user);

                if (result.isSuccess()) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Answer deleted successfully!");
                    AnswersPage.reloadAnswers();
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
