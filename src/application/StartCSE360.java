package application;
import javafx.application.Application;
import javafx.stage.Stage;
import logic.StatusData;
import model.Answers;
import model.Questions;
import model.Reviews;
import model.User;
import pages.FirstPage;
import pages.InitialAccessPage;

import java.sql.SQLException;

import java.util.*;

import databasePart1.DatabaseHelper;


public class StartCSE360 extends Application {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	
	public static final int WINDOW_WIDTH = 3000;  
	public static final int WINDOW_HEIGHT = 700;  
	
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
  
            //Note: methods to run to alter tables in the database
            //databaseHelper.addMissingColumns();  //for db user table
            //databaseHelper.alterNull();

            StatusData.questions = new Questions(databaseHelper);
            StatusData.answers = new Answers(databaseHelper);
            
 //StatusData.databaseHelper.deleteOneTime();
            
            if (StatusData.questions.size() == 0) {
	          //=======DUMMY Qs ==========
	    		StatusData.questions.create(50, "Meg", "Eclipse", "Where do we download eclipse?", List.of("eclipse", "download"));
	    		StatusData.questions.create(51, "Charles", "HW2 homework", "Is there an extension on HW2?", List.of("HW2", "due"));
	    		StatusData.questions.create(52, "User1", "Github", "Does anyone have experience using github via eclipse?", List.of("github", "eclipse"));
	    		StatusData.questions.create(53, "AviUser", "Exam", "Is there a final exam?", List.of("exam", "final"));
	    		StatusData.questions.create(54, "User1", "Confused", "How do we know what to do?", null);
	    		StatusData.questions.create(55, "James", "Homework 1", "How did everyone else do on HW1?", null);
	    		StatusData.questions.create(56, "Meg", "CSE Majors", "Has anyone found a cheap place to purchase textbooks?", List.of("CSE", "textbooks"));
	    		
	    		//=======DUMMY As ==========
	    		StatusData.answers.create(50, 1, "User1", "Look on Module 0 in canvas");
	    		StatusData.answers.create(51, 1, "AviUser", "Go to this website: hyperlink");
	    		StatusData.answers.create(52, 7, "Charles", "If you search for them in Bing you're more likely to find them");
	    		StatusData.answers.create(53, 4, "User1", "The final exam is optional");
	    		StatusData.answers.create(54, 5, "Meg", "Go to myasu, then canvas, then dashboard, then click on class cse360, then look at the pages there");
	    		StatusData.answers.create(55, 5, "James", "The graders said it was confusing on purpose");
	    		StatusData.answers.create(56, 2, "James", "There was an extension but it's past now");
            }
            

            
            if (databaseHelper.isDatabaseEmpty()) {
            	
            	new FirstPage(databaseHelper).show(primaryStage);
            } else {
            	try {
            		List<User> existingReviewers = databaseHelper.getUsersByRole(User.Role.REVIEWER);
            		if (existingReviewers == null || existingReviewers.isEmpty()) {
            			System.out.println("No reviewers found, adding reviewers.");
            			
            			User r1 = User.createUser("Amy", "Password123!", User.Role.REVIEWER, "Reviewer Amy", "email@a.com", null);
            			User r2 = User.createUser("Bob", "Password123!", User.Role.REVIEWER, "Reviewer Bob", "email@a.com", null);
            			User r3 = User.createUser("Stanley", "Password123!", User.Role.REVIEWER, "Reviewer Stanley", "email@a.com", null);
            			User r4 = User.createUser("Claire", "Password123!", User.Role.REVIEWER, "Reviewer Claire", "email@a.com", null);
            			
            			if (!databaseHelper.doesUserExist(r1.getUserName())) databaseHelper.register(r1);
            			if (!databaseHelper.doesUserExist(r2.getUserName())) databaseHelper.register(r2);
            			if (!databaseHelper.doesUserExist(r3.getUserName())) databaseHelper.register(r3);
            			if (!databaseHelper.doesUserExist(r4.getUserName())) databaseHelper.register(r4);
            			
            			System.out.println("Reviewers created successfully.");
            			
            			//Create sample reviews
            			Reviews reviewsManager = new Reviews(databaseHelper);
            			
            			
            			if (reviewsManager.readAll().size() == 0)  {
                            System.out.println("No reviews found, adding sample reviews from auto-created reviewers.");

                            // Amy reviews Answer #1 and #2
                            reviewsManager.create(
                                    r1.getId(),
                                    1,  
                                    r1.getName(),
                                    "This answer gives a clear pointer to where Eclipse can be downloaded."
                            );
                            reviewsManager.create(
                                    r1.getId(),
                                    2,
                                    r1.getName(),
                                    "Good explanation of the homework extension timing."
                            );

                            // Bob reviews Answer #3
                            reviewsManager.create(
                                    r2.getId(),
                                    3,
                                    r2.getName(),
                                    "Nice suggestion about using Bing; might also mention official docs."
                            );

                            // Stanley reviews Answer #5
                            reviewsManager.create(
                                    r3.getId(),
                                    5,
                                    r3.getName(),
                                    "Very detailed steps, this should help confused students a lot."
                            );

                            // Claire reviews Answer #7
                            reviewsManager.create(
                                    r4.getId(),
                                    7,
                                    r4.getName(),
                                    "Accurate information about the extension; short but useful."
                            );

                            System.out.println("Sample reviews created successfully.");
                        } else {
                            System.out.println("Reviews already exist, skipping auto-creation of sample reviews.");
                        } 
            		} else {
            			System.out.println("Reviewers already exist. Skipping reviewer creation.");
            		}
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	new InitialAccessPage(databaseHelper).show(primaryStage);
                
            }
            System.out.println("Users: " + databaseHelper.getAllUsers());
            //System.out.println("Questions: " + databaseHelper.loadAllQs());
            //System.out.println("Questions author: " + question.getAuthor() + ", " + question.getUserId());
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    }
	

}