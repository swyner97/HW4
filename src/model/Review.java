package model;

/**
 * Represents a single review attached to an answer.
 * <p>
 * This class stores per-user ratings (a map from userId -> rating 1..5),
 * plus author/content/timestamp metadata. It provides convenient getters
 * used by {@link Reviews} and by the database helper.
 *
 * <p>Design notes:
 * <ul>
 *   <li>Average rating is computed on demand from {@code userRatings}.</li>
 *   <li>{@literal @link pages.TrustedReviewersPage.ReviewRow.getRating()} returns a nullable Integer suitable for
 *       storing as a single rating column in legacy schemas (null = no ratings).</li>
 * </ul>
 *
 * @author Sarah Wyner
 * @since 1.0
 */
public class Review {
    private int reviewId;
    private int userId;
    private int answerId;

    private String author;
    private String content;
    private String timestamp; // ISO or epoch-millis string
    
    /**
     * Construct a Review with the provided metadata.
     *
     * @param reviewId id for this review (application-assigned)
     * @param userId id of the user who wrote the review
     * @param answerId id of the answer this review is attached to
     * @param author author display name
     * @param content review body text
     */

    public Review(int reviewId, int userId, int answerId, String author, String content) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.answerId = answerId;
        this.author = author;
        this.content = content;

        // set a default timestamp now (can be overridden by DB loader)
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    // Getters / setters used by DB helper
    public int getReviewId() { return reviewId; }
    public void setReviewId(int id) { this.reviewId = id; }

    public int getUserId() { return userId; }
    public int getAnswerId() { return answerId; }

    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // timestamp accessors
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

}
