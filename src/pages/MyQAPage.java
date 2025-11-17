package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.Result;
import logic.StatusData;
import model.Clarification;
import model.NavigationBar;
import model.Question;
import model.Questions;
import model.User;
import model.User.Role;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.List;

/**
 * The MyQAPage class displays questions created by the logged-in user.
 * It allows users to view their questions and navigate to answers.
 */
public class MyQAPage {
    
    private Questions questions;
    
    private TableView<Question> questionTable;
    private TextArea questionDetails;
    
    private Stage stage;
    private User user;
    
    // Form fields
    private TextField titleField;
    private TextArea descriptionArea;
    
    public void show(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        
        questions = new Questions(StatusData.databaseHelper);
        
        stage.setTitle("My Questions");
        
        BorderPane mainPane = new BorderPane();
        
        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);
        
        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));
        
        // Create Question Form (collapsible)
        VBox createQuestionBox = createQuestionForm();
        
        // Top: Table
        VBox tableBox = new VBox(10);
        
        Label tableTitle = new Label("My Questions");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        questionTable = new TableView<>();
        questionTable.setPrefHeight(250);
        
        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        idCol.setPrefWidth(50);
        
        TableColumn<Question, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(100);
        
        TableColumn<Question, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);
        
        TableColumn<Question, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        
        TableColumn<Question, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            String timestamp = cellData.getValue().getTimestamp();
            return new javafx.beans.property.SimpleStringProperty(timestamp != null ? timestamp : "N/A");
        });
        dateCol.setPrefWidth(150);
        
        // Action buttons column
        TableColumn<Question, Void> actionCol = new TableColumn<>("Answers");
        actionCol.setPrefWidth(220);
        actionCol.setCellFactory(param -> new TableCell<Question, Void>() {
            private final Button viewAnswersBtn = new Button("View Answers");
            private final HBox actionBox = new HBox(10, viewAnswersBtn);

            {
                viewAnswersBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    showAnswersPage(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });

        // Edit column
        TableColumn<Question, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(80);

        editCol.setCellFactory(param -> new TableCell<Question, Void>() {
            private final Button editBtn = new Button("Edit");
            private final HBox actionBox = new HBox(5, editBtn);

            {
                // Button style
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");

                // Click action
                editBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    MyQAPage.this.showEditQuestionPage(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Question question = getTableView().getItems().get(getIndex());

                // Role-based visibility logic
                // Assumes User.getRole() returns User.Role enum
                if (user.getRole() == User.Role.STUDENT) {
                    // Students: only show for their own questions
                    if (question.getAuthor().equals(user.getUserName())) {
                        setGraphic(actionBox);
                    } else {
                        setGraphic(null);
                    }
                } else {
                    // Non-students (teacher/admin/reviewer/etc.): show for all
                    setGraphic(actionBox);
                }
            }
        });

        questionTable.getColumns().addAll(idCol, authorCol, titleCol, statusCol, dateCol, actionCol, editCol);
        
        questionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) displayQuestionDetails(newVal);
        });
        
        tableBox.getChildren().addAll(tableTitle, questionTable);
        
     // ---------Bottom: Details with Tabs ----------
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10, 0, 0, 0));
        
        // Tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Question Details Tab
        questionDetails = new TextArea();
        questionDetails.setEditable(false);
        questionDetails.setWrapText(true);
        questionDetails.setPrefRowCount(8);
        questionDetails.setPromptText("Select a question to view details...");
        
        ScrollPane detailsScroll = new ScrollPane(questionDetails);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background-color: transparent;");
        Tab detailsTab = new Tab("Question Details", detailsScroll);
        
        // View Suggestions tab
        ListView<String> suggestionList = new ListView<>();
        suggestionList.setPlaceholder(new Label ("No suggestions yet."));
        Tab suggestionsTab = new Tab("View Suggestions", suggestionList);
        
        tabPane.getTabs().addAll(detailsTab, suggestionsTab);
        tabPane.setPrefHeight(220);
        tabPane.setStyle("-fx-background-color: white; " +
        	    "-fx-border-color: #2196f3; " +
        	    "-fx-border-radius: 5; -fx-background-radius: 5;"
		);
        
        detailsBox.getChildren().addAll(tabPane);
        
        // Combine all sections
        VBox mainContent = new VBox(15);
        mainContent.getChildren().addAll(createQuestionBox, tableBox, detailsBox);
           
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        content.setCenter(scrollPane);
        mainPane.setCenter(content);
        
        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
        
        loadUserQuestions();
    }
    
    private VBox createQuestionForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Header with toggle button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label formTitle = new Label("Create New Question");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button toggleButton = new Button("▼ Hide");
        toggleButton.setStyle("-fx-font-size: 10px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(formTitle, spacer, toggleButton);
        
        // Form content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));
        
        Label authorLabel = new Label("Author:");
        Label authorValue = new Label(user != null ? user.getName() : "");
        authorValue.setStyle("-fx-font-weight: bold;");
        
        Label titleLabel = new Label("Title:");
        titleField = new TextField();
        titleField.setPromptText("Enter question title...");
        titleField.setPrefWidth(500);
        
        Label descLabel = new Label("Description:");
        
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter question description...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPrefWidth(500);
        descriptionArea.setWrapText(true);

        grid.add(authorLabel, 0, 0);
        grid.add(authorValue, 1, 0);
        grid.add(titleLabel, 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(descLabel, 0, 2);
        grid.add(descriptionArea, 1, 2);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button submitButton = new Button("Post Question");
        submitButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        
        Button clearButton = new Button("Clear");
        clearButton.setStyle("-fx-padding: 8 20;");
        
        submitButton.setOnAction(e -> createQuestion());
        clearButton.setOnAction(e -> clearForm());
        
        buttonBox.getChildren().addAll(clearButton, submitButton);
        
        VBox formContent = new VBox(10);
        formContent.getChildren().addAll(grid, buttonBox);
        
        formBox.getChildren().addAll(header, formContent);
        
        // Toggle functionality
        toggleButton.setOnAction(e -> {
            if (formContent.isVisible()) {
                formContent.setVisible(false);
                formContent.setManaged(false);
                toggleButton.setText("▶ Show");
            } else {
                formContent.setVisible(true);
                formContent.setManaged(true);
                toggleButton.setText("▼ Hide");
            }
        });
        
        return formBox;
    }
    
    private void createQuestion() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a title for your question.");
            return;
        }
        
        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a description for your question.");
            return;
        }
        
        Result result = questions.create(
            user.getId(),
            user.getName(),
            title,
            description,
            null
        );
        
        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your question has been posted!");
            clearForm();
            loadUserQuestions();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }
    
    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
    }
    
    private void loadUserQuestions() {
        // Load only questions authored by the logged-in user
        List<Question> allQuestions = StatusData.databaseHelper.loadAllQs();
        List<Question> userQuestions = allQuestions.stream()
            .filter(q -> q.getAuthor().equals(user.getName()))
            .toList();
        questionTable.getItems().setAll(userQuestions);
    }
    
    private void displayQuestionDetails(Question q) {
        StringBuilder details = new StringBuilder();
        details.append("Question ID: ").append(q.getQuestionId()).append("\n");
        details.append("Author: ").append(q.getAuthor()).append("\n");
        details.append("Title: ").append(q.getTitle()).append("\n");
        details.append("Status: ").append(q.getStatus()).append("\n");
        details.append("Timestamp: ").append(q.getTimestamp() != null ? q.getTimestamp() : "N/A").append("\n");
        details.append("\nDescription:\n");
        details.append(q.getDescription());
        
        questionDetails.setText(details.toString());
    }
    
    private void showAnswersPage(Question question) {
        AnswersPage answersPage = new AnswersPage(this, question);
        answersPage.show(stage, user);
    }
    private void showEditQuestionPage(Question question) {
        EditQuestionPage editPage = new EditQuestionPage(this, question, questions);
        editPage.show(stage, user);
    }

    private void loadSuggestionsForQs(Question question, ListView<String> suggestionList) {
        suggestionList.getItems().clear();
        
        try {
            List<Clarification> list = StatusData.databaseHelper.loadClarificationsforQ(question.getQuestionId());
            
            if (list == null || list.isEmpty()) {
                suggestionList.getItems().add("No suggestions yet.");
            }
            else {
                for (Clarification c : list) {
                    suggestionList.getItems().add(c.getAuthor() + ": " + c.getContent());
                }
            }
        }
        catch (Exception e) {
            suggestionList.getItems().add("Error loading suggestions: " + e.getMessage());
            e.printStackTrace();
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
