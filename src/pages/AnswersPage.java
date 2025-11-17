package pages;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.Result;
import logic.StatusData;
import model.Answer;
import model.Answers;
import model.NavigationBar;
import model.Question;
import model.User;

/**
 * AnswersPage UI: shows answers for a question, allows submitting, editing, deleting
 */
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
   
    private Button postReviewButton;
    private Label submitTitle;
    private Answer editingAnswer = null;

    public AnswersPage(Object parentPage, Question question) {
        this.parentPage = parentPage;
        AnswersPage.question = question;
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
        User.Role role = user.getRole();

        boolean canMarkAsSolution =
                role == User.Role.ADMIN ||
                role == User.Role.INSTRUCTOR ||
                role == User.Role.REVIEWER ||
                role == User.Role.TA ||
                user.getName().equals(question.getAuthor());

        markAsSolution.setDisable(!canMarkAsSolution);

        if (!canMarkAsSolution) {
            markAsSolution.setTooltip(new Tooltip(
                "Only admins, instructors, reviewers, TAs, or the question author can mark answers as solutions"
            ));
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
        
        submitButton.setOnAction(e -> {
            String answerContent = answerInput.getText().trim();
            if (answerContent.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Answer", "Please write an answer before submitting.");
                return;
            }

            boolean isSolution = markAsSolution.isSelected();
            
            Result result = answers.create(
                user.getId(),
                question.getQuestionId(),
                user.getName(),
                answerContent,
                isSolution  // NOW THIS WILL WORK
            );
            
            if (result.isSuccess()) {
                answerInput.clear();
                markAsSolution.setSelected(false);
                loadAnswers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your answer has been submitted!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
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

        Label tableTitle = new Label("💬 All Answers (" + getAnswerCount() + ")");
        tableTitle.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50; " +
            "-fx-padding: 5 0 10 0;"
        );

        answerTable = new TableView<>();
        answerTable = new TableView<>();
        answerTable.setPrefHeight(300);
        answerTable.setFixedCellSize(50); 
        answerTable.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3); " +
            "-fx-padding: 0;"
        );

        TableColumn<Answer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
        idCol.setCellFactory(column -> new TableCell<Answer, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("#" + item);
                    setStyle(
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #495057; " +
                        "-fx-font-size: 12px; " +
                        "-fx-alignment: center;"
                    );
                }
            }
        });
        idCol.setPrefWidth(60);

        TableColumn<Answer, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setCellFactory(column -> new TableCell<Answer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("👤 " + item);
                    setStyle(
                        "-fx-text-fill: #495057; " +
                        "-fx-font-size: 12px;"
                    );
                }
            }
        });
        authorCol.setPrefWidth(120);

        TableColumn<Answer, String> contentCol = new TableColumn<>("Content Preview");
        contentCol.setCellValueFactory(cellData -> {
            String answerContent = cellData.getValue().getContent();
            String preview = answerContent.length() > 50 ? answerContent.substring(0, 50) + "..." : answerContent;
            return new javafx.beans.property.SimpleStringProperty(preview);
        });
        contentCol.setCellFactory(column -> new TableCell<Answer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle(
                        "-fx-text-fill: #212529; " +
                        "-fx-font-size: 12px;"
                    );
                }
            }
        });
        contentCol.setPrefWidth(350);

        TableColumn<Answer, Void> reviewCol = new TableColumn<>("Reviews");
        reviewCol.setPrefWidth(120);

        reviewCol.setCellFactory(col -> new TableCell<Answer, Void>() {
            private final Button viewReviewsBtn = new Button("View Reviews");
            
            
           

            {
            	viewReviewsBtn.setStyle(
            		    "-fx-background-color: #0d6efd; " +
            		    "-fx-text-fill: white; " +
            		    "-fx-font-size: 11px; " +
            		    "-fx-font-weight: 600; " +
            		    "-fx-padding: 8 14; " +
            		    "-fx-background-radius: 6; " +
            		    "-fx-cursor: hand; " +
            		    "-fx-effect: dropshadow(gaussian, rgba(13,110,253,0.2), 4, 0, 0, 2);"
            		);
            		viewReviewsBtn.setOnMouseEntered(e ->
            		    viewReviewsBtn.setStyle(
            		        "-fx-background-color: #0b5ed7; " +
            		        "-fx-text-fill: white; " +
            		        "-fx-font-size: 11px; " +
            		        "-fx-font-weight: 600; " +
            		        "-fx-padding: 8 14; " +
            		        "-fx-background-radius: 6; " +
            		        "-fx-cursor: hand; " +
            		        "-fx-effect: dropshadow(gaussian, rgba(13,110,253,0.4), 6, 0, 0, 3);"
            		    )
            		);
            		viewReviewsBtn.setOnMouseExited(e ->
            		    viewReviewsBtn.setStyle(
            		        "-fx-background-color: #0d6efd; " +
            		        "-fx-text-fill: white; " +
            		        "-fx-font-size: 11px; " +
            		        "-fx-font-weight: 600; " +
            		        "-fx-padding: 8 14; " +
            		        "-fx-background-radius: 6; " +
            		        "-fx-cursor: hand; " +
            		        "-fx-effect: dropshadow(gaussian, rgba(13,110,253,0.2), 4, 0, 0, 2);"
            		    )
            		);



                viewReviewsBtn.setPrefWidth(100);
                viewReviewsBtn.setPrefHeight(40);
                viewReviewsBtn.setMaxHeight(40);
                viewReviewsBtn.setOnAction(event -> {
                    Answer answer = getTableRow().getItem();
                    if (answer != null && answerTable.getScene() != null) {
                        Stage owner = (Stage) answerTable.getScene().getWindow();
                        ReviewPage rp = new ReviewPage(AnswersPage.this);
                        rp.showForAnswer(owner, user, answer);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(viewReviewsBtn);
            }
        });


        TableColumn<Answer, String> solutionCol = new TableColumn<>("Solution");
        solutionCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isSolution() ? "Yes" : "No"));
        solutionCol.setCellFactory(column -> new TableCell<Answer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item.equals("Yes") ? "✓ Solution" : "");
                    if (item.equals("Yes")) {
                        badge.setPadding(new Insets(4, 12, 4, 12));
                        badge.setStyle(
                            "-fx-background-color: #d1e7dd; " +
                            "-fx-text-fill: #0f5132; " +
                            "-fx-background-radius: 15; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;"
                        );
                        setGraphic(badge);
                    } else {
                        setText("-");
                        setStyle("-fx-text-fill: #6c757d; -fx-alignment: center;");
                    }
                    setText(null);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
        solutionCol.setPrefWidth(100);

        // Edit button column
        TableColumn<Answer, Void> editCol = new TableColumn<>("Actions");
        editCol.setPrefWidth(80);
        editCol.setCellFactory(param -> new TableCell<Answer, Void>() {
            private final Button editBtn = new Button("Edit");

            {
        		// Edit Button
        		editBtn.setStyle(
        		    "-fx-background-color: #fd7e14; " +
        		    "-fx-text-fill: white; " +
        		    "-fx-font-size: 11px; " +
        		    "-fx-font-weight: 600; " +
        		    "-fx-padding: 8 14; " +
        		    "-fx-background-radius: 6; " +
        		    "-fx-cursor: hand; " +
        		    "-fx-effect: dropshadow(gaussian, rgba(253,126,20,0.2), 4, 0, 0, 2);"
        		);
        		editBtn.setOnMouseEntered(e ->
        		    editBtn.setStyle(
        		        "-fx-background-color: #e8590c; " +
        		        "-fx-text-fill: white; " +
        		        "-fx-font-size: 11px; " +
        		        "-fx-font-weight: 600; " +
        		        "-fx-padding: 8 14; " +
        		        "-fx-background-radius: 6; " +
        		        "-fx-cursor: hand; " +
        		        "-fx-effect: dropshadow(gaussian, rgba(253,126,20,0.4), 6, 0, 0, 3);"
        		    )
        		);
        		editBtn.setOnMouseExited(e ->
        		    editBtn.setStyle(
        		        "-fx-background-color: #fd7e14; " +
        		        "-fx-text-fill: white; " +
        		        "-fx-font-size: 11px; " +
        		        "-fx-font-weight: 600; " +
        		        "-fx-padding: 8 14; " +
        		        "-fx-background-radius: 6; " +
        		        "-fx-cursor: hand; " +
        		        "-fx-effect: dropshadow(gaussian, rgba(253,126,20,0.2), 4, 0, 0, 2);"
        		    )
        		);
                editBtn.setPrefWidth(90);
                editBtn.setPrefHeight(40);
                editBtn.setMaxHeight(40); 
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
                boolean isAuthor = answer.getUserId() == user.getId();
                boolean isPrivileged = user.isPrivileged();


                // ✅ Show Edit button only if author or privileged user
                if (isAuthor || isPrivileged) {
                    editBtn.setDisable(false);
                    setGraphic(editBtn);
                } else {
                    setGraphic(null); // Hide completely
                }
            }
        });

        answerTable.getColumns().addAll(idCol, authorCol, contentCol, solutionCol, editCol, reviewCol);

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

        postReviewButton = new Button("Post Review");
        postReviewButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        
        postReviewButton.setOnAction(e -> {
            Answer selected = answerTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Answer Selected", "Select an answer before posting a review.");
                return;
            }

            // open ReviewPage showing reviews for this answer (in same stage)
            ReviewPage rp = new ReviewPage(this);
            // showForAnswer expects (Stage, User, Answer)
            rp.showForAnswer((Stage) answerTable.getScene().getWindow(), user, selected);
        });

        detailsBox.getChildren().addAll(detailsTitle, answerDetails, postReviewButton);

        // Combine all sections
        VBox mainContent = new VBox(10);
        mainContent.getChildren().addAll(topSection, submitSection, tableBox, detailsBox);

        
        
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        content.setCenter(scrollPane);
        mainPane.setCenter(content);

        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();

        loadAnswers();
    }

    private void submitAnswer(String content, boolean isSolution) {
        // Create the answer
        Result result = answers.create(
                user.getId(), // userId
                question.getQuestionId(), // question
                user.getName(), // author
                content // answer content
        );
        
        // If successful and should be marked as solution, update it
        if (result.isSuccess() && isSolution) {
            Answer newAnswer = (Answer) result.getData();
            if (newAnswer != null) {
                // Update the answer to mark it as solution
                answers.update(
                    newAnswer.getAnswerId(),
                    question.getQuestionId(),
                    user,
                    content,
                    true  // Mark as solution
                );
            }
        }
    }

    static void loadAnswers() {
        List<Answer> allAnswers = answers.readAll();
        List<Answer> questionAnswers = allAnswers.stream()
                .filter(a -> a.getQuestionId() == question.getQuestionId())
                .toList();
        answerTable.getItems().setAll(questionAnswers);

        // Update answer count (table title is first child of the VBox parent)
        if (answerTable.getParent() instanceof VBox) {
            VBox parent = (VBox) answerTable.getParent();
            if (!parent.getChildren().isEmpty() && parent.getChildren().get(0) instanceof Label) {
                Label tableTitle = (Label) parent.getChildren().get(0);
                tableTitle.setText("All Answers (" + questionAnswers.size() + ")");
            }
        }
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

        User.Role role = user.getRole();  
        boolean isAuthor = editingAnswer.getUserId() == user.getId();
        boolean isPrivileged =
                role == User.Role.ADMIN ||
                role == User.Role.STAFF ||
                role == User.Role.INSTRUCTOR ||
                role == User.Role.REVIEWER ||
                role == User.Role.TA; 

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
    	boolean isAuthor = answer.getUserId() == user.getId();
    	boolean isPrivileged = user.isPrivileged();

    	// Only author or privileged roles can delete
    	if (!isAuthor && !isPrivileged) {
    	    showAlert(Alert.AlertType.ERROR, "Permission Denied",
    	            "Only the author or privileged staff can delete this review.");
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