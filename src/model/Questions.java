
package model;

import databasePart1.*;
import logic.Result;

import java.util.stream.Collectors;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Handles CRUD and search operations for {@link QUestion} entities,
 * maintaining an in-memory map synchronized with the database.
 * Provides filtering, recent-question retrieval, and permission checks.
 */
//importing comparator for getRecent
import java.util.Comparator;

public class Questions {
	private DatabaseHelper db;
    private Map<Integer, Question> questions;
    private int nextId;

    public Questions(DatabaseHelper db) {
    	this.db = db;
        this.questions = new HashMap<>();
        this.nextId = 1;
        
        List<Question> storedQuestions = db.loadAllQs();
        for (Question q : storedQuestions) {
        	questions.put(q.getQuestionId(), q);
        	if (q.getQuestionId() >= nextId) {
        		nextId = q.getQuestionId() + 1;
        	}
        }
    }

    public Result create(int userId, String author, String title, String description, List<String> tags) {
    	if (author == null || author.isBlank()) {
    		return new Result(false, "Author is required.", null);
    	}
    	if (title == null || title.isBlank()) {
    		return new Result(false, "Title is required.", null);
    	}
    	if (description == null || description.isBlank()) {
    		return new Result(false, "Description is required.", null);
    	}
        Question question = new Question(nextId, userId, author, title, description, null, "open", (tags == null ? new ArrayList<>() : new ArrayList<>(tags)));
        
        question.setTimestamp(LocalDateTime.now().toString().substring(0, 19));

        questions.put(nextId, question);
        
        try {
        	db.insertQuestion(question);
        }
        catch (SQLException e) {
        	e.printStackTrace();
        	return new Result(false, "Failed to save question to DB", null);
        }
        
        nextId++;
        return new Result(true, "Question created successfully", question);
    }

    public Question read(int questionId) {
        return questions.get(questionId);
    }

    //public List<Question> readAll() {
      //  return new ArrayList<>(questions.values());
    //}

    public Result update(int questionId, int userID, String title,
            String description, String status, List<String> tags, User user) {
    		Question question = questions.get(questionId);
    		if (question == null) {
    			return new Result(false, "Question not found", null);
    			}

    		if (question.getUserId() != userID && user.getRole() == User.Role.STUDENT)
    			return new Result(false, "You must be the author to edit this question.", null);

    		if (title != null && !title.isBlank()) question.setTitle(title);
    		if (description != null && !description.isBlank()) question.setDescription(description);
    		if (status != null) question.setStatus(status);
    		if (tags != null) question.setTags(tags);

    			try {
    				db.updateQuestion(question);
    			} catch (SQLException e) {
    				e.printStackTrace();
    				return new Result(false, "Database update failed.", null);
    			}

    			return new Result(true, "Question updated successfully.", question);
    }

    public Result delete(int questionId, int userID, User user) {
    	Question question = questions.get(questionId);
        if (!questions.containsKey(questionId)) {
            return new Result(false, "Question not found", null);
        }
        if (question.getUserId() != userID && user.getRole() == User.Role.STUDENT)
			return new Result(false, "You must be the author to edit this question.", null);

        
        // Remove from in-memory map
        questions.remove(questionId);
        
        // Delete from database
        try {
            db.deleteQuestion(questionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Failed to delete question from database.", null);
        }
        
        return new Result(true, "Question deleted successfully", null);
    }
    
    public List<Question> search(String keyword, String status, String author) {
    	//changing this to pull results from the database
        return db.searchQuestions(keyword, status, author);

    }
    
    public List<Question> getQuestionsByUserId(int userId) {
        return questions.values().stream()
            .filter(q -> q.getUserId() == userId)
            .collect(Collectors.toList());
    }
    
    public List<Question> getRecent() {
    	//changing this to pull results from the database
    	List<Question> all = db.searchQuestions(null, null, null);
    	all.sort(Comparator.comparing(Question::getTimestamp).reversed());
    	return all;
    }

    public int size() {
        return questions.size();
    }

}