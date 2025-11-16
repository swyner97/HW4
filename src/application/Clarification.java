package application;

public class Clarification {
	private int id, questionId, answerId;
	private String author, content, timestamp;
	
	public Clarification(int id, int questionId, int answerId, String author, String content, String timestamp) {
		this.id = id;
		this.questionId = questionId;
		this.answerId = answerId;
		this.author = author;
		this.content = content;
		this.timestamp = timestamp;
	}
	
	//GETTERS
	public int getId() {
		return id;
	}
	public int getQuestionId() {
		return questionId;
	}
	public int getAnswerId() {
		return answerId;
	}
	public String getAuthor() {
		return author;
	}
	public String getContent() {
		return content;
	}
	public String getTimestamp() {
		return timestamp;
	}
	
	//SETTERS
	public void setId(int id) {
		this.id = id;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public void setAnswerId(int answerId) {
		this.answerId = answerId;
	}
	public void setAuthor(String author) {
		this.author= author;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
