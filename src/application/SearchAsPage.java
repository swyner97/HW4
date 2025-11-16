package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class SearchAsPage {
	private final Answers answers = StatusData.answers;
	
	public void showSearchWindow() {
		//=======DUMMY Qs ==========
		/*
		questions.create(1, "Meg", "Eclipse", "Where do we download eclipse?", List.of("eclipse", "download"));
		questions.create(2, "Charles", "HW2 homework", "Is there an extension on HW2?", List.of("HW2", "due"));
		questions.create(3, "User1", "Github", "Does anyone have experience using github via eclipse?", List.of("github", "eclipse"));
		questions.create(4, "AviUser", "Exam", "Is there a final exam?", List.of("exam", "final"));
		questions.create(5, "User1", "Confused", "How do we know what to do?", null);
		questions.create(6, "James", "Homework 1", "How did everyone else do on HW1?", null);
		questions.create(7, "Meg", "CSE Majors", "Has anyone found a cheap place to purchase textbooks?", List.of("CSE", "textbooks"));
		*/
		//=======DUMMY As ==========
		/*answers.create(1, 1, "User1", "Look on Module 0 in canvas");
		answers.create(2, 1, "AviUser", "Go to this website: hyperlink");
		answers.create(3, 7, "Charles", "If you search for them in Bing you're more likely to find them");
		answers.create(4, 4, "User1", "The final exam is optional");
		answers.create(5, 5, "Meg", "Go to myasu, then canvas, then dashboard, then click on class cse360, then look at the pages there");
		answers.create(6, 5, "James", "The graders said it was confusing on purpose");
		answers.create(7, 2, "James", "There was an extension but it's past now");*/
		
		
		Stage popupAnsSearch = new Stage();
		popupAnsSearch.setTitle("Search Answers");
		
		Label questionIdLabel = new Label("Search by Question ID:");
		TextField questionIdField = new TextField();
		questionIdField.setPromptText("Enter question ID...");
		
		Label keywordLabel = new Label("Search by Keyword:");
		TextField keywordField = new TextField();
		keywordField.setPromptText("Enter keyword...");
		
		Label authorSearchLabel = new Label("Search by Author:");
		TextField authorField = new TextField();
		authorField.setPromptText("Enter author...");
		
		CheckBox resolveOnlyBox = new CheckBox("Only show solutions");
		
		Button searchButton = new Button("Search");
		ListView<String> resultList = new ListView<>();
		
		SearchFunction searchFunction = new SearchFunction(StatusData.questions, StatusData.answers);
		searchButton.setOnAction(e -> {
			resultList.getItems().clear();
			
			String keyword = keywordField.getText().toLowerCase();
			String author = authorField.getText();
			boolean solutionOnly = resolveOnlyBox.isSelected();
			
			List<Answer> results = searchFunction.searchAnswers(keyword, author, solutionOnly);

		    if (results.isEmpty()) {
		        resultList.getItems().add("No answers found.");
		    }
		    
			else {
				for (Answer a : results) {
					resultList.getItems().add(a.toString() + ": " + a.getContent());
				}
			}	
		});
		
		VBox inputFields = new VBox(8, 
				questionIdLabel, questionIdField,
				keywordLabel, keywordField,
				authorSearchLabel, authorField,
				resolveOnlyBox,
				searchButton
		);
		
		HBox topSection = new HBox(20, inputFields);
		VBox layout = new VBox(15, topSection, resultList);
		layout.setPadding(new javafx.geometry.Insets(20));
		
		Scene scene = new Scene(layout, 600, 400);
		popupAnsSearch.setScene(scene);
		popupAnsSearch.show();
	}
}
