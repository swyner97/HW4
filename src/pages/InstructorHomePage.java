package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.List;

import logic.*;
import model.*;
import databasePart1.DatabaseHelper;

/**********
 * The {@code InstructorHomePage} class represents the home page for instructors.
 * <p>
 * Instructors can view request from students who wants to become reviewer.
 * Instructors will accept or deny them based on questions and answers students has made.
 * This class manages requests views, questions and answer views through {@link DatabaseHelper}.
 * </p>
 * 
 * <p>
 * Key features:
 * <ul>
 *   <li>Display a list of users requesting reviewer roles.</li>
 *   <li>Accept or deny reviewer requests directly from a table.</li>
 *   <li>View questions and answers by selected users.</li>
 *   <li>Update account details via a separate page.</li>
 * </ul>
 * 
 */

public class InstructorHomePage {

    private TextArea questionDetails;

    private Stage stage;
    private User user;

    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    
    /**********
     * Default constructor. Uses the currently logged-in user and database helper from {@link StatusData}.
     */
    public InstructorHomePage() {
        this(StatusData.databaseHelper, StatusData.currUser);
    }

    /**********
     * Constructor that specifies a database helper and the current user.
     *
     * @param databaseHelper the database helper for performing database operations
     * @param currentUser    the currently logged-in instructor
     */
    public InstructorHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }


    /**********
     * Displays the instructor home page in stage.
     * Sets up reviewer request, and detailed tabs for questions and answers.
     *
     * @param stage the primary stage to display the instructor page
     */
    public void show(Stage stage) {
        this.stage = stage;

        stage.setTitle("Instructor Homepage");

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(new NavigationBar());

        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label instructorLabel = new Label("Hello, Instructor!");
        instructorLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button updateAccountButton = new Button("Update Account");
        updateAccountButton.setOnAction(e ->
            new UpdateAccountPage(databaseHelper, currentUser).show(stage)
        );
        // 🔹 User Table setup
        TableView<User> userTable = new TableView<>();
        userTable.setPlaceholder(new Label("Select a user to view questions"));
        userTable.setEditable(true);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> userNameCol = new TableColumn<>("Username");
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userNameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    User user = getTableView().getItems().get(getIndex());
                    // Highlight users needing password reset
                    if ("PENDING".equalsIgnoreCase(user.getTempPw())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Show all roles a user currently has
        TableColumn<User, String> allRolesCol = new TableColumn<>("All Roles");
        allRolesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        try {
                            List<String> roles = databaseHelper.allUserRoles(user.getUserName());
                            setText(String.join(", ", roles));

                        } catch (SQLException e) {
                            setText("Error loading roles");
                        }
                    }
                }
            }
        });

        // instructor accepts or denies student
        TableColumn<User, String> acceptCol = new TableColumn<>("Accept or Deny");
        acceptCol.setCellFactory(ComboBoxTableCell.forTableColumn("Accept", "Deny"));
        acceptCol.setOnEditCommit(event -> {
        	User user = event.getRowValue();
        	String response = event.getNewValue();
        	if(response.equals("Accept")) {
        		try {
        			databaseHelper.addUserRoles(user.getUserName(), User.Role.REVIEWER);
        			databaseHelper.deleteReviewerRequest(user.getUserName());
        			System.out.println("User " + user.getUserName() + " is given reviewer role");
        			ObservableList<User> updatedRequests = databaseHelper.getAllReviewerRequest();
        	        userTable.setItems(updatedRequests);
        	        userTable.refresh();
        		} catch (SQLException e) {
        			e.printStackTrace();
        		}
        	} else if (response.equals("Deny")) {
                try {
                    databaseHelper.deleteReviewerRequest(user.getUserName());
                    System.out.println("User " + user.getUserName() + "'s request was denied.");
                    ObservableList<User> updatedRequests = databaseHelper.getAllReviewerRequest();
                    userTable.setItems(updatedRequests);
                    userTable.refresh();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        userTable.getColumns().addAll(
            idCol, userNameCol, allRolesCol, acceptCol
        );

        //display question and answer
        Label tableTitle = new Label("Questions and Answers");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
      
        // ---------Bottom: Details with Tabs ---------- <- from welcomeloginpage
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
        detailsBox.getChildren().addAll(tabPane);
        
        // View questions tab
        ListView<String> questionList = new ListView<>();
        questionList.setPlaceholder(new Label ("No questions yet."));
        Tab questionsTab = new Tab("View Questions", questionList);
        
        // View Answers tab
        ListView<String> answerList = new ListView<>();
        answerList.setPlaceholder(new Label ("No answers yet."));
        Tab answersTab = new Tab("View Answers", answerList);
        
        //combine tabs
        tabPane.getTabs().addAll(questionsTab, answersTab);
        tabPane.setPrefHeight(220);
        tabPane.setStyle("-fx-background-color: white; " +
        	    "-fx-border-color: #2196f3; " +
        	    "-fx-border-radius: 5; -fx-background-radius: 5;"
		);
        
        //when user selected, it will display their questions or answers
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, selectedUser) -> {
            if (selectedUser != null) {
                try {
                    List<Question> questionsByUser = databaseHelper.getQuestionsByUser(selectedUser.getUserName());
                    questionList.getItems().setAll(displayQuestionDetailsString(questionsByUser));
                    List<Answer> answersByUser = databaseHelper.getAnswersByUser(selectedUser.getUserName());
                    answerList.getItems().setAll(displayAnswersString(answersByUser));
                    //doesnt work 
                    ObservableList<User> updatedRequests = databaseHelper.getAllReviewerRequest();
                    if (updatedRequests.size() == 0) {
                    	answerList.setPlaceholder(new Label ("No answers yet."));
                    	questionList.setPlaceholder(new Label ("No questions yet."));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    questionDetails.setText("Error loading questions: " + e.getMessage());
                }
            }
        });
        
        //retrieve user table info fron reviewer request table
        try {
            ObservableList<User> users = databaseHelper.getAllReviewerRequest();
            userTable.setItems(users);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        layout.getChildren().addAll(instructorLabel, userTable, tableTitle, detailsBox);
        borderPane.setCenter(layout);

        Scene instructorScene = new Scene(borderPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(instructorScene);
        stage.setTitle("Instructor Page");
        stage.show();
    }     
    
    /**********
     * Converts a list of {@link Question} objects into a string 
     *
     * @param questions the list of questions to display
     * @return a formatted string
     */
    //same thing from welcomelogin w/ small adjustments
    private String displayQuestionDetailsString(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return ("No questions made yet.");
        }
        StringBuilder sb = new StringBuilder();
        for (Question q: questions) {
        	sb.append("Question: ").append(q.getDescription()).append("\n");
        	sb.append("Author: ").append(q.getAuthor()).append("\n");
        	sb.append("Title: ").append(q.getTitle()).append("\n");
        	sb.append("Status: ").append(q.getStatus()).append("\n");
        	sb.append("Timestamp: ").append(q.getTimestamp() != null ? q.getTimestamp() : "N/A").append("\n");
        }
        return sb.toString();
    }
    
    /**********
     * Converts a list of {@link Answer} objects into a string
     *
     * @param answers the list of answers to display
     * @return a formatted string 
     */
    //same thing from welcomelogin w/ small adjustments
    private String displayAnswersString(List<Answer> answers) {
        if (answers == null || answers.isEmpty()) {
            return "No answers yet.\n";
        }

        StringBuilder sb = new StringBuilder();
        for (Answer a : answers) {
            sb.append("Answer ID: ").append(a.getAnswerId()).append("\n");
            sb.append("  Author: ").append(a.getAuthor()).append("\n");
            sb.append("  Content: ").append(a.getContent()).append("\n");
            sb.append("  Timestamp: ").append(a.getTimestamp() != null ? a.getTimestamp() : "N/A").append("\n");
            sb.append("  ").append(a.isSolution() ? "[Solution]" : "[Answer]").append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    
    
}
