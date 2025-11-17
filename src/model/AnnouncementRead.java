package model;

/**
 * Tracks which announcements have been read by which users.
 * Used for "show once" type announcements.
 */
public class AnnouncementRead {
    private int readId;
    private int announcementId;
    private int userId;
    private String readDate;
    
    // Constructors
    public AnnouncementRead() {}
    
    public AnnouncementRead(int announcementId, int userId) {
        this.announcementId = announcementId;
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getReadId() {
        return readId;
    }
    
    public void setReadId(int readId) {
        this.readId = readId;
    }
    
    public int getAnnouncementId() {
        return announcementId;
    }
    
    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getReadDate() {
        return readDate;
    }
    
    public void setReadDate(String readDate) {
        this.readDate = readDate;
    }
}