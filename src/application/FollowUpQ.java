package application;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class FollowUpQ extends Stage{
	
	private TextField titleField;
    private TextArea descriptionArea;
    private final User user;
    private final Questions questions;
    private final int followUpQId;
    
    public FollowUpQ(User user, Questions questions, int followUpQId){
    
    	this.user = user;
    	this.questions = questions;
    	this.followUpQId = followUpQId;
    	
    	setTitle("Ask a Follow-Up Question");
        VBox root = createForm();
        Scene scene = new Scene(root, 600, 400);
        setScene(scene);
    	
    }
    private VBox createForm() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3;");

        Label title = new Label("Ask a Follow-Up Question");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label followUpLabel = new Label("Following up on Question ID: " + followUpQId);
        followUpLabel.setStyle("-fx-font-style: italic;");

        Label titleLabel = new Label("Title:");
        titleField = new TextField();
        titleField.setPromptText("Enter title...");

        Label descLabel = new Label("Description:");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter your follow-up question...");
        descriptionArea.setWrapText(true);

        Button postButton = new Button("Post Follow-Up");
        postButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        postButton.setOnAction(e -> handleSubmit());

        formBox.getChildren().addAll(title, followUpLabel, titleLabel, titleField, descLabel, descriptionArea, postButton);
        return formBox;
    }

    private void handleSubmit() {
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Both title and description are required.");
            return;
        }

        Question newQ = new Question(-1, user.getId(), user.getUserName(), title, desc);
        newQ.setTimestamp(LocalDateTime.now().toString().substring(0, 19));
        newQ.setStatus("open");
        newQ.setFollowUp(followUpQId);

        Result result = questions.create(user.getId(), user.getUserName(), title, desc, null);

        if (result.isSuccess()) {
        	showAlert(Alert.AlertType.INFORMATION, "Follow-up question posted successfully!");
            close();
        } else {
            showAlert(Alert.AlertType.ERROR, result.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.initOwner(this);
        alert.showAndWait();
    }
}

