package model;

/**
 * Model class representing a Frequently Asked Question (FAQ) entry.
 *
 * Each FAQ links to a question and contains display metadata such as
 * category, title, staff notes, and the staff member who marked it.
 */
public class FAQ {
    private int faqId;
    private int questionId;
    private String category;
    private String displayTitle;
    private String staffNotes;
    private String dateMarked;
    private int markedByStaffId;
    private String questionTitle;

    // -------- Constructors --------

    public FAQ() {}

    /**
     * Constructor used when creating a new FAQ (ID auto-generated).
     *
     * @param questionId ID of the related question
     * @param category FAQ category (e.g. "General", "Technical")
     * @param displayTitle Display title shown in the FAQ list
     * @param staffNotes Optional notes left by the staff
     * @param markedByStaffId ID of the staff/admin who created the FAQ
     */
    public FAQ(int questionId, String category, String displayTitle, String staffNotes, int markedByStaffId) {
        this.questionId = questionId;
        this.category = category;
        this.displayTitle = displayTitle;
        this.staffNotes = staffNotes;
        this.markedByStaffId = markedByStaffId;
    }

    /**
     * Full constructor including FAQ ID (used in tests or when reloading from DB).
     */
    public FAQ(int faqId, int questionId, String category, String displayTitle, String staffNotes, int markedByStaffId) {
        this.faqId = faqId;
        this.questionId = questionId;
        this.category = category;
        this.displayTitle = displayTitle;
        this.staffNotes = staffNotes;
        this.markedByStaffId = markedByStaffId;
    }

    // -------- Getters & Setters --------

    public int getFaqId() { return faqId; }
    public void setFaqId(int faqId) { this.faqId = faqId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDisplayTitle() { return displayTitle; }
    public void setDisplayTitle(String displayTitle) { this.displayTitle = displayTitle; }

    public String getStaffNotes() { return staffNotes; }
    public void setStaffNotes(String staffNotes) { this.staffNotes = staffNotes; }

    public String getDateMarked() { return dateMarked; }
    public void setDateMarked(String dateMarked) { this.dateMarked = dateMarked; }

    public int getMarkedByStaffId() { return markedByStaffId; }
    public void setMarkedByStaffId(int markedByStaffId) { this.markedByStaffId = markedByStaffId; }

    public String getQuestionTitle() { return questionTitle; }
    public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
}
