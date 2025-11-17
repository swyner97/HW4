package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.List;
import logic.*;
import model.*;

import java.sql.SQLException;


/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
    private Questions questions;

    private TableView<Question> questionTable;
    private TextArea questionDetails;

    private Stage stage;
    private User user;

    // Form fields
    private TextField titleField;
    private TextArea descriptionArea;

    @SuppressWarnings({ "unchecked", "unused" })
	public void show(Stage stage, User user) {
    	
    	//debug
    	try {
			System.out.println("All UserRoles: " + StatusData.databaseHelper.allUserRoles(StatusData.currUser.getUserName()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	try {
    	    List<Question> allQs = StatusData.databaseHelper.loadAllQs();
    	    for (Question q : allQs) {
    	        boolean hasSol = StatusData.databaseHelper.questionHasSolution(q.getQuestionId());
    	        System.out.println("Q#" + q.getQuestionId() + " has solution: " + hasSol);
    	    }
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
        this.stage = stage;
        this.user = user;

        questions = new Questions(StatusData.databaseHelper);

        stage.setTitle("Home");

        BorderPane mainPane = new BorderPane();

     // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Center content
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(15));

        // Create Question Form (collapsible)
        VBox createQuestionBox = createQuestionForm();

        // Initialize table FIRST - create a NEW table each time
        questionTable = new TableView<>();
        questionTable.setPrefHeight(350);
        questionTable.setFixedCellSize(50);

        // Top: Table
        VBox tableBox = new VBox(10);

        Label tableTitle = new Label("📋 Questions");
        tableTitle.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50; " +
            "-fx-padding: 5 0 0 5;"
        );
        
        // Create horizontal header bar
        HBox tableHeader = new HBox(15);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(15, 10, 15, 10));
        tableHeader.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%); " +
            "-fx-border-color: #e9ecef; " +
            "-fx-border-width: 0 0 2 0; " +
            "-fx-background-radius: 10 10 0 0;"
        );
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label questionCount = new Label("Loading...");
        questionCount.setStyle(
            "-fx-background-color: #e3f2fd; " +
            "-fx-text-fill: #1976d2; " +
            "-fx-padding: 4 10; " +
            "-fx-background-radius: 12; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold;"
        );
        
        tableHeader.getChildren().addAll(tableTitle, spacer, questionCount);

        TableColumn<Question, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId")); 
        idCol.setCellFactory(column -> new TableCell<Question, Integer>() {
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



        TableColumn<Question, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setCellFactory(column -> new TableCell<Question, String>() {
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

        TableColumn<Question, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title")); 
        titleCol.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle(
                        "-fx-text-fill: #212529; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 600;"
                    );
                }
            }
        });
        titleCol.setPrefWidth(280);

        TableColumn<Question, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status")); // ADD THIS BACK
        statusCol.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label statusLabel = new Label(item);
                    statusLabel.setPadding(new Insets(4, 12, 4, 12));
                    statusLabel.setStyle(
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        getStatusStyle(item.toLowerCase())
                    );
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
            
            private String getStatusStyle(String status) {
                switch (status) {
                    case "open":
                        return "-fx-background-color: #cfe2ff; -fx-text-fill: #084298;";
                    case "resolved":
                        return "-fx-background-color: #d1e7dd; -fx-text-fill: #0f5132;";
                    case "closed":
                        return "-fx-background-color: #e2e3e5; -fx-text-fill: #41464b;";
                    default:
                        return "-fx-background-color: #fff3cd; -fx-text-fill: #997404;";
                }
            }
        });
        statusCol.setPrefWidth(100);

        TableColumn<Question, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            String timestamp = cellData.getValue().getTimestamp();
            return new javafx.beans.property.SimpleStringProperty(timestamp != null ? timestamp : "N/A");
        });
        dateCol.setCellFactory(column -> new TableCell<Question, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("🕐 " + item);
                    setStyle(
                        "-fx-text-fill: #6c757d; " +
                        "-fx-font-size: 11px;"
                    );
                }
            }
        });
        dateCol.setPrefWidth(180);
        

        // Action buttons column
        TableColumn<Question, Void> actionCol = new TableColumn<>("Answers");
        actionCol.setPrefWidth(120);
        
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewAnswersBtn = new Button("View Answers");
            
            private final HBox actionBox = new HBox(5, viewAnswersBtn);

            {
                viewAnswersBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    showAnswersPage(question);
                });
                
                viewAnswersBtn.setPrefWidth(110);
                viewAnswersBtn.setPrefHeight(40);
                viewAnswersBtn.setMaxHeight(40); 
                
                viewAnswersBtn.setStyle(
                	    "-fx-background-color: #0d6efd; " +
                	    "-fx-text-fill: white; " +
                	    "-fx-font-size: 11px; " +
                	    "-fx-font-weight: 600; " +
                	    "-fx-padding: 8 14; " +
                	    "-fx-background-radius: 6; " +
                	    "-fx-cursor: hand; " +
                	    "-fx-effect: dropshadow(gaussian, rgba(13,110,253,0.2), 4, 0, 0, 2);"
                	);
                	viewAnswersBtn.setOnMouseEntered(e -> 
                	    viewAnswersBtn.setStyle(
                	        "-fx-background-color: #0b5ed7; " +
                	        "-fx-text-fill: white; " +
                	        "-fx-font-size: 11px; " +
                	        "-fx-font-weight: 600; " +
                	        "-fx-padding: 8 14; " +
                	        "-fx-background-radius: 6; " +
                	        "-fx-cursor: hand; " +
                	        "-fx-effect: dropshadow(gaussian, rgba(13,110,253,0.4), 6, 0, 0, 3); " +
                	        "-fx-scale-y: 1.05;"
                	    )
                	);
                	viewAnswersBtn.setOnMouseExited(e -> 
                	    viewAnswersBtn.setStyle(
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
            
        //Clarification button column
        TableColumn<Question, Void> clarifyCol = new TableColumn<>("Suggestions");
        clarifyCol.setPrefWidth(100);
        
        clarifyCol.setCellFactory(param -> new TableCell<>() {
        	private final Button suggestClarificationBtn = new Button("Suggest\nClarification");
        	private final HBox suggestClarification = new HBox(5, suggestClarificationBtn);
        
        	
        	{
        		//suggestClarificationBtn.setMinWidth(100);  
        		suggestClarificationBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        		suggestClarificationBtn.setWrapText(true);
        	 
            	suggestClarificationBtn.setPrefWidth(90);
            	suggestClarificationBtn.setPrefHeight(40);
            	suggestClarificationBtn.setMaxHeight(40); 
        	    suggestClarificationBtn.setAlignment(Pos.CENTER);

        		
	            suggestClarificationBtn.setOnAction(event -> {
	                Question question = getTableView().getItems().get(getIndex());
	                showClarificationPopup(question);
	            });
        	}
        	 @Override
             protected void updateItem(Void item, boolean empty) {
                 super.updateItem(item, empty);
                 setGraphic(empty ? null : suggestClarification);
             }
           
        });
        
     // Edit column
        TableColumn<Question, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(80);

        editCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            
            private final HBox actionBox = new HBox(5, editBtn);

            {
                // Button style
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
                editBtn.setPrefWidth(80);
                editBtn.setPrefHeight(40);
                editBtn.setMaxHeight(40);

                // Click action
                editBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    WelcomeLoginPage.this.showEditQuestionPage(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                // clear previous graphic/text
                setGraphic(null);
                setText(null);

                if (empty) return;

                Question question = getTableView().getItems().get(getIndex());
                if (question == null || user == null) return;

                // Normalize and check values
                String role = (user.getRole() != null)
                        ? user.getRole().name().toLowerCase()
                        : "";

                String author = (question.getAuthor() != null) ? question.getAuthor().trim().toLowerCase() : "";
                String name = (user.getName() != null) ? user.getName().trim().toLowerCase() : "";
                String username = (user.getUserName() != null) ? user.getUserName().trim().toLowerCase() : "";

                boolean isPrivileged = role.contains("admin") || role.contains("staff") ||
                                       role.contains("instructor") || role.contains("teacher") ||
                                       role.contains("reviewer") || role.contains("ta");

              
                boolean isAuthor = author.equals(name) || author.equals(username) || question.getUserId() == user.getId();

         
                if (isAuthor || isPrivileged) {
                    setGraphic(actionBox);
                } else {
                    setGraphic(null);
                }
            }
        });
        
     // FAQ column (only visible to staff)
        TableColumn<Question, Void> faqCol = new TableColumn<>("FAQ");
        faqCol.setPrefWidth(100);

        faqCol.setCellFactory(param -> new TableCell<>() {
            private final Button markFAQBtn = new Button();
            private final HBox actionBox = new HBox(5, markFAQBtn);

            {
                markFAQBtn.setWrapText(true);
                markFAQBtn.setPrefWidth(90);
                markFAQBtn.setPrefHeight(40);
                markFAQBtn.setMaxHeight(40); 

                markFAQBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    if (question == null) return;

                    try {
                        boolean already = StatusData.databaseHelper.isQuestionMarkedAsFAQ(question.getQuestionId());

                        if (already) {
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                            		"Remove this question from the FAQ page?", ButtonType.OK, ButtonType.CANCEL);
                            confirm.setTitle("Remove FAQ");
                            confirm.initOwner(getScene() == null ? null : (Stage) getScene().getWindow());
                            confirm.showAndWait().ifPresent(r -> {
                                if (r == ButtonType.OK) {
                                    try {
                                        boolean removed = StatusData.databaseHelper.removeQuestionFromFAQ(question.getQuestionId());
                                        if (removed) {
                                            showAlert(Alert.AlertType.INFORMATION, "Success", "Question removed from FAQ page.");
                                            loadAllQuestions();
                                        } else {
                                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove question from FAQ.");
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove from FAQ: " + ex.getMessage());
                                    }
                                }
                            });
                        } else {
                            // Reuse your dialog which on success calls markQuestionAsFAQ(...) and loadAllQuestions()
                            showMarkAsFAQDialog(question);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Could not determine FAQ state: " + ex.getMessage());
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                setText(null);

                System.out.println("FAQ updateItem: empty=" + empty + ", user=" + (user != null ? user.getName() : "NULL"));

                if (empty || user == null) {
                    System.out.println("  -> Returning: empty row or null user");
                    return;
                }

                Question question = getTableView().getItems().get(getIndex());
                if (question == null) {
                    System.out.println("  -> Returning: null question");
                    return;
                }

                System.out.println("  -> Q#" + question.getQuestionId() + " processing...");

                // Change here if you want only STAFF and ADMIN
                boolean isStaffOrAdmin = StatusData.DEV_MODE || (user.getRole() == User.Role.STAFF || user.getRole() == User.Role.ADMIN);
                
                System.out.println("  -> Role: " + user.getRole() + ", DEV_MODE: " + StatusData.DEV_MODE + ", isStaffOrAdmin: " + isStaffOrAdmin);

                if (!isStaffOrAdmin) {
                    System.out.println("  -> Returning: not staff or admin");
                    return;
                }

                // Safely check DB state
                boolean hasSolution = false;
                boolean isAlreadyFAQ = false;
                try {
                    hasSolution = StatusData.databaseHelper.questionHasSolution(question.getQuestionId());
                    isAlreadyFAQ = StatusData.databaseHelper.isQuestionMarkedAsFAQ(question.getQuestionId());
                    System.out.println("  -> hasSolution: " + hasSolution + ", isAlreadyFAQ: " + isAlreadyFAQ);
                } catch (Exception ex) {
                    System.out.println("  -> Exception: " + ex.getMessage());
                    ex.printStackTrace();
                    markFAQBtn.setTooltip(new Tooltip("Error checking FAQ state"));
                    return;
                }

                // Only show the control if there's a solution (per your requirement)
                if (!hasSolution) {
                    System.out.println("  -> Returning: no solution");
                    return;
                }

                System.out.println("  -> SUCCESS! Setting button graphic");

                if (isAlreadyFAQ) {
                    markFAQBtn.setText("Remove FAQ");
                    markFAQBtn.setStyle("-fx-background-color: #e91e63; -fx-text-fill: white;");
                } else {
                    markFAQBtn.setText("Mark as FAQ");
                    markFAQBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white;");
                }

                setGraphic(actionBox);
            }
        });

        questionTable.getColumns().addAll(idCol, authorCol, titleCol, statusCol, dateCol, 
                                          actionCol, clarifyCol, editCol, faqCol);


        questionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) displayQuestionDetails(newVal);
        });

        // Add header and table to layout (ONLY ONCE)
        tableBox.getChildren().addAll(tableHeader, questionTable);

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
        
        Button suggestClarificationBtn = new Button("Suggest Clarification");
        suggestClarificationBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        suggestClarificationBtn.setOnAction(event -> {
        	Question selected = questionTable.getSelectionModel().getSelectedItem();
        	if (selected == null) {
        		showAlert(Alert.AlertType.INFORMATION, "No Question Selected", "Please selected a question.");
        		return;
        	}
        	showClarificationPopup(selected);
        });
        
        Button followUpBtn = new Button("Ask FollowUp");
        followUpBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        followUpBtn.setOnAction(e -> {
        	Question selected = questionTable.getSelectionModel().getSelectedItem();
        	@SuppressWarnings("unused")
			int qId = selected.getQuestionId();
        	if (selected == null) {
        		showAlert(Alert.AlertType.INFORMATION, "No Question Selected", "Please selected a question.");
        		return;
        	}
        	//FollowUpQ followUpPopup = new FollowUpQ(user, questions, qId;
            showFollowUpPopup(selected);
        });
        
        Button privateMsgBtn = new Button("Private Message Author");
        privateMsgBtn.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: black; -fx-font-weight: bold;");
        
        privateMsgBtn.setOnAction(e -> {
        	Question selected = questionTable.getSelectionModel().getSelectedItem();
        	if (selected == null) {
        		showAlert(Alert.AlertType.INFORMATION, "No Question Selected", "Please select a question.");
        		return;
        	}
        	
        	int authorId = selected.getUserId();
        	System.out.println("Selected question user_id: " + authorId);
        	User author; 
        	System.out.println("Selected Question ID: " + selected.getQuestionId());
            System.out.println("Extracted authorId from question: " + authorId);
        	
        	try {
        	   author = StatusData.databaseHelper.getUserById(authorId);
        	   
        	   if (author == null) {
	           		showAlert(Alert.AlertType.ERROR, "Author Not Found", "Author could not be found.");
	           		return;
           		}
           	
        	} catch (SQLException ex) {
        	    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not retrieve author: " + ex.getMessage());
        	    ex.printStackTrace(); // optional: for debugging
        	    return;
        	}
        	
        	
        	
        	TextInputDialog dialog = new TextInputDialog();
        	dialog.setTitle("Send Private Message");
        	dialog.setHeaderText("Send a private message to: " + author.getUserName());
        	dialog.setContentText("Enter your message:");
        	
        	dialog.initOwner(stage);
        	dialog.showAndWait().ifPresent(messageText -> {
        		if (messageText.trim().isEmpty()) {
        			showAlert(Alert.AlertType.WARNING, "Empty Message", "Message cannot be empty.");
        			return;
        		}
        		
        		Messages message = new Messages(user.getId(), author.getId(), messageText);
        		message.setSenderName(user.getUserName());
        		message.setRecipientName(author.getUserName());
        		StatusData.databaseHelper.sendMessage(message);
        		showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent to " + author.getUserName());
        	});
        });
        
        HBox buttonBox = new HBox(10, suggestClarificationBtn, followUpBtn, privateMsgBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));
        
        detailsBox.getChildren().addAll(tabPane, buttonBox);
        
     // Combine all sections
        VBox mainContent = new VBox(15);
        mainContent.getChildren().addAll(createQuestionBox, tableBox, detailsBox, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        content.setCenter(scrollPane);
        mainPane.setCenter(content);

        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
        
        questionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        	if (newVal != null) {
        		displayQuestionDetails(newVal);
        		loadSuggestionsForQs(newVal, suggestionList);
        	}
        });
        
 
        loadAllQuestions();
        questionCount.setText(questionTable.getItems().size() + " total");
        
    }

    private void showMarkAsFAQDialog(Question question) throws SQLException {
        // Check if already marked as FAQ
        boolean isAlreadyFAQ = StatusData.databaseHelper.isQuestionMarkedAsFAQ(question.getQuestionId());
        
        if (isAlreadyFAQ) {
            // Show confirmation to remove from FAQ
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Remove from FAQ");
            confirmAlert.setHeaderText("This question is currently marked as FAQ");
            confirmAlert.setContentText("Do you want to remove it from the FAQ page?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean removed = StatusData.databaseHelper.removeQuestionFromFAQ(question.getQuestionId());
                    if (removed) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", 
                                 "Question removed from FAQ page.");
                        loadAllQuestions(); // Refresh to update button
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", 
                                 "Failed to remove question from FAQ.");
                    }
                }
            });
            return;
        }
        
        // Check if question has a solution
        boolean hasSolution = StatusData.databaseHelper.questionHasSolution(question.getQuestionId());
        
        if (!hasSolution) {
            showAlert(Alert.AlertType.WARNING, "Cannot Mark as FAQ", 
                     "Only questions with a marked solution can be added to FAQ.");
            return;
        }
        
        // Create dialog for marking as FAQ
        Stage dialog = new Stage();
        dialog.setTitle("Mark Question as FAQ");
        dialog.initOwner(stage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Add to FAQ: " + question.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
     // show all solutions preview (multiple answers allowed)
        List<Answer> solutions = StatusData.databaseHelper.getSolutionsForQuestion(question.getQuestionId());
        if (solutions != null && !solutions.isEmpty()) {
            Label solHeader = new Label("Solution(s):");
            solHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            layout.getChildren().add(solHeader);

            for (Answer sol : solutions) {
                Label solBy = new Label("By " + (sol.getAuthor() != null ? sol.getAuthor() : "Unknown") + ":");
                solBy.setStyle("-fx-font-weight: bold; -fx-padding: 6 0 0 0;");

                TextArea solutionContent = new TextArea(sol.getContent());
                solutionContent.setEditable(false);
                solutionContent.setWrapText(true);
                solutionContent.setPrefRowCount(Math.min(6, Math.max(3, sol.getContent().split("\n").length)));
                solutionContent.setStyle("-fx-background-color: #f9f9f9;");
                solutionContent.setMaxWidth(380);

                layout.getChildren().addAll(solBy, solutionContent);
            }
            layout.getChildren().add(new Separator());
        }

        
        // Category selection
        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "General",
            "Technical",
            "Account",
            "Features",
            "Troubleshooting",
            "Other"
        );
        categoryCombo.setValue("General");
        categoryCombo.setEditable(true);
        categoryCombo.setPrefWidth(300);
        
        // Display title (optional override)
        Label displayTitleLabel = new Label("Display Title (leave blank to use original):");
        TextField displayTitleField = new TextField();
        displayTitleField.setPromptText(question.getTitle());
        displayTitleField.setPrefWidth(300);
        
        // Staff notes
        Label notesLabel = new Label("Staff Notes (optional):");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Add any notes about why this is an FAQ...");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        notesArea.setPrefWidth(300);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        Button markBtn = new Button("Mark as FAQ");
        markBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-font-weight: bold;");
        
        buttonBox.getChildren().addAll(cancelBtn, markBtn);
        
        layout.getChildren().addAll(
            titleLabel,
            categoryLabel, categoryCombo,
            displayTitleLabel, displayTitleField,
            notesLabel, notesArea,
            buttonBox
        );
        
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        Scene scene = new Scene(scrollPane, 400, 550);
        dialog.setScene(scene);
        
        // Button actions
        cancelBtn.setOnAction(e -> dialog.close());
        
        markBtn.setOnAction(e -> {
            String category = categoryCombo.getValue();
            String displayTitle = displayTitleField.getText().trim();
            String notes = notesArea.getText().trim();
            
            if (category == null || category.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Category", 
                         "Please select a category.");
                return;
            }
            
            // Use original title if no override provided
            if (displayTitle.isEmpty()) {
                displayTitle = question.getTitle();
            }
            
            // Create FAQ object
            FAQ faq = new FAQ(
                question.getQuestionId(),
                category,
                displayTitle,
                notes,
                user.getId()
            );
            
            // Save to database
            boolean success = StatusData.databaseHelper.markQuestionAsFAQ(faq, user);

            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Question has been added to the FAQ page!");
                dialog.close();
                loadAllQuestions(); // Refresh table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to mark question as FAQ. Please try again.");
            }
        });
        
        dialog.show();
    }
    
	@SuppressWarnings("unused")
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
        grid.add(descriptionArea, 0, 3, 2, 1);

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
            loadAllQuestions();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
    }


    private void loadAllQuestions() {
        List<Question> allQuestions = StatusData.databaseHelper.loadAllQs();
        questionTable.getItems().setAll(allQuestions);
    }

    public void reloadQuestions() {
        if (questionTable != null) {
            loadAllQuestions();
        }
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
    
    @SuppressWarnings("unused")
	private void showClarificationPopup(Question question) {
		Stage popup = new Stage();
		popup.setTitle("Suggest Clarification");
		
		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER_LEFT);
		
		Label titleQ = new Label("Suggest Clarification for Question # " + question.getQuestionId());
		titleQ.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		
		//Label titleA = new Label("Suggest Clarification for Answer # " + answer.getAnswerId());
		//titleQ.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		
		TextArea input = new TextArea();
		input.setPromptText("Enter your suggestion here...");
		input.setWrapText(true);
		input.setPrefRowCount(5);
		input.setPrefWidth(350);
		
		Button submitButton = new Button("Submit");
		submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
		Button cancelButton = new Button("Cancel");
		
		HBox buttonBox = new HBox(10, cancelButton, submitButton);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		
		layout.getChildren().addAll(titleQ, input, buttonBox);
		
		Scene scene = new Scene(layout, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
		popup.setScene(scene);
		popup.show();
		
		cancelButton.setOnAction(e -> popup.close());
		
		submitButton.setOnAction(e -> {
			String content = input.getText();
			if (content.isEmpty()) {
				showAlert(Alert.AlertType.WARNING, "Empty Suggestion", "You must enter a suggestion before submitting");
				return;
			}
			
			int questionId = question.getQuestionId();
			int recipientId = question.getUserId();
			
			try {
				ClarificationsManager clarifications = new ClarificationsManager(StatusData.databaseHelper);
				Result result = clarifications.create(
					    questionId,
					    user.getId(),
					    recipientId,
					    user.getUserName(),
					    content
				);
				
				if (result.isSuccess()) {
					showAlert(Alert.AlertType.INFORMATION, "Success", "Suggestion submitted successfully!");
					popup.close();
				}
				else {
					showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
				}
			} catch (SQLException ev) {
			    ev.printStackTrace();
			    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save clarification: " + ev.getMessage());
			}

		});

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
    
    @SuppressWarnings("unused")
	private void showFollowUpPopup(Question question) {
        Stage popup = new Stage();
        popup.setTitle("Ask a Follow-Up Question");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Following up on Question # " + question.getQuestionId());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextField titleField = new TextField();
        titleField.setPromptText("Enter a title for your follow-up...");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Describe your follow-up question...");
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(5);
        descriptionField.setPrefWidth(350);

        Button submitButton = new Button("Post Follow-Up");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10, cancelButton, submitButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        layout.getChildren().addAll(title, new Label("Title:"), titleField,
                                    new Label("Description:"), descriptionField, buttonBox);

        Scene scene = new Scene(layout, 450, 300);
        popup.setScene(scene);
        popup.show();

        cancelButton.setOnAction(e -> popup.close());

        submitButton.setOnAction(e -> {
            String followUpTitle = titleField.getText().trim();
            String followUpDesc = descriptionField.getText().trim();

            if (followUpTitle.isEmpty() || followUpDesc.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please enter both title and description.");
                return;
            }

            question.setFollowUp(question.getQuestionId());  // Set the follow-up reference before saving

            Result result = questions.create(
                user.getId(),
                user.getName(),
                followUpTitle,
                followUpDesc,
                null
            );

            if (result.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Follow-up question posted successfully!");
                popup.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
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
    // Helper method to create styled buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                       "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                       "-fx-background-radius: 3;");
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                           "-fx-background-radius: 3;"));
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                           "-fx-background-radius: 3;"));
        return button;
    }
    
    // Helper method to create styled menu buttons - NOW WITH WHITE TEXT
    private MenuButton createStyledMenuButton(String text) {
        MenuButton menuButton = new MenuButton(text);
        menuButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                           "-fx-background-radius: 3;");
        menuButton.setOnMouseEntered(e -> 
            menuButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                               "-fx-background-radius: 3;"));
        menuButton.setOnMouseExited(e -> 
            menuButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-padding: 5 15 5 15; " +
                               "-fx-background-radius: 3;"));
        return menuButton;
    }
   
}