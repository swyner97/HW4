package pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;
import logic.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewPage - shows either:
 *  - "My Reviews" (show(stage,user)) OR
 *  - "Reviews for a selected Answer" (showForAnswer(stage,user,answer))
 *
 */
public class ReviewPage {

   
    private Reviews reviewsManager;

    private TableView<Review> reviewTable;
    private TextArea reviewDetails;
    private Object parentPage;

    private User user;

	protected Stage stage;

    public ReviewPage(Object parentPage) {
        this.parentPage = parentPage;
    }

    public void show(Stage stage, User user) {
  
        this.user = user;
        this.stage = stage; 
        this.reviewsManager = new Reviews(StatusData.databaseHelper);

        stage.setTitle("My Reviews");

        BorderPane root = buildBaseLayout();
        Label title = new Label("My Reviews");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox tableBox = new VBox(10, title, reviewTable);
        tableBox.setPadding(new Insets(10));

        VBox detailsBox = new VBox(10, buildDetailsTabPane());
        detailsBox.setPadding(new Insets(10, 0, 0, 0));

        VBox mainContent = new VBox(15, tableBox, detailsBox);
        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.setCenter(scroll);

        stage.setScene(new Scene(root, 900, 700));
        stage.show();

        loadUserReviews();
    }

    @SuppressWarnings("unused")
	public void showForAnswer(Stage stage, User user, Answer answer) {
        this.user = user;
        this.stage = stage; // Added: Store stage reference
        this.reviewsManager = new Reviews(StatusData.databaseHelper);

        stage.setTitle("Reviews for Answer #" + answer.getAnswerId());

        BorderPane root = buildBaseLayout();
        Label title = new Label("Reviews for Answer #" + answer.getAnswerId());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button backBtn = new Button("← Back to Answers");
        backBtn.setOnAction(e -> {
            if (parentPage instanceof AnswersPage) {
                ((AnswersPage) parentPage).show(stage, user);
            } else if (parentPage instanceof WelcomeLoginPage) {
                ((WelcomeLoginPage) parentPage).show(stage, user);
            }
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, backBtn, spacer, title);
        header.setPadding(new Insets(10));

        // ---- Submit Review Section ----
        VBox submitSection = new VBox(10);
        submitSection.setPadding(new Insets(10, 0, 10, 0));
        submitSection.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 12; -fx-border-color: #4caf50; -fx-border-width: 1;");

        Label submitTitle = new Label("Post a Review for this Answer");
        submitTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TextArea reviewInput = new TextArea();
        reviewInput.setPromptText("Write your review here...");
        reviewInput.setWrapText(true);
        reviewInput.setPrefRowCount(4);

        HBox submitButtonBox = new HBox(10);
        submitButtonBox.setAlignment(Pos.CENTER_RIGHT);

        Button postReviewBtn = new Button("Post Review");
        postReviewBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");

        postReviewBtn.setOnAction(ev -> {
            String content = reviewInput.getText().trim();

            if (content.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please write a review before submitting.", ButtonType.OK);
                a.showAndWait();
                return;
            }

            Result createRes = reviewsManager.create(user.getId(), answer.getAnswerId(), user.getName(), content);

            if (!createRes.isSuccess()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create review: " + createRes.getMessage());
                return;
            }

            // Refresh and confirm
            loadReviewsForAnswer(answer.getAnswerId());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your review has been posted.");
            reviewInput.clear();
        });

        submitButtonBox.getChildren().add(postReviewBtn);
        submitSection.getChildren().addAll(submitTitle, reviewInput, submitButtonBox);


        VBox tableBox = new VBox(10, header, submitSection, reviewTable);
        tableBox.setPadding(new Insets(10));

        VBox detailsBox = new VBox(10, buildDetailsTabPane());
        detailsBox.setPadding(new Insets(10, 0, 0, 0));

        VBox mainContent = new VBox(15, tableBox, detailsBox);
        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.setCenter(scroll);

        stage.setScene(new Scene(root, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT));
        stage.show();

        loadReviewsForAnswer(answer.getAnswerId());
    }

    /* ----------------- layout & helpers ----------------- */

    @SuppressWarnings({ "unchecked", "unused" })
	private BorderPane buildBaseLayout() {
        BorderPane mainPane = new BorderPane();

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Table
        reviewTable = new TableView<>();
        reviewTable.setPrefHeight(300);

        TableColumn<Review, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        idCol.setPrefWidth(60);

        TableColumn<Review, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(120);

        TableColumn<Review, String> contentCol = new TableColumn<>("Content Preview");
        contentCol.setCellValueFactory(cell -> {
            String content = cell.getValue().getContent();
            String preview = content == null ? "" : (content.length() > 70 ? content.substring(0, 70) + "..." : content);
            return new javafx.beans.property.SimpleStringProperty(preview);
        });
        contentCol.setPrefWidth(600);

        // Edit column
        TableColumn<Review, Void> actionCol = new TableColumn<>("Edit");
        actionCol.setPrefWidth(80);

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final HBox actionBox = new HBox(5, editBtn);

            {
                // Button style
                editBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");

                // Click action
                editBtn.setOnAction(event -> {
                    Review review = getTableView().getItems().get(getIndex());
                    ReviewPage.this.showEditReviewPage(review);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Review review = getTableView().getItems().get(getIndex());
                    // Only show edit button if current user is the author
                    if (review != null && review.getAuthor().equals(user.getName())) {
                        setGraphic(actionBox);
                    } else {
                        setGraphic(null); // Hide button for reviews by other users
                    }
                }
            }
        });
        
        reviewTable.getColumns().addAll(idCol, authorCol, contentCol, actionCol);

        reviewTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) displayReviewDetails(n);
        });

        return mainPane;
    }


    private TabPane buildDetailsTabPane() {
        reviewDetails = new TextArea();
        reviewDetails.setEditable(false);
        reviewDetails.setWrapText(true);
        reviewDetails.setPrefRowCount(8);
        reviewDetails.setPromptText("Select a review to view details...");

        ScrollPane detailsScroll = new ScrollPane(reviewDetails);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background-color: transparent;");

        Tab detailsTab = new Tab("Review Details", detailsScroll);

        ListView<String> suggestionList = new ListView<>();
        suggestionList.setPlaceholder(new Label("No suggestions yet."));
        Tab suggestionsTab = new Tab("View Suggestions", suggestionList);

        TabPane tp = new TabPane(detailsTab, suggestionsTab);
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tp.setPrefHeight(220);
        tp.setStyle("-fx-background-color: white; -fx-border-color: #2196f3; -fx-border-radius: 5; -fx-background-radius: 5;");

        return tp;
    }

    /* ----------------- data loading ----------------- */

    // filters reviews to show only those written by current user
    private void loadUserReviews() {
        this.reviewsManager = new Reviews(StatusData.databaseHelper);
        List<Review> all = reviewsManager.readAll();
        List<Review> mine = all.stream()
                .filter(r -> r.getAuthor().equals(user.getName()))
                .collect(Collectors.toList());
        reviewTable.getItems().setAll(mine);
    }
    
    
    // shows all reviews for a specific answer
    private void loadReviewsForAnswer(int answerId) {
        this.reviewsManager = new Reviews(StatusData.databaseHelper);
        List<Review> list = reviewsManager.search(answerId, "", null); // returns reviews for that answer
        reviewTable.getItems().setAll(list);
    }
    
    public void reloadReviews() {
        if (reviewTable != null) {
        	loadUserReviews();
        }
    }

    private void displayReviewDetails(Review r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Review ID: ").append(r.getReviewId()).append("\n");
        sb.append("Answer ID: ").append(r.getAnswerId()).append("\n");
        sb.append("Author: ").append(r.getAuthor()).append("\n\n");
        sb.append("--- Full Review ---\n\n");
        sb.append(r.getContent());
        reviewDetails.setText(sb.toString());
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    
    private void showEditReviewPage(Review review) {
        EditReviewPage editPage = new EditReviewPage(this, review, reviewsManager); 
        editPage.show(stage, user);
    }
}