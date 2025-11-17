package pages;

import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import databasePart1.*;
import pages.*;
import model.*;
import logic.*;
	
public class MessagingPageLauncher extends Application {
	
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
		DatabaseHelper databaseHelper = new DatabaseHelper();
        
		
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            StatusData.databaseHelper = databaseHelper;
            User testUser = StatusData.databaseHelper.getUserById(9);
            //StatusData.currUser = StatusData.databaseHelper.insertTestUser();
            StatusData.currUser = testUser;
            
            System.out.println(StatusData.databaseHelper.getAllUsers());
            new MessagingPage().show(primaryStage);
            
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
	}
}
