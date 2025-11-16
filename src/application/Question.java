
package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class Question {
    private int questionId;
    private int userId;   // ADDED USERID
    private String author;
    private String title;
    private String description;
    private String timestamp;
    private String status;
    private int followUp;
    private List<String> tags;
    private List<Answer> answers = new ArrayList<>();
    
    public Question(int questionId, int userId, String author, String title, String description) {
        this(questionId, userId, author, title, description, 
             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
             "Unresolved", new ArrayList<>());
    }
    
 // CONSTRUCTOR 2
    public Question(int questionId, int userId, String author, String title, String description,
                       String timestamp, String status, List<String> tags) {
            this.questionId = questionId;
            this.userId = userId;
            this.author = author;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.status = status;
            this.tags = tags != null ? tags : new ArrayList<>();
        }
    
  
    public void markResolved() {
        this.status = "Resolved";
    }
    
    public void markUnresolved() {
        this.status = "Unresolved";
    }
    
 // GETTERS SETTERS
    public int getQuestionId() { return questionId; }
     public void setQuestionId(int questionId) { this.questionId = questionId; }
     
     public int getUserId() { return userId; } // GET SET USER ID					
     public void setUserId(int userId) { this.userId = userId; }
     
     public String getAuthor() { return author; }
     public void setAuthor(String author) { this.author = author; }
     
     public String getTitle() { return title; }
     public void setTitle(String title) { this.title = title; }
     
     public String getDescription() { return description; }
     public void setDescription(String description) { this.description = description; }
     
     public String getTimestamp() { return timestamp; }
     public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
     
     public String getStatus() { return status; }
     public void setStatus(String status) { this.status = status; }

     public int getFollowUp() { return followUp; }
     public void setFollowUp(int followUp) { this.followUp = followUp; }
     
     public List<String> getTags() { return tags; }
     public void setTags(List<String> tags) { this.tags = tags; }
     
     public List<Answer> getAnswers() { return answers; }
     public void setAnswers(List<Answer> answers) { 
    	 this.answers = answers != null ? answers : new ArrayList<>();
     }
    
     
     
    @Override
    public String toString() {
        return String.format("Q#%d: %s (%s)", questionId, title, status);
    }
}

