package logic;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Answers;
import model.Questions;
import model.User;


/**
 * Holds shared application state and configuration for the JavaFX client.
 * Stores shared instances such as the {@link databasePart1.DatabaseHelper}, the primary
 * {@link javafx.stage.Stage}, and the {@link Questions} / {@link Answers} managers,
 * as well as window sizing and a utility method for setting scenes.
 */
public class StatusData {
    public static DatabaseHelper databaseHelper;
    public static Stage primaryStage;
    public static Questions questions;
    public static Answers answers;
    
    public static User currUser;
    public static final boolean DEV_MODE = false;
    // Window size constants
    public static final int WINDOW_WIDTH = 1500;
    public static final int WINDOW_HEIGHT =1000;
    
    // Utility method to set scene with consistent sizing
    public static void setScene(Stage stage, BorderPane root) {
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }
}