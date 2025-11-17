package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Represents a single answer posted to a question.
 * Stores author, content, timestamp, and solution status.
 * Used by {@link model.Answers} and database operations.
 */
// single answer
public class Answer {
    private int answerId;
    private int userId;
    private int questionId;
    private String author;
    private String content;
    private String timestamp;
    private boolean isSolution;
    
    public Answer(int answerId, int userId, int questionId, String author, String content) {
        this(answerId, userId, questionId, author, content, 
             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), false);
    }
    
    public Answer(int answerId, int userId, int questionId, String author, String content, 
                  String timestamp, boolean isSolution) {
        this.answerId = answerId;
        this.userId = userId;
        this.questionId = questionId;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        this.isSolution = isSolution;
    }
    

    // Getters and Setters
    public int getAnswerId() { return answerId; }
    public void setAnswerId(int answerId) { this.answerId = answerId; }
    
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public int getUserId() { return userId; } // GET SET USER ID					
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public boolean isSolution() { return isSolution; }
    public void setSolution(boolean solution) { isSolution = solution; }
    
    @Override
    public String toString() {
        String solutionMarker = isSolution ? " [SOLUTION]" : "";
        return String.format("Answer #%d by %s%s", answerId, author, solutionMarker);
    }
}
