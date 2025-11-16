package application;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.SQLException;

import java.util.*;

import databasePart1.DatabaseHelper;


public class StartCSE360 extends Application {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	
	public static void main( String[] args )
	{
		StatusData.databaseHelper = databaseHelper;
		
		// Adding a catch to make sure the app shuts down before launching again
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			databaseHelper.closeConnection();
			System.out.println("Database connection closed (via shutdown hook).");
		}));
		
		launch(args);
	}
	
	@Override
    public void start(Stage primaryStage) {
		StatusData.primaryStage = primaryStage;
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            StatusData.questions = new Questions(databaseHelper);
            StatusData.answers = new Answers(databaseHelper);
            //StatusData.databaseHelper.deleteOneTime();
            
            if (StatusData.questions.size() == 0) {
	          //=======DUMMY Qs ==========
	    		StatusData.questions.create(1, "Meg", "Eclipse", "Where do we download eclipse?", List.of("eclipse", "download"));
	    		StatusData.questions.create(2, "Charles", "HW2 homework", "Is there an extension on HW2?", List.of("HW2", "due"));
	    		StatusData.questions.create(3, "User1", "Github", "Does anyone have experience using github via eclipse?", List.of("github", "eclipse"));
	    		StatusData.questions.create(4, "AviUser", "Exam", "Is there a final exam?", List.of("exam", "final"));
	    		StatusData.questions.create(5, "User1", "Confused", "How do we know what to do?", null);
	    		StatusData.questions.create(6, "James", "Homework 1", "How did everyone else do on HW1?", null);
	    		StatusData.questions.create(7, "Meg", "CSE Majors", "Has anyone found a cheap place to purchase textbooks?", List.of("CSE", "textbooks"));
	    		
	    		//=======DUMMY As ==========
	    		StatusData.answers.create(1, 1, "User1", "Look on Module 0 in canvas");
	    		StatusData.answers.create(2, 1, "AviUser", "Go to this website: hyperlink");
	    		StatusData.answers.create(3, 7, "Charles", "If you search for them in Bing you're more likely to find them");
	    		StatusData.answers.create(4, 4, "User1", "The final exam is optional");
	    		StatusData.answers.create(5, 5, "Meg", "Go to myasu, then canvas, then dashboard, then click on class cse360, then look at the pages there");
	    		StatusData.answers.create(6, 5, "James", "The graders said it was confusing on purpose");
	    		StatusData.answers.create(7, 2, "James", "There was an extension but it's past now");
            }
            
            if (databaseHelper.isDatabaseEmpty()) {
            	
            	new FirstPage(databaseHelper).show(primaryStage);
            } else {
            	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
                
            }
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    }
	

}
