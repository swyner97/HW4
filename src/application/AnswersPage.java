package application;

import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AnswersPage {
    
    private static Answers answers;
    private static Question question;
    private Object parentPage;
    private User user;
    
    private static TableView<Answer> answerTable;
    private TextArea answerDetails;
    private TextArea answerInput;
    private CheckBox markAsSolution;
    private Button submitButton;
    private Button updateAnswerButton;
    private Button deleteAnswerButton;
    private Button cancelEditButton;
    private Label submitTitle;
    private Answer editingAnswer = null;
    
    public AnswersPage(Object parentPage, Question question) {
        this.parentPage = parentPage;
        this.question = question;
    }
    
    public void show(Stage stage, User user) {
        this.user = user;
        answers = new Answers(StatusData.databaseHelper);
        
        stage.setTitle("Answers for Question #" + question.getQuestionId());
        
        BorderPane mainPane = new BorderPane();
        
        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);
        
        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));
        
        // Top section: Question info + Back button
        VBox topSection = new VBox(10);
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Button backButton = new Button("← Back to Questions");
        backButton.setOnAction(e -> {
            if (parentPage instanceof MyQAPage) {
                ((MyQAPage) parentPage).show(stage, user);
            } else if (parentPage instanceof WelcomeLoginPage) {
                ((WelcomeLoginPage) parentPage).show(stage, user);
            }
        });
        
        Label pageTitle = new Label("Answers for Question #" + question.getQuestionId());
        pageTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(backButton, spacer, pageTitle);
        
        // Question info box
        VBox questionBox = new VBox(5);
        questionBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1;");
        
        Label questionTitle = new Label("Question: " + question.getTitle());
        questionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label questionAuthor = new Label("By: " + question.getAuthor() + " | Status: " + question.getStatus());
        questionAuthor.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label questionDesc = new Label(question.getDescription());
        questionDesc.setWrapText(true);
        questionDesc.setStyle("-fx-font-size: 12px;");
        
        questionBox.getChildren().addAll(questionTitle, questionAuthor, questionDesc);
        
        topSection.getChildren().addAll(headerBox, questionBox);
        
        // Submit Answer Section
        VBox submitSection = new VBox(10);
        submitSection.setPadding(new Insets(10, 0, 10, 0));
        submitSection.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 15; -fx-border-color: #4caf50; -fx-border-width: 1;");
        
        submitTitle = new Label("Submit Your Answer");
        submitTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        answerInput = new TextArea();
        answerInput.setPromptText("Write your answer here...");
        answerInput.setWrapText(true);
        answerInput.setPrefRowCount(4);
        
        HBox submitButtonBox = new HBox(10);
        submitButtonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        markAsSolution = new CheckBox("Mark as solution");
        
        // Only allow certain roles to mark answers as solutions
        String userRole = user.getRole().toLowerCase();
        boolean canMarkAsSolution = userRole.equals("admin") || 
                                     userRole.equals("instructor") || 
                                     userRole.equals("reviewer") ||
                                     userRole.equals("ta") ||
                                     user.getName().equals(question.getAuthor());
        
        markAsSolution.setDisable(!canMarkAsSolution);
        if (!canMarkAsSolution) {
            markAsSolution.setTooltip(new Tooltip("Only admins, instructors, reviewers, TAs, or the question author can mark answers as solutions"));
        }
        
        submitButton = new Button("Submit Answer");
        submitButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        updateAnswerButton = new Button("Update Answer");
        updateAnswerButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        updateAnswerButton.setVisible(false);
        updateAnswerButton.setManaged(false);
        
        deleteAnswerButton = new Button("Delete Answer");
        deleteAnswerButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteAnswerButton.setVisible(false);
        deleteAnswerButton.setManaged(false);
        
        cancelEditButton = new Button("Cancel");
        cancelEditButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white;");
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
        
        submitButton.setOnAction(e -> {
            String answerContent = answerInput.getText().trim();
            if (answerContent.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Answer", "Please write an answer before submitting.");
                return;
            }
            
            boolean isSolution = markAsSolution.isSelected();
            submitAnswer(answerContent, isSolution);
            answerInput.clear();
            markAsSolution.setSelected(false);
            loadAnswers();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your answer has been submitted!");
        });
        
        updateAnswerButton.setOnAction(e -> {
            String answerContent = answerInput.getText().trim();
            if (answerContent.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Answer", "Please write an answer before updating.");
                return;
            }
            
            if (editingAnswer != null) {
                updateAnswer(answerContent);
            }
        });
        
        deleteAnswerButton.setOnAction(e -> {
            if (editingAnswer != null) {
                deleteAnswer(editingAnswer);
            }
        });
        
        cancelEditButton.setOnAction(e -> clearAnswerForm());
        
        submitButtonBox.getChildren().addAll(markAsSolution, submitButton, updateAnswerButton, deleteAnswerButton, cancelEditButton);
        submitSection.getChildren().addAll(submitTitle, answerInput, submitButtonBox);
        
        // Middle: Answers table
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label tableTitle = new Label("All Answers (" + getAnswerCount() + ")");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        answerTable = new TableView<>();
        answerTable.setPrefHeight(200);
        
        TableColumn<Answer, Integer> idCol = new TableColumn<>("Answer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setPrefWidth(80);
        
        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);
        
        TableColumn<Answer, String> contentCol = new TableColumn<>("Content Preview");
        contentCol.setCellValueFactory(cellData -> {
            String answerContent = cellData.getValue().getContent();
            String preview = answerContent.length() > 50 ? answerContent.substring(0, 50) + "..." : answerContent;
            return new javafx.beans.property.SimpleStringProperty(preview);
        });
        contentCol.setPrefWidth(350);
        
        TableColumn<Answer, String> solutionCol = new TableColumn<>("Solution");
        solutionCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().isSolution() ? "✓ Yes" : "No"));
        solutionCol.setPrefWidth(80);
        
        // Edit button column
        TableColumn<Answer, Void> editCol = new TableColumn<>("Actions");
        editCol.setPrefWidth(80);
        editCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                editBtn.setOnAction(event -> {
                    Answer answer = getTableView().getItems().get(getIndex());
                    editAnswer(answer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Answer answer = getTableView().getItems().get(getIndex());
                String role = user.getRole().toLowerCase();

                boolean isAuthor = answer.getUserId() == user.getId();
                boolean isPrivileged = role.contains("admin") || role.contains("staff") ||
                                       role.contains("instructor") || role.contains("teacher") ||
                                       role.contains("reviewer");

                // ✅ Show Edit button only if author or privileged user
                if (isAuthor || isPrivileged) {
                    editBtn.setDisable(false);
                    setGraphic(editBtn);
                } else {
                    setGraphic(null); // Hide completely
                }
            }
        });


        
        answerTable.getColumns().addAll(idCol, authorCol, contentCol, solutionCol, editCol);
        
        answerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) displayAnswerDetails(newVal);
        });
        
        tableBox.getChildren().addAll(tableTitle, answerTable);
        
        // Bottom: Answer details
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label detailsTitle = new Label("Answer Details");
        detailsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        answerDetails = new TextArea();
        answerDetails.setEditable(false);
        answerDetails.setWrapText(true);
        answerDetails.setPrefRowCount(6);
        answerDetails.setPromptText("Select an answer to view full details...");
        
        detailsBox.getChildren().addAll(detailsTitle, answerDetails);
        
        // Combine all sections
        VBox mainContent = new VBox(10);
        mainContent.getChildren().addAll(topSection, submitSection, tableBox, detailsBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        content.setCenter(scrollPane);
        mainPane.setCenter(content);
        
        Scene scene = new Scene(mainPane, 900, 700);
        stage.setScene(scene);
        stage.show();
        
        loadAnswers();
    }
    
    private void submitAnswer(String content, boolean isSolution) {
        answers.create(
            user.getId(), // userId
            question.getQuestionId(), // questionId
            user.getName(), // author
            content // answer content
        );
    }
    
    static void loadAnswers() {
        List<Answer> allAnswers = answers.readAll();
        List<Answer> questionAnswers = allAnswers.stream()
            .filter(a -> a.getQuestionId() == question.getQuestionId())
            .toList();
        answerTable.getItems().setAll(questionAnswers);
        
        // Update answer count
        Label tableTitle = (Label) ((VBox) answerTable.getParent()).getChildren().get(0);
        tableTitle.setText("All Answers (" + questionAnswers.size() + ")");
    }
    
    public static void reloadAnswers() {
        if (answerTable != null) {
            loadAnswers();
        }
    }
    
    private int getAnswerCount() {
        List<Answer> allAnswers = answers.readAll();
        return (int) allAnswers.stream()
            .filter(a -> a.getQuestionId() == question.getQuestionId())
            .count();
    }
    
    private void displayAnswerDetails(Answer a) {
        StringBuilder details = new StringBuilder();
        details.append("Answer ID: ").append(a.getAnswerId()).append("\n");
        details.append("Question ID: ").append(a.getQuestionId()).append("\n");
        details.append("Author: ").append(a.getAuthor()).append("\n");
        details.append("Is Solution: ").append(a.isSolution() ? "Yes" : "No").append("\n");
        details.append("\n--- Full Answer Content ---\n\n");
        details.append(a.getContent());
        
        answerDetails.setText(details.toString());
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void editAnswer(Answer answer) {
        editingAnswer = answer;
        answerInput.setText(answer.getContent());
        markAsSolution.setSelected(answer.isSolution());
        
        submitTitle.setText("Edit Answer #" + answer.getAnswerId());
        submitButton.setVisible(false);
        submitButton.setManaged(false);
        updateAnswerButton.setVisible(true);
        updateAnswerButton.setManaged(true);
        deleteAnswerButton.setVisible(true);
        deleteAnswerButton.setManaged(true);
        cancelEditButton.setVisible(true);
        cancelEditButton.setManaged(true);
        
       
    }
    
    private void updateAnswer(String content) {
        if (editingAnswer == null) return;

        String role = user.getRole().toLowerCase();
        boolean isAuthor = editingAnswer.getUserId() == user.getId();
        boolean isPrivileged = role.contains("admin") || role.contains("staff") ||
                               role.contains("instructor") || role.contains("teacher") ||
                               role.contains("reviewer");

        // Only author or privileged roles can edit
        if (!isAuthor && !isPrivileged) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                      "Only the author or staff can edit this answer.");
            return;
        }

        Result result = answers.update(
                editingAnswer.getAnswerId(),
                editingAnswer.getQuestionId(),
                user,
                content,
                markAsSolution.isSelected()
        );

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your answer has been updated!");
            clearAnswerForm();
            loadAnswers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }

    
    private void deleteAnswer(Answer answer) {
        String role = user.getRole().toLowerCase();
        boolean isAuthor = answer.getUserId() == user.getId();
        boolean isPrivileged = role.contains("admin") || role.contains("staff") ||
                               role.contains("instructor") || role.contains("teacher") ||
                               role.contains("reviewer");

        // Only author or privileged roles can delete
        if (!isAuthor && !isPrivileged) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied",
                      "Only the author or staff can delete this answer.");
            return;
        }

        // Confirm deletion
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
                    clearAnswerForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
                }
            }
        });
    }

    private void clearAnswerForm() {
        editingAnswer = null;
        answerInput.clear();
        markAsSolution.setSelected(false);
        
        submitTitle.setText("Submit Your Answer");
        submitButton.setVisible(true);
        submitButton.setManaged(true);
        updateAnswerButton.setVisible(false);
        updateAnswerButton.setManaged(false);
        deleteAnswerButton.setVisible(false);
        deleteAnswerButton.setManaged(false);
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
    }

}
