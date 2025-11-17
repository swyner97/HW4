package pages;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.SearchFunction;
import logic.StatusData;
import model.Answers;
import model.Question;
import model.Questions;

import java.util.List;


public class SearchPage {
	private Questions questions = StatusData.questions;
	private Answers answers = StatusData.answers;
	
	public void showSearchWindow() {
		
		
		
		Stage popupSearch = new Stage();
		popupSearch.setTitle("Search Questions");
		
		Label keywordSearchLabel = new Label("Search by Keyword:");
		TextField keywordField = new TextField();
		keywordField.setPromptText("Enter keyword...");
		
		Label authorSearchLabel = new Label("Search by Author:");
		TextField authorField = new TextField();
		authorField.setPromptText("Enter author...");
		
		Label filterLabel = new Label("Filter");
		ComboBox<String> filterBox = new ComboBox<>();
		filterBox.getItems().addAll("All", "Resolved", "Open", "Recent");
		filterBox.setValue("All");
		
		Button searchButton = new Button("Search");
		
		ListView<String> resultList = new ListView<>();
		
		SearchFunction searchFunction = new SearchFunction(questions, answers);
		
		searchButton.setOnAction(e -> {
			String keyword = keywordField.getText().toLowerCase();
			String filter = filterBox.getValue();
			String author = authorField.getText().toLowerCase();
			
			List<Question> filtered = searchFunction.searchQuestions(keyword, filter, author);
	
			resultList.getItems().clear();
			
			if (filtered.isEmpty()) {
				resultList.getItems().add("No questions match your search.");
			}
			else {
				for (Question q : filtered) {
					resultList.getItems().add(
							q.getQuestionId() + ": " + q.getTitle() + " (" + q.getStatus() + ") "+ 
							"\n" + q.getDescription() + "\n"
					);
				}
			}
			
		});
		
		VBox inputFields = new VBox(8, 
				keywordSearchLabel, keywordField,
				authorSearchLabel, authorField,
				filterLabel, filterBox,
				searchButton
		);
		
		HBox topSection = new HBox(20, inputFields);
		VBox layout = new VBox(15, topSection, resultList);
		layout.setPadding(new javafx.geometry.Insets(20));
		
		Scene scene = new Scene(layout, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
		popupSearch.setScene(scene);
		popupSearch.show();
	}


}
