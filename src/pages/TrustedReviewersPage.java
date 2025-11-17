package pages;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import logic.StatusData;
import model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * JavaFX GUI page that allows students to manage and rate their 
 * trusted reviewers. Integrates with {@link databasePart1.DatabaseHelper}
 * for database operations and displays ratings and associated reviews.
 */
public class TrustedReviewersPage {
	
	private Stage stage;
	private User user;
	
	private TextField addUserField;
	private TableView<User> allReviewersTable = new TableView<>();
	private TableView<TrustedReviewerRow>  trustedTable = new TableView<>();
	private List<Integer> trustedIds = new ArrayList<>();
	private TableView<TrustedReviewDisplayRow> trustedReviewsTable = new TableView<>();
	
	public void show(Stage stage) {
		this.stage = stage;
		this.user = StatusData.currUser;

		stage.setTitle("Trusted Reviewers");
		
		BorderPane mainPane = new BorderPane();
		
		// Navigation Bar
		NavigationBar navBar = new NavigationBar();
		mainPane.setTop(navBar);
		
		// Center content
		BorderPane content = new BorderPane();
		content.setPadding(new Insets(15));
		
		// Left: Manage Trusted Reviewers
		VBox manageBox = new VBox(6);
		manageBox.setPadding(new Insets(6, 10, 6, 10));
		manageBox.setStyle(
				"-fx-background-color: #e3f2fd; " +
				"-fx-border-color: #2196f3; -fx-border-width: 1; " +
				"-fx-border-radius: 5; -fx-background-radius: 5;"
		);
		manageBox.setMaxWidth(Double.MAX_VALUE);
		
		Label manageTitle = new Label("Manage Trusted Reviewers");
		manageTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		
		Label addLabel = new Label("Search reviewer:");
		addUserField = new TextField();
		addUserField.setPromptText("Enter reviewer username...");
		addUserField.setPrefWidth(220);
		
		Button addButton = new Button("Add");
		addButton.setOnAction(_ -> onAddTrustedReviewer());
		
		HBox addRow = new HBox(8, addLabel, addUserField, addButton);
		addRow.setAlignment(Pos.CENTER_LEFT);
		
		allReviewersTable.setPlaceholder(new Label("No reviewers found."));
		allReviewersTable.setPrefHeight(160);
		allReviewersTable.setMaxHeight(Region.USE_PREF_SIZE);
		allReviewersTable.setFixedCellSize(30);
		
		TableColumn<User, String> tUserCol = new TableColumn<>("Username");
		tUserCol.setPrefWidth(100);
		tUserCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
		
		TableColumn<User, String> tNameCol = new TableColumn<>("Name");
		tNameCol.setPrefWidth(120);
		tNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		TableColumn<User, Void> tActionCol = new TableColumn<>("Action");
		tActionCol.setPrefWidth(65);
		tActionCol.setCellFactory(col -> new TableCell<>() {
			private final Button addBtn = new Button("Add");
	        private final Button removeBtn = new Button("Remove");
	        private final HBox box = new HBox(5, addBtn, removeBtn);
	        
	        {
	        	addBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white");
	        	removeBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");
	        	
	        	addBtn.setOnAction(e -> {
	        		User reviewer = getTableView().getItems().get(getIndex());

	        		try {
	        			boolean added = StatusData.databaseHelper.addTrustedReviewer(user.getId(), reviewer.getId());
	        			if (added) {
	        				StatusData.databaseHelper.updateTrustedReviewerRating(user.getId(), reviewer.getId(), 3);
	        			}
	        			reloadTrustedData();
	        		} catch (Exception ex) {
	        			ex.printStackTrace();
	        			showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
	        		}
	        	});
	        	
	        	removeBtn.setOnAction(e -> {
	                User reviewer = getTableView().getItems().get(getIndex());
	                try {
	                    StatusData.databaseHelper.removeTrustedReviewer(user.getId(), reviewer.getId());
	                    reloadTrustedData();
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                    showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
	                }
	            });
	        }
	        @Override
	        protected void updateItem(Void item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty) {
	            	setGraphic(null);
	            	return;
	            }
	            User reviewer = getTableView().getItems().get(getIndex());
	            boolean isTrusted = trustedIds.contains(reviewer.getId());
	            
	            addBtn.setVisible(!isTrusted);
	            addBtn.setManaged(!isTrusted);
	            removeBtn.setVisible(isTrusted);
	            removeBtn.setManaged(isTrusted);
	            
	            setGraphic(box);
	        }
	    });
		
		allReviewersTable.getColumns().addAll(tUserCol, tNameCol, tActionCol);
		manageBox.getChildren().addAll(manageTitle, addRow, allReviewersTable);
		
		// Right: Rating Panel
		VBox ratingBox = new VBox(6);
		ratingBox.setPadding(new Insets(6, 10, 6, 10));
		ratingBox.setStyle(
				"-fx-background-color: white; " +
				"-fx-border-color: #2196f3; -fx-border-width: 1; " +
				"-fx-border-radius: 5; -fx-background-radius: 5; "
		);
		ratingBox.setMaxWidth(Double.MAX_VALUE);
		
		Label ratingTitle = new Label("My Trusted Reviewers");
		ratingTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		
		trustedTable.setEditable(true);
		trustedTable.setPlaceholder(new Label("No trusted reviewers yet."));
		trustedTable.setPrefHeight(190);
		trustedTable.setMaxHeight(Region.USE_PREF_SIZE);
		trustedTable.setFixedCellSize(30);
		
		TableColumn<TrustedReviewerRow, String> rUserCol = new TableColumn<>("Username");
		rUserCol.setPrefWidth(150);
		rUserCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
		
		TableColumn<TrustedReviewerRow, String> rNameCol = new TableColumn<>("Name");
		rNameCol.setPrefWidth(160);
		rNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		TableColumn<TrustedReviewerRow, Integer> ratingCol = new TableColumn<>("Rating");
		ratingCol.setPrefWidth(65);
		ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
		
		ratingCol.setCellFactory(col -> new TableCell<>() {
		    private final ComboBox<Integer> combo = new ComboBox<>();

		    {
		        combo.getItems().addAll(1, 2, 3, 4, 5);

		        combo.setOnAction(e -> {
		            TrustedReviewerRow row = getTableView().getItems().get(getIndex());
		            Integer newRating = combo.getValue();
		            if (row != null && newRating != null) {
		                row.setRating(newRating);
		                try {
		                    StatusData.databaseHelper.updateTrustedReviewerRating(
		                            user.getId(),
		                            row.getReviewerId(),
		                            newRating
		                    );
		                    loadTrustedReviews();
		                } catch (SQLException ex) {
		                    ex.printStackTrace();
		                    showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
		                }
		            }
		        });
		    }

		    @Override
		    protected void updateItem(Integer value, boolean empty) {
		        super.updateItem(value, empty);
		        if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
		            setGraphic(null);
		            return;
		        }

		        TrustedReviewerRow row = getTableView().getItems().get(getIndex());
		        // Show the current rating in the dropdown
		        int currentRating = row.getRating();
		        if (currentRating < 1 || currentRating > 5) {
		            currentRating = 3; // default if 0 / unset
		        }
		        combo.setValue(currentRating);

		        setGraphic(combo);
		    }
		});
		
		trustedTable.getColumns().setAll(rUserCol, rNameCol, ratingCol);
		ratingBox.getChildren().addAll(ratingTitle, trustedTable);
		VBox.setVgrow(trustedTable,  Priority.ALWAYS);
		
		// Bottom: Reviews from Trusted Reviewers
		Label trustedReviewsTitle = new Label("Reviews from Trusted Reviewers");
		trustedReviewsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");
		
		trustedReviewsTable.setPlaceholder(new Label("No reviews from trusted reviewers yet."));
		trustedReviewsTable.setMaxHeight(Double.MAX_VALUE);
		trustedReviewsTable.setPrefHeight(Region.USE_COMPUTED_SIZE);
		VBox.setVgrow(trustedReviewsTable,  Priority.ALWAYS);
		
		TableColumn<TrustedReviewDisplayRow, Integer> revIdCol = new TableColumn<>("Review ID");
		revIdCol.setPrefWidth(80);
		revIdCol.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
		
		TableColumn<TrustedReviewDisplayRow, String> revReviewerCol = new TableColumn<>("Reviewer");
		revReviewerCol.setPrefWidth(130);
		revReviewerCol.setCellValueFactory(new PropertyValueFactory<>("reviewerName"));
		
		 TableColumn<TrustedReviewDisplayRow, Integer> revRatingCol = new TableColumn<>("Rating");
		 revRatingCol.setPrefWidth(70);
		 revRatingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
		 
		 TableColumn<TrustedReviewDisplayRow, Integer> revAnswerIdCol = new TableColumn<>("Answer ID");
		 revAnswerIdCol.setPrefWidth(90);
		 revAnswerIdCol.setCellValueFactory(new PropertyValueFactory<>("answerId"));
		 
		 TableColumn<TrustedReviewDisplayRow, String> revContentCol = new TableColumn<>("Content Preview");
		 revContentCol.setPrefWidth(400);
		 revContentCol.setCellValueFactory(new PropertyValueFactory<>("contentPreview"));
		 
		 TableColumn<TrustedReviewDisplayRow, Void> revInfoCol = new TableColumn<>("Info");
		 revInfoCol.setPrefWidth(80);
		 
		 revInfoCol.setCellFactory(col -> new TableCell<>() {
			 private final Button infoButton = new Button("More Info");
			 
			 {
				 infoButton.setOnAction(e -> {
					 TrustedReviewDisplayRow row = getTableView().getItems().get(getIndex());
					 if (row != null) {
						 showTrustedReviewPopup(row);
					 }
				 });
				 infoButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
			 }
			 
			 @Override
			 protected void updateItem(Void item, boolean empty) {
				 super.updateItem(item,  empty);
				 if (empty) {
					 setGraphic(null);
				 } else {
					 setGraphic(infoButton);
				 }
			 }
		 });
		 
		 trustedReviewsTable.getColumns().setAll(revIdCol, revReviewerCol, revRatingCol, revAnswerIdCol, revContentCol, revInfoCol);
		 VBox reviewsSection = new VBox(6, trustedReviewsTitle, trustedReviewsTable);
		 reviewsSection.setPadding(new Insets(10, 15, 10, 15));
		 
		
		// Layout
		HBox topRow = new HBox(15, manageBox, ratingBox);
		HBox.setHgrow(manageBox, Priority.ALWAYS);
		HBox.setHgrow(ratingBox, Priority.ALWAYS);
		
		VBox centerContent = new VBox(12, topRow, reviewsSection);
		centerContent.setPadding(new Insets(10));
		VBox.setVgrow(centerContent, Priority.NEVER);
		
		ScrollPane scrollPane = new ScrollPane(centerContent);
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background-color: transparent;");
		
		content.setCenter(scrollPane);
		mainPane.setCenter(content);
		
		Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
		stage.setScene(scene);
		stage.show();
		
		reloadTrustedData();
		loadTrustedReviews();
	}
	
	private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
	
	private void onAddTrustedReviewer() {
		String searchText = addUserField.getText() == null ? "" : addUserField.getText().trim();
		if(searchText.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Validation", "Please enter a reviewer username.");
			return;
		}
		try {
			List<User>reviewers = StatusData.databaseHelper.getUsersByRole(User.Role.REVIEWER);
			
			String lower = searchText.toLowerCase();
			List<User> matches = reviewers.stream()
					.filter(u -> {
						String un = u.getUserName() == null ? "" : u.getUserName().toLowerCase();
						String nm = u.getName() == null ? "" : u.getName().toLowerCase();
						return un.contains(lower) || nm.contains(lower);
					}).toList();
			
			if (matches.isEmpty()) {
				showAlert(Alert.AlertType.INFORMATION, "No Match", "No reviewers found matching: \"" + searchText + "\"");
				return;
			}
			
			matches = matches.stream()
					.filter(u -> u.getId() != user.getId()).toList();
			
			if (matches.isEmpty()) {
				showAlert(Alert.AlertType.ERROR, "Invalid", "You cannot add yourself as a trusted reviewer.");
				return;
			}
			
			if (matches.size() == 1) {
				User reviewer = matches.get(0);
				String namePart = reviewer.getName() != null && !reviewer.getName().isEmpty()
						? reviewer.getName() + " (" + reviewer.getUserName() + ")"
						: reviewer.getUserName();
				
				Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
				confirm.setTitle("Confirm Trusted Reviewer");
				confirm.setHeaderText("Add Trusted Reviewer");
				confirm.setContentText("Do you want to add " + namePart + " as a trusted reviewer?");
				
				var result = confirm.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					boolean added = StatusData.databaseHelper.addTrustedReviewer(user.getId(), reviewer.getId());
					if (added) {
						StatusData.databaseHelper.updateTrustedReviewerRating(user.getId(), reviewer.getId(), 3);
						addUserField.clear();
						reloadTrustedData();
						showAlert(Alert.AlertType.INFORMATION, "Success", "Added " + namePart + " to your trusted reviewers.");
					} else {
						showAlert(Alert.AlertType.INFORMATION, "No Change", namePart + " is already in your trusted reviewers.");
					}
				}
			} else {
				showReviewerSelectionPopup(matches);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
		}
	}
	
	// Logic helper for testing
	public static List<User> filterReviewerMatches(List<User> reviewers, String searchText, int currentUserId) {
		if (searchText == null) searchText = "";
	    String lower = searchText.toLowerCase().trim();

	    return reviewers.stream()
	            .filter(u -> u.getId() != currentUserId)
	            .filter(u -> {
	                String un = u.getUserName() == null ? "" : u.getUserName().toLowerCase();
	                String nm = u.getName() == null ? "" : u.getName().toLowerCase();
	                return un.contains(lower) || nm.contains(lower);
	            })
	            .toList();
	}
	
	private void reloadTrustedData() {
		try {
			List<User> reviewers = StatusData.databaseHelper.getUsersByRole(User.Role.REVIEWER);
			allReviewersTable.getItems().setAll(reviewers);
			
			var ratingsMap = StatusData.databaseHelper.getTrustedReviewerRatings(user.getId());
			trustedIds = new ArrayList<>(ratingsMap.keySet());
			
			List<TrustedReviewerRow> rows = new ArrayList<>();
			for (var entry : ratingsMap.entrySet()) {
				int reviewerId = entry.getKey();
				int rating = entry.getValue();
				
				User u = StatusData.databaseHelper.getUserById(reviewerId);
				if (u != null) {
					rows.add(new TrustedReviewerRow(
							reviewerId,
							u.getUserName(),
							u.getName(),
							rating
					));
				}
			}
			
			trustedTable.getItems().setAll(rows);
			loadTrustedReviews();
		} catch (Exception ex) {
			ex.printStackTrace();
			allReviewersTable.getItems().clear();
			trustedTable.getItems().clear();
		}
	}
	
	private void loadTrustedReviews() {
		try {
			var ratingsMap = StatusData.databaseHelper.getTrustedReviewerRatings(user.getId());
			if (ratingsMap == null || ratingsMap.isEmpty()) {
				trustedReviewsTable.getItems().clear();
				return;
			}
		
			Reviews reviewsManager = new Reviews(StatusData.databaseHelper);
			List<Review> allReviews = reviewsManager.readAll();
		
			List<TrustedReviewDisplayRow> rows = allReviews.stream()
					.filter(r -> ratingsMap.containsKey(r.getUserId()))
					.map(r -> {
						int rating = ratingsMap.getOrDefault(r.getUserId(), 0);
						String content = r.getContent();
						String preview = content == null ? "" : (content.length() > 70 ? content.substring(0,70) + "..." : content);
						
						return new TrustedReviewDisplayRow(
							r.getReviewId(),
							r.getAuthor(),
							rating,
							r.getAnswerId(),
							preview,
							content
						);
					})
					.sorted((a,b) -> Integer.compare(b.getRating(), a.getRating())).toList();
		
			trustedReviewsTable.getItems().setAll(rows);
		} catch (Exception ex) {
			ex.printStackTrace();
			trustedReviewsTable.getItems().clear();
		}
	}
	
	private void showTrustedReviewPopup(TrustedReviewDisplayRow row) {
		try {
			Answer answer = StatusData.databaseHelper.getAnswerById(row.getAnswerId());
			if (answer == null) {
				showAlert(Alert.AlertType.ERROR, "Not Found", "Could not find the answer for this review (Answer ID = " + row.getAnswerId() + ").");
				return;
			}
			
			Question question = StatusData.databaseHelper.getQuestionById(answer.getQuestionId());
			
			String qTitle = (question != null ? question.getTitle() : "(Unknown Question)");
			String answerText = answer.getContent();
			String reviewText = row.getFullContent();
			
			Dialog<Void> dialog = new Dialog<>();
			dialog.setTitle("Review Info");
			dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
			
			Label qLabel = new Label("Question Title:");
			Label aLabel = new Label("Answer:");
			Label rLabel = new Label("Review:");
			
			TextArea qArea = new TextArea(qTitle);
			qArea.setEditable(false);
	        qArea.setWrapText(true);
	        qArea.setPrefRowCount(2);

	        TextArea aArea = new TextArea(answerText);
	        aArea.setEditable(false);
	        aArea.setWrapText(true);
	        aArea.setPrefRowCount(5);

	        TextArea rArea = new TextArea(reviewText);
	        rArea.setEditable(false);
	        rArea.setWrapText(true);
	        rArea.setPrefRowCount(6);

	        GridPane grid = new GridPane();
	        grid.setHgap(10);
	        grid.setVgap(10);
	        grid.setPadding(new Insets(10));

	        grid.add(qLabel, 0, 0);
	        grid.add(qArea, 1, 0);
	        grid.add(aLabel, 0, 1);
	        grid.add(aArea, 1, 1);
	        grid.add(rLabel, 0, 2);
	        grid.add(rArea, 1, 2);

	        dialog.getDialogPane().setContent(grid);
	        dialog.initOwner(stage);
	        dialog.showAndWait();
	        
		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error", "Failed to load review details: " + ex.getMessage());
		}
	}
	
	public static class ReviewRow {
		private final String reviewerUserName;
		private final int rating;
		private final int answerId;
		private final String answerText;
		
		public ReviewRow(String reviewerUserName, int rating, int answerId, String answerText) {
			this.reviewerUserName = reviewerUserName;
			this.rating = rating;
			this.answerId = answerId;
			this.answerText = answerText;
		}
		
		public String getReviewerUserName() { return reviewerUserName; }
		public int getRating() { return rating; }
		public int getAnswerId() { return answerId; }
		public String getAnswerText() { return answerText; }
	}
	
	public static class TrustedReviewerRow {
		private final int reviewerId;
		private final String userName;
		private final String name;
		private int rating;
		
		public TrustedReviewerRow(int reviewerId, String userName, String name, int rating) {
			this.reviewerId = reviewerId;
			this.userName = userName;
			this.name = name;
			this.rating = rating;
		}
		
		public int getReviewerId() { return reviewerId; }
		public String getUserName() { return userName; }
		public String getName() { return name; }
		public int getRating() { return rating; }
		public void setRating(int rating) { this.rating = rating; }
	}
	
	public static class TrustedReviewDisplayRow {
		private final int reviewId;
		private final String reviewerName;
		private final int rating;
		private final int answerId;
		private final String contentPreview;
		private final String fullContent;
		
		public TrustedReviewDisplayRow(int reviewId, String reviewerName, int rating, int answerId, String contentPreview, String fullContent) {
			this.reviewId = reviewId;
			this.reviewerName = reviewerName;
			this.rating = rating;
			this.answerId = answerId;
			this.contentPreview = contentPreview;
			this.fullContent = fullContent;
		}
		
		public int getReviewId() { return reviewId; }
		public String getReviewerName() { return reviewerName; }
		public int getRating() { return rating; }
		public int getAnswerId() { return answerId; }
		public String getContentPreview() { return contentPreview; }
		public String getFullContent() {return fullContent; }
	}
	
	private void showReviewerSelectionPopup(List<User> matches) {
		Stage dialog = new Stage();
		dialog.initOwner(stage);
		dialog.setTitle("Select Reviewer to Add");
		
		Label prompt = new Label("Multiple reviewers matched your search. Please choose one to add:");
		prompt.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		
		TableView<User> table = new TableView<>();
		table.setPlaceholder(new Label("No reviewers."));
		table.setPrefHeight(200);
		table.setFixedCellSize(26);
		
		TableColumn<User, String> userCol = new TableColumn<>("Username");
		userCol.setPrefWidth(150);
		userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
		
		TableColumn<User, String> nameCol = new TableColumn<>("Name");
		nameCol.setPrefWidth(180);
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		TableColumn<User, Void> actionCol = new TableColumn<>("Action");
		actionCol.setPrefWidth(80);
		actionCol.setCellFactory(col -> new TableCell<>() {
			private final Button addBtn = new Button("Add");
			
			{
	            addBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
	            addBtn.setOnAction(e -> {
	                User reviewer = getTableView().getItems().get(getIndex());
	                if (reviewer == null) return;

	                if (reviewer.getId() == user.getId()) {
	                    showAlert(Alert.AlertType.ERROR, "Invalid", "You cannot add yourself as a trusted reviewer.");
	                    return;
	                }

	                try {
	                    boolean added = StatusData.databaseHelper.addTrustedReviewer(user.getId(), reviewer.getId());
	                    if (added) {
	                    	StatusData.databaseHelper.updateTrustedReviewerRating(user.getId(), reviewer.getId(), 3);
	                        reloadTrustedData();
	                        showAlert(Alert.AlertType.INFORMATION, "Success",
	                                "Added " + reviewer.getName() + " (" + reviewer.getUserName() + ") as trusted.");
	                        dialog.close();
	                    } else {
	                        showAlert(Alert.AlertType.INFORMATION, "No Change",
	                                "This reviewer is already in your trusted list.");
	                    }
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                    showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
	                }
	            });
	        }

	        @Override
	        protected void updateItem(Void item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty) {
	                setGraphic(null);
	            } else {
	                setGraphic(addBtn);
	            }
	        }
		});
		
		table.getColumns().setAll(userCol, nameCol, actionCol);
		table.getItems().setAll(matches);
		
		Button closeButton = new Button("Close");
		closeButton.setOnAction(e -> dialog.close());
		
		HBox buttonBar = new HBox(closeButton);
		buttonBar.setAlignment(Pos.CENTER_RIGHT);
		buttonBar.setPadding(new Insets(10, 0, 0, 0));
		
		VBox layout = new VBox(10, prompt, table, buttonBar);
		layout.setPadding(new Insets(10, 15, 10, 15));
		
		Scene scene = new Scene(layout, 450, 320);
		dialog.setScene(scene);
		dialog.show();
	}
}
