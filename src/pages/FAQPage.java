package pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logic.StatusData;
import model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FAQPage - UI page that displays frequently asked questions grouped by category.
 *
 * Staff users create FAQ entries in the administration UI; FAQPage reads those
 * entries (from the faqs table) and renders them in an Accordion grouped by category.
 *
 * Each FAQ displays:
 * - Question title (as the FAQ display title)
 * - Original question description
 * - One or more Solution answers (every answer with is_solution = TRUE)
 * - Optional staff notes
 *
 * Usage:
 *   new FAQPage().show(stage, currentUser);
 */

public class FAQPage {
    private Stage stage;
    private User user;

    public void show(Stage stage, User user) throws SQLException {
        this.stage = stage;
        this.user = user;

        stage.setTitle("Frequently Asked Questions");

        BorderPane mainPane = new BorderPane();

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        mainPane.setTop(navBar);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("Frequently Asked Questions");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subHeader = new Label("Common questions and their resolved answers");
        subHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        VBox header = new VBox(5, headerLabel, subHeader);
        header.setAlignment(Pos.CENTER);

        // Load FAQs
        List<FAQ> faqs = StatusData.databaseHelper.getAllFAQs();

        if (faqs.isEmpty()) {
            Label emptyLabel = new Label("No FAQs available yet.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            VBox emptyBox = new VBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            content.getChildren().addAll(header, emptyBox);
        } else {
            // Group FAQs by category
            Map<String, List<FAQ>> faqsByCategory = faqs.stream()
                .collect(Collectors.groupingBy(FAQ::getCategory));

            Accordion accordion = new Accordion();

            for (Map.Entry<String, List<FAQ>> entry : faqsByCategory.entrySet()) {
                String category = entry.getKey();
                List<FAQ> categoryFAQs = entry.getValue();

                VBox categoryContent = new VBox(10);
                categoryContent.setPadding(new Insets(10));

                for (FAQ faq : categoryFAQs) {
                    VBox faqBox = createFAQBox(faq);
                    categoryContent.getChildren().add(faqBox);
                }

                TitledPane categoryPane = new TitledPane(
                    category + " (" + categoryFAQs.size() + ")",
                    categoryContent
                );
                accordion.getPanes().add(categoryPane);
            }

            // Expand first category
            if (!accordion.getPanes().isEmpty()) {
                accordion.setExpandedPane(accordion.getPanes().get(0));
            }

            content.getChildren().addAll(header, accordion);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        mainPane.setCenter(scrollPane);

        Scene scene = new Scene(mainPane, StatusData.WINDOW_WIDTH, StatusData.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createFAQBox(FAQ faq) throws SQLException {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; " +
                     "-fx-border-radius: 5; -fx-background-radius: 5;");

        // Get the question
        Question question = StatusData.databaseHelper.getQuestionById(faq.getQuestionId());

        if (question == null) {
            Label errorLabel = new Label("Question not found.");
            errorLabel.setStyle("-fx-text-fill: red;");
            box.getChildren().add(errorLabel);
            return box;
        }

        // Question title
        Label titleLabel = new Label("Q: " + faq.getDisplayTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);

        // Question description
        Label descLabel = new Label(question.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #555;");

        box.getChildren().addAll(titleLabel, descLabel);


     // Get all solution answers for this question
        List<Answer> solutions = StatusData.databaseHelper.getSolutionsForQuestion(faq.getQuestionId());

        if (solutions != null && !solutions.isEmpty()) {
            for (Answer solution : solutions) {
                // spacing between multiple solutions
                Region spacer = new Region();
                spacer.setPrefHeight(8);
                box.getChildren().add(spacer);

                Label answerHeader = new Label("✓ Solution:");
                answerHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-size: 14px;");

                Label answerLabel = new Label(solution.getContent());
                answerLabel.setWrapText(true);
                answerLabel.setStyle("-fx-text-fill: #333;");

                Label answerAuthor = new Label("— " + solution.getAuthor());
                answerAuthor.setStyle("-fx-font-style: italic; -fx-text-fill: #888;");

                box.getChildren().addAll(answerHeader, answerLabel, answerAuthor);
            }
        } else {
            // This shouldn't happen if we validated properly, but just in case
            Label noSolutionLabel = new Label("No solution found for this question.");
            noSolutionLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-style: italic;");
            box.getChildren().add(noSolutionLabel);
        }

        
        // Add staff notes if they exist
        if (faq.getStaffNotes() != null && !faq.getStaffNotes().trim().isEmpty()) {
            Region spacer = new Region();
            spacer.setPrefHeight(5);
            
            Label notesLabel = new Label("Staff Note: " + faq.getStaffNotes());
            notesLabel.setWrapText(true);
            notesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-style: italic;");
            
            box.getChildren().addAll(spacer, notesLabel);
        }

        return box;
    }
}