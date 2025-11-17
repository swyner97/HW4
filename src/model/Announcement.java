package model;

/**
 * The Announcement class represents a system-wide announcement.
 * Announcements can be created by staff and displayed to all users.
 */
public class Announcement {
    private int announcementId;
    private String title;
    private String content;
    private Priority priority;
    private String startDate;
    private String endDate;
    private DisplayType displayType;
    private int createdByStaffId;
    private String createdByStaffName;
    private String createdDate;
    private String lastModifiedDate;
    
    /**
     * Priority levels for announcements
     */
    public enum Priority {
        NORMAL("Normal", "#2196f3"),
        IMPORTANT("Important", "#ff9800"),
        URGENT("Urgent", "#f44336");
        
        private final String displayName;
        private final String color;
        
        Priority(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    /**
     * Display types for announcements
     */
    public enum DisplayType {
        SHOW_ONCE("Show Once Per User"),
        SHOW_ALWAYS("Show On Every Login");
        
        private final String displayName;
        
        DisplayType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Announcement() {}
    
    public Announcement(String title, String content, Priority priority, 
                       String startDate, String endDate, DisplayType displayType,
                       int createdByStaffId) {
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.displayType = displayType;
        this.createdByStaffId = createdByStaffId;
    }
    
    // Getters and Setters
    public int getAnnouncementId() {
        return announcementId;
    }
    
    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public DisplayType getDisplayType() {
        return displayType;
    }
    
    public void setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
    }
    
    public int getCreatedByStaffId() {
        return createdByStaffId;
    }
    
    public void setCreatedByStaffId(int createdByStaffId) {
        this.createdByStaffId = createdByStaffId;
    }
    
    public String getCreatedByStaffName() {
        return createdByStaffName;
    }
    
    public void setCreatedByStaffName(String createdByStaffName) {
        this.createdByStaffName = createdByStaffName;
    }
    
    public String getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    /**
     * Check if this announcement is currently active
     */
    public boolean isActive() {
        String now = java.time.LocalDateTime.now().toString();
        
        // Check if started
        if (startDate != null && now.compareTo(startDate) < 0) {
            return false;
        }
        
        // Check if expired
        if (endDate != null && now.compareTo(endDate) > 0) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + announcementId +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", active=" + isActive() +
                '}';
    }
}