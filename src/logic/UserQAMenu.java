package logic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.NavigationBar;
import pages.MyPostsPage;
import pages.SearchAsPage;
import pages.SearchPage;

public class UserQAMenu extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Question & Answer Menu");
		
		Button searchQsButton = new Button("Search Questions");
		Button searchAsButton = new Button("Search Answers");
		Button searchMineButton = new Button("Search My Posts");
		
		searchQsButton.setOnAction(e -> {
			SearchPage searchQsPage = new SearchPage();
			searchQsPage.showSearchWindow();
		});
		
		searchAsButton.setOnAction(e -> {
			SearchAsPage searchAsPage = new SearchAsPage();
			searchAsPage.showSearchWindow();
		});
		
		searchMineButton.setOnAction(e -> {
			MyPostsPage searchMinePage = new MyPostsPage();
			Stage searchMineStage = new Stage();
			searchMinePage.start(searchMineStage);
		});
		
		
		VBox layout = new VBox(20, searchQsButton, searchAsButton, searchMineButton);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 30;");
	    
	    BorderPane borderPane = new BorderPane();
	    borderPane.setTop(new NavigationBar());
	    borderPane.setCenter(layout);
	    
	   Scene scene = new Scene(borderPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
	   primaryStage.setScene(scene);
	   primaryStage.show();
	   
	}

}