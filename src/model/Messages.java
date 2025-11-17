package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import databasePart1.*;

public class Messages {
	private int id;
	private int senderId;
	private int recipientId;
	private String senderName;
	private String recipientName;
	private String message;
	private LocalDateTime timestamp;
	private boolean isRead;

	public Messages(int id, int senderId, int recipientId, String message, LocalDateTime timestamp, boolean isRead) {
		this.id = id;
		this.senderId = senderId;
		this.recipientId = recipientId;
		this.message = message;
		this.timestamp = timestamp;
		this.isRead = isRead;
	}
	
	//constructor without ID for new messages before saving to DB
	public Messages(int senderId, int recipientId, String message) {
		this.senderId = senderId;
		this.recipientId = recipientId;
		this.message = message;
		this.timestamp = LocalDateTime.now();
	}
	
	//getters
	public int getId() { return id; }
	public int getSenderId() { return senderId; }
	public int getRecipientId() { return recipientId; }
	public String getMessage() { return message; }
	public String getSenderName() { return senderName; }
	public String getRecipientName() { return recipientName; }
	public LocalDateTime getTimestamp() { return timestamp; }
	public boolean isRead() { return isRead; }
	
	//setters
	public void setId(int id) { this.id = id; }
	public void setSenderId(int senderId) { this.senderId = senderId; }
	public void setRecipientId(int recipientId) { this.recipientId = recipientId; }
	public void setMessage(String message) { this.message = message; }
	public void setSenderName(String senderName) { this.senderName = senderName; }
	public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
	public void setIsRead(boolean read) { isRead = read; }
}


