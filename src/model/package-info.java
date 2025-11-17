/**
 * The {@code model} package defines the core data structures and entities used throughout the application.
 * <p>
 * These classes represent the primary objects in the system—such as users, questions, answers, FAQs,
 * announcements, messages, and reviews—and serve as the bridge between the database layer
 * ({@code databasePart1}) and the higher-level application logic ({@code logic} and {@code pages} packages).
 * Each model encapsulates relevant fields, constructors, and methods to ensure data consistency,
 * integrity, and ease of interaction across all system layers.
 *
 * <h2>Class Summary:</h2>
 * <ul>
 *   <li>{@link Admin} – Represents an administrative user with full system management privileges (highest role).</li>
 *   <li>{@link Answer} – Defines a single answer associated with a specific question, including content, author, and solution status.</li>
 *   <li>{@link Answers} – Provides utilities for managing collections of {@code Answer} objects.</li>
 *   <li>{@link Announcement} – Represents a system-wide announcement created by a STAFF or ADMIN user, with metadata such as priority, start/end dates, and display type.</li>
 *   <li>{@link Clarification} – Represents a user-submitted clarification or suggestion related to a question or an answer, aiding collaborative refinement.</li>
 *   <li>{@link FAQ} – Represents a Frequently Asked Question entry, linking a question and one or more answers marked as solutions. Includes category, staff notes, and metadata about the staff member who created it.</li>
 *   <li>{@link FollowUpQ} – Represents a follow-up question tied to an existing discussion thread for deeper exploration.</li>
 *   <li>{@link Instructor} – Represents an instructor-type user, typically with permissions for reviewing, grading, or providing official responses.</li>
 *   <li>{@link Messages} – Encapsulates private message data exchanged between users (e.g., question authors and reviewers).</li>
 *   <li>{@link NavigationBar} – Defines the reusable navigational UI model for the application’s layout (menu, buttons, and links).</li>
 *   <li>{@link Question} – Represents a single user-posted question, including title, description, timestamp, author, and status fields.</li>
 *   <li>{@link Questions} – Manages loading, filtering, and organizing collections of {@code Question} objects.</li>
 *   <li>{@link Review} – Represents a review posted by a user, typically tied to an answer or piece of content.</li>
 *   <li>{@link Reviewer} – Defines a reviewer-type user who provides evaluations or feedback on submitted questions or answers.</li>
 *   <li>{@link Reviews} – Handles aggregation and management of multiple {@code Review} objects.</li>
 *   <li>{@link Staff} – Represents a staff-level user who can manage FAQs, announcements, and other administrative content (role below ADMIN).</li>
 *   <li>{@link Student} – Represents a standard student user with limited privileges (can post questions, suggest clarifications, and view FAQs).</li>
 *   <li>{@link User} – The foundational base class for all user types, encapsulating shared fields such as ID, username, password, email, and {@code Role} enum.</li>
 *   <li>{@link User.Role} – Enumerates all role types recognized by the system ({@code ADMIN}, {@code STAFF}, {@code INSTRUCTOR}, {@code STUDENT}, {@code REVIEWER}, {@code TA}, {@code UNKNOWN}).</li>
 * </ul>
 *
 * Together, these classes form the foundation of the system’s domain model. They ensure that the data
 * representing user interactions, QA threads, FAQs, reviews, and announcements is consistent,
 * secure, and easily manageable by both the business logic and UI layers.
 * </p>
 *
 * <p>
 * Used extensively by:
 * <ul>
 *   <li>{@code logic} package – for implementing permission checks, content workflows, and validations.</li>
 *   <li>{@code pages} package – for building interactive JavaFX-based user interfaces and dashboards.</li>
 * </ul>
 * </p>
 */
package model;
