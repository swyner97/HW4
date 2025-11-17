/**
 * The {@code pages} package contains all the JavaFX-based user interface (UI) components of the application.
 * <p>
 * Each class in this package typically corresponds to a specific scene in the application. These pages are composed
 * using JavaFX layout managers, controls, and styles, and they handle event-driven logic for user interactions.
 * 
 * <h2>Key Functional Areas:</h2>
 * <ul>
 *   <li><b>Authentication and Account Management</b>
 *     <ul>
 *       <li>{@literal @link pages.UserLoginPage} – Main login screen for users to access the platform.</li>
 *       <li>{@literal @link pages.SignUpPage} – Allows new users to register and create an account.</li>
 *       <li>{@literal @link pages.SetupAccountPage} – Guides users through initial account setup post-registration.</li>
 *       <li>{@literal @link pages.UpdateAccountPage} – Enables users to edit their profile and credentials.</li>
 *       <li>{@literal @link pages.ResetPasswordPage} – Provides password reset functionality via secure prompts.</li>
 *       <li>{@literal @link pages.SetupLoginSelectionPage}, {@literal @link RoleSelectionPage}, {@literal @link WelcomeLoginPage} – Assist with login method, role selection, and redirection to appropriate entry points.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Home Pages</b>
 *     <ul>
 *       <li>{@link pages.AdminHomePage}, {@link pages.InstructorHomePage} – Home dashboards tailored to specific user roles.</li>
 *       <li>{@link pages.FirstPage}, {@link pages.InitialAccessPage} – Landing pages shown after login or first use.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Question and Answer Management</b>
 *     <ul>
 *       <li>{@literal @link pages.EditQuestionPage}, {@literal @link pages.EditAnswerPage} – Interfaces for editing submitted QA content.</li>
 *       <li>{@literal @link pages.MyPostsPage}, {@literal @link pages.MyQAPage} – Personal views of questions or answers authored by the user.</li>
 *       <li>{@literal @link pages.SearchPage}, {@literal @link pages.SearchAsPage} – Provide advanced question/answer search features.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Reviewing System</b>
 *     <ul>
 *       <li>{@link pages.ReviewPage} – Displays reviews for questions/answers.</li>
 *       <li>{@link pages.EditReviewPage} – Page for editing previously written reviews.</li>
 *       <li>{@link pages.TrustedReviewersPage} – Interface for assigning or managing trusted reviewers.</li>
 *     </ul>
 *   </li>
 *   
 *   <li><b>Messaging System</b>
 *     <ul>
 *       <li>{@link pages.MessagingPage} – Core messaging interface for user communication and clarifications.</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>User Profiles</b>
 *     <ul>
 *       <li>{@link pages.ProfilePage} – View and manage a user's public profile.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * The classes in this package coordinate directly with the {@code model}, {@code logic}, and {@code databasePart1} layers
 * to fetch data, trigger logic, and reflect user actions. Most pages follow a pattern of initializing UI controls, loading
 * dynamic content on scene display, and defining button or event handlers inline.
 * </p>
 */
package pages;