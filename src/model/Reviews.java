package model;

import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import databasePart1.*;
import logic.Result;

/**
 * In-memory manager for {@link Review} objects with backing persistence
 * via {@link DatabaseHelper}.
 *
 * <p>The class caches reviews in-memory and exposes CRUD and search
 * operations. It also handles basic permission checks for update/delete:
 * authors may edit/delete their own review; privileged users (staff/admin)
 * may operate on any review.
 *
 * <p>On construction the manager loads existing reviews from the database
 * so that callers can operate on the cached set.
 *
 * @see Review
 * @see DatabaseHelper
 * @author Sarah Wyner
 * @since 1.0
 */
public class Reviews {

    public Map<Integer, Review> reviews;
    private int nextId;
    private DatabaseHelper db;

    /**
     * Construct a Reviews manager backed by the provided DatabaseHelper.
     *
     * @param db database helper used for loading/saving review records
     */
    public Reviews(DatabaseHelper db) {
        this.reviews = new HashMap<>();
        this.nextId = 1;
        this.db = db;
        loadReviewsFromDatabase();
    }

    /**
     * Load reviews from the database into the in-memory cache.
     * This also advances {@link #nextId} so newly created reviews get unique ids.
     */
    private void loadReviewsFromDatabase() {
        List<Review> dbReviews = db.loadAllReviews();
        for (Review review : dbReviews) {
            reviews.put(review.getReviewId(), review);
            if (review.getReviewId() >= nextId) {
                nextId = review.getReviewId() + 1;
            }
        }
        System.out.println("Loaded " + reviews.size() + " reviews from database.");
    }

    /**
     * Create and persist a new review.
     *
     * @param userId id of the review author
     * @param answerId id of the answer being reviewed
     * @param author author display name
     * @param content review body
     * @return Result indicating success or failure (with message and payload)
     */
    public Result create(int userId, int answerId, String author, String content) {
        if (author == null || author.isBlank()) {
            return new Result(false, "Author is required.", null);
        }
        if (content == null || content.isBlank()) {
            return new Result(false, "Content is required.", null);
        }

        Review review = new Review(nextId, userId, answerId, author, content);

        try {
            db.insertReview(review);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Failed to save to database: " + e.getMessage(), null);
        }

        reviews.put(nextId, review);
        nextId++;
        return new Result(true, "Review created successfully.", review);
    }

    /**
     * Read a single review by id from the in-memory cache.
     *
     * @param reviewId id of the review
     * @return Review or {@code null} if not found
     */
    public Review read(int reviewId) {
        return reviews.get(reviewId);
    }

    /**
     * Return a list of all cached reviews.
     *
     * @return list snapshot of reviews
     */
    public List<Review> readAll() {
        return new ArrayList<>(reviews.values());
    }

    /**
     * Return reviews attached to a specific answer.
     *
     * @param answerId answer id
     * @return list of reviews for that answer
     */
    public List<Review> readByAnswerId(int answerId) {
        return reviews.values().stream()
                .filter(r -> r.getAnswerId() == answerId)
                .collect(Collectors.toList());
    }

    /**
     * Update the review content (and persist). Permission checks (author vs privileged)
     * may be applied by the caller; this method verifies the parent answer exists first.
     *
     * @param reviewId id of review to update
     * @param answerId id of parent answer (verified against DB)
     * @param currUser current user performing the update (may be used for permission checks)
     * @param content new content (if non-null will replace current)
     * @return Result indicating success or failure
     */
    public Result update(int reviewId, int answerId, User currUser, String content) {
        // Verify the answer exists
        List<Answer> answers = db.loadAllAnswers();
        Answer answer = answers.stream()
                .filter(a -> a.getAnswerId() == answerId)
                .findFirst()
                .orElse(null);

        Review review = reviews.get(reviewId);

        if (review == null) {
            return new Result(false, "Review not found.", null);
        }
        if (answer == null) {
            return new Result(false, "Parent answer not found.", null);
        }

        if (content != null) review.setContent(content);

        // Save updates to the database
        try {
            db.updateReview(review);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Failed to update review in database: " + e.getMessage(), null);
        }

        return new Result(true, "Review updated successfully.", review);
    }

    /**
     * Delete a review if the user is either the author or privileged.
     *
     * @param reviewId id of the review to delete
     * @param user the user requesting deletion (used for permission check)
     * @return Result indicating success or failure
     */
    public Result delete(int reviewId, User user) {
        Review review = reviews.get(reviewId);

        if (review == null) {
            return new Result(false, "Review not found.", null);
        }

        boolean isAuthor = review.getUserId() == user.getId();
        boolean isPrivileged = user.isPrivileged();

        // Only author or privileged roles can delete
        if (!isAuthor && !isPrivileged) {
            return new Result(false, "Only the author or staff can delete this review.", null);
        }

        // Remove from memory and database
        reviews.remove(reviewId);
        try {
            db.deleteReview(reviewId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Failed to delete review from database: " + e.getMessage(), null);
        }

        return new Result(true, "Review deleted successfully.", null);
    }

    /* -------------------- Search helpers -------------------- */

    /**
     * Search reviews for a specific answer, optional keyword, and minimum average rating.
     *
     * @param answerId id of the answer to search reviews for
     * @param keyword optional keyword matched against author or content (case-insensitive)
     * @return list of matching reviews
     */
    public List<Review> search(int answerId, String keyword, Integer minRating) {
        return reviews.values().stream()
                .filter(r -> r.getAnswerId() == answerId)
                .filter(r -> (keyword == null || keyword.isEmpty()
                        || r.getContent().toLowerCase().contains(keyword.toLowerCase())
                        || r.getAuthor().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
    }

    /**
     * Search reviews across all answers by keyword, author and optional min rating.
     *
     * @param keyword keyword to search in author/content
     * @param author optional author substring
     * @return matching reviews
     */
    
    public List<Review> search(String keyword, String author, Integer minRating) {
        String kw = keyword == null ? "" : keyword.toLowerCase();
        String au = author == null ? "" : author.toLowerCase();

        return reviews.values().stream()
                .filter(r -> kw.isEmpty()
                        || (r.getContent() != null && r.getContent().toLowerCase().contains(kw))
                        || (r.getAuthor() != null && r.getAuthor().toLowerCase().contains(kw)))
                .filter(r -> au.isEmpty()
                        || (r.getAuthor() != null && r.getAuthor().toLowerCase().contains(au)))
                .collect(Collectors.toList());
    }

}
