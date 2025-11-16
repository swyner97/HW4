package application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;
import java.util.*;
import databasePart1.DatabaseHelper;

public class ClarificationsManager {
	private final Map<Integer, Clarification> clarifications;
	private int nextId;
	private final DatabaseHelper db;
	
	public ClarificationsManager(DatabaseHelper db) {
		this.db = db;
		this.clarifications = new HashMap<>();
		this.nextId = 1;
		loadAllClarifications();
	}
	
	private void loadAllClarifications() {
		try {
			System.out.println("Clarifications will be loaded on demand.");
		}
		catch (Exception e) {
			System.err.println("Error loading clarifications: " + e.getMessage());
		}
	}
	
	public Result create(int questionId, int answerId, String author, String content) {
		if (content == null || content.isBlank()) {
			return new Result(false, "Clarification content cannot be empty.", null);
		}
		
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		Clarification clarification = new Clarification(nextId, questionId, answerId, author, content, timestamp);
		
		try {
			db.insertClarification(clarification);
			clarifications.put(nextId, clarification);
			nextId++;
			return new Result(true, "Clarification added successfully.", clarification);
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new Result(false, "Database error while saving clarification: " + e.getMessage(), null);
		}
	}
		
		List<Clarification> readByQuestion(int questionId) {
			try {
				return db.loadClarificationsforQ(questionId);
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
		
		public List<Clarification> readByAnswer(int answerId) {
			try {
				return db.loadClarificationsforA(answerId);
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
				
		}
		
	}


