package pages;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.Result;
import logic.SearchFunction;
import logic.StatusData;
import model.Answers;
import model.Question;
import model.Questions;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import databasePart1.*;

public class MyPostsPage extends Application {
	private DatabaseHelper db;
	private Questions questions = StatusData.questions;
	private final Answers answers = StatusData.answers;
	private final String userName = StatusData.currUser.getName();
	
	@Override
	public void start(Stage stage) {
		db = new DatabaseHelper();
		try {
			db.connectToDatabase();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Failed to connect to db");
		}
		
		questions = new Questions(db);
		stage.setTitle("My Posts");
		
		Label keywordSearchLabel = new Label("Search by Keyword:");
		TextField keywordField = new TextField();
		keywordField.setPromptText("Enter keyword...");
		
		Label filterLabel = new Label("Filter");
		ComboBox<String> filterBox = new ComboBox<>();
		filterBox.getItems().addAll("All", "Resolved", "Unresolved", "Recent");
		filterBox.setValue("All");
		
		Button searchButton = new Button("Search");
		
		ListView<String> resultList = new ListView<>();
		
		SearchFunction searchFunction = new SearchFunction(questions, answers);
		
		searchButton.setOnAction(e -> {
			String keyword = keywordField.getText().toLowerCase();
			String filter = filterBox.getValue();
			
			List<Question> filtered = searchFunction.searchQuestions(keyword, filter, userName);
			
			 resultList.getItems().clear();
	            for (Question q : filtered) {
	                resultList.getItems().add(q.getTitle() + " (" + q.getStatus() + ")");
	            }
        });
		
		//-------UI for posting questions
		Label askLabel = new Label("Ask a New Question");
		
		Label titleLabel = new Label("Title *");
		TextField titleField = new TextField();
		titleField.setPromptText("Enter a title for your question");
		HBox titleBox = new HBox(8, titleLabel, titleField);
		
		Label bodyLabel = new Label("Description *");
		TextArea bodyArea = new TextArea();
		bodyArea.setPromptText("Ask your question here...");
		VBox bodyBox = new VBox(4, bodyLabel, bodyArea);
		
		Label tagLabel = new Label("Add tags separated with commas");
		TextField tagsField = new TextField();
		tagsField.setPromptText("Comma-separated tags (optional)");
		
		Button askButton = new Button("Post Question");
		Label askMsg = new Label();
		
		searchButton.fire();
		
		VBox askQuestionBox = new VBox(20, 
				titleBox,
				bodyBox,
				tagsField,
				askButton,
				askMsg
		);
		askQuestionBox.setPadding(new javafx.geometry.Insets(20));
		
		askQuestionBox.setVisible(false);
		ScrollPane askBoxScroll = new ScrollPane(askQuestionBox);
		askBoxScroll.setFitToWidth(true);
		askBoxScroll.setVisible(false);

		
		
		//toggle ask box
		Button toggleAskFormButton = new Button("Ask a Question");
		toggleAskFormButton.setOnAction(_ -> {
			askBoxScroll.setVisible(!askBoxScroll.isVisible());
			askQuestionBox.setVisible(!askQuestionBox.isVisible());
		});
		
		askButton.setOnAction(_ -> {
			String title = titleField.getText();
			String description = bodyArea.getText().trim();
			String tagInput = tagsField.getText();
			
			if (title.isEmpty()) {
				askMsg.setText("Title cannot be empty.");
				return;
			}
			if (description.isEmpty()) {
				askMsg.setText("Question cannot be blank.");
				return;
			}
			//convert tags to strings
			List<String> tags = tagInput.isEmpty() ? null : List.of(tagInput.split("\\s*,\\s*"));
			
			//call questions create method
			Result result = questions.create(-1, userName, title, description, tags);
			
			if (result.isSuccess()) {
				askMsg.setText(result.getMessage());
				titleField.clear();
				bodyArea.clear();
				tagsField.clear();
				//refresh search results
				searchButton.fire();	
			}
			else {
				askMsg.setText("Question failed to post: " + result.getMessage());
			}
		});
		
		VBox inputs = new VBox(10, keywordSearchLabel, keywordField, filterLabel, filterBox, searchButton);
		VBox topSection = new VBox(20, inputs, resultList, toggleAskFormButton, askBoxScroll);
		topSection.setPadding(new javafx.geometry.Insets(20));
		
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(topSection, askBoxScroll);
		splitPane.setDividerPositions(0.5);

		stage.setScene(new Scene(splitPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT));
		stage.setTitle("My Posts");
		stage.show();
	}

}