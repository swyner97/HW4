package application;

import java.util.*;
import java.util.stream.Collectors;

public class SearchFunction {
	private final Questions questions;
    private final Answers answers;
    
    public SearchFunction(Questions questions, Answers answers) {
    	this.questions = questions;
    	this.answers = answers;
    }
    
    public List<Question> searchQuestions(String keyword, String filter, String author) {
    	List<Question> result = questions.readAll();
    	
    	if (filter != null) {
    		if (filter.equals("Resolved")) {
				result = questions.search(null, "Resolved", null);
			}
			else if (filter.equals("Open")) {
				result = questions.search(null, "Open", null);
			}
			else if (filter.equals("Recent")) {
				result = questions.getRecent();
			}
    	}
    	
    	if (keyword != null && !keyword.isBlank()) {
    		result = questions.search(keyword, null, null);
    	}
    	
    	if (author != null && !author.isBlank()) {
    		String a = author.toLowerCase();
    		result = result.stream()
    				.filter(q -> q.getAuthor().toLowerCase().contains(a))
    				.collect(Collectors.toList());
    	}
    	return result;
    }
    
    public List<Answer> searchAnswers(String keyword, String author, Boolean isSolution) {
    	return answers.search(keyword, author, isSolution);
    }
   
}
