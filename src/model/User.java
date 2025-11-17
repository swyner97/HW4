package model;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import databasePart1.DatabaseHelper;
import logic.StatusData;


/**
 * Representes a generic user in the system, encapsulating common attributes such as
 * ID, username, password, role, name, and email.
 * <p>
 * This class serves as the base for all user types, including students, reviewers,
 * instructors, and administrators. It also provides helper methods that interact
 * with the database to manage trusted reviewer relationships, allowing any user
 * to add, remove, or check trusted reviewers.
 * </p>
 */
public class User {
    private int id;
    private String userName;
    private String password;
    private Role role;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String tempPw;
    /* ---------- Factory helpers ---------- */

    // Accept Role
    public static User createUser(int id, String userName, String password, Role role, String name, String email, String tempPw) {
        return new User(id, userName, password, role, name, email, tempPw);
    }

    // Accept role as String
    public static User createUser(int id, String userName, String password, String role, String name, String email, String tempPw) {
        return new User(id, userName, password, Role.fromString(role), name, email, tempPw);
    }
    
    public static User createUser(String userName, String password, Role role, String name, String email, String tempPw) {
        return new User(0, userName, password, role, name, email, tempPw);
    }

    public static User createUser(String userName, String password, String role, String name, String email, String tempPw) {
        return new User(0, userName, password, Role.fromString(role), name, email, tempPw);
    }

    public static User createUser(String userName, String password, String role) {
        return new User(0, userName, password, Role.fromString(role), "", "", null);
    }

    /* ---------- Constructors ---------- */

    public User(int id, String userName, String password, Role role, String name, String email, String tempPw) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.role = role == null ? Role.UNKNOWN : role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.tempPw = tempPw;
    }


    public User(String userName, String password, Role role, String name, String email, String tempPw) {
        this(0, userName, password, role, name, email, tempPw);
    }

    public User(String userName, String password, String role, String name, String email, String tempPw) {
        this(0, userName, password, Role.fromString(role), name, email, tempPw);

    }
    
    //Constructor with phone and bio to maintain table completeness
    protected User(int id, String userName, String password, Role role, String name, String email, String phone, String bio, String tempPw) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.role = role == null ? Role.UNKNOWN : role;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.tempPw = tempPw;
    }
    
    public static User createUser(int id, String userName, String password, String role, String name, String email, String phone, String bio, String tempPw) {
        return new User(id, userName, password, Role.fromString(role), name, email, phone, bio, tempPw);
    }


    /* ---------- Role enum ---------- */

    public enum Role {
        ADMIN,
        INSTRUCTOR,
        STUDENT,
        REVIEWER,
        TA,
        STAFF,
        UNKNOWN;

        public static Role fromString(String s) {
            if (s == null) return UNKNOWN;
            try {
                return Role.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return UNKNOWN;
            }
        }
    }

    /* ---------- Getters / Setters ---------- */

    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }

    // return Role enum
    public Role getRole() { return role == null ? Role.UNKNOWN : role; }

    // convenience: return role name as String
    public String getRoleName() { return getRole().name(); }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBio() { return bio; }
    public String getTempPw() { return tempPw; }

    public List<Question> getUserQuestions() {
        return StatusData.databaseHelper.searchQuestions(null, null, getName());
    }

    public void setId(int id) { this.id = id; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }

    // set by Role
    public void setRole(Role role) { this.role = role == null ? Role.UNKNOWN : role; }

    // set by string
    public void setRole(String role) { this.role = Role.fromString(role); }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBio(String bio) { this.bio = bio; }
    public void setTempPw(String tempPw) { this.tempPw = tempPw; }

    @Override
    /*public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", role='" + getRoleName() + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }*/
    public String toString() {
    	if (name != null && !name.isEmpty()) {
    		return name + " (" + userName + ")";
    	}
    	return userName;
    }

    /* ---------- Permission helpers ---------- */

    /**
     * Returns true for roles that should be treated as privileged (admin/instructor/reviewer/ta/staff).
     */
    public boolean isPrivileged() {
        Role r = getRole();
        return r == Role.ADMIN || r == Role.INSTRUCTOR || r == Role.REVIEWER || r == Role.TA || r == Role.STAFF;
    }

    /**
     * Convenience used by the UI: can this user mark an answer as solution?
     * By default privileged users or the question author may mark as solution.
     */
    public boolean canMarkSolution(Question q) {
        if (isPrivileged()) return true;
        if (q == null) return false;
        if (this.name == null) return false;
        return Objects.equals(this.name, q.getAuthor());
    }
    
    /* ---------------- Trusted Reviewer Functions ---------------- */
    public boolean isTrusted(User reviewer, DatabaseHelper db) throws SQLException {
    	return db.isTrusted(this.getId(), reviewer.getId());
    }
    
    public boolean addTrustedReviewer(User reviewer, DatabaseHelper db) throws SQLException {
    	return db.addTrustedReviewer(this.getId(), reviewer.getId());
    }
    
    public boolean removeTrustedReviewer(User reviewer, DatabaseHelper db) throws SQLException {
    	return db.removeTrustedReviewer(this.getId(), reviewer.getId());
    }
    
    
}
