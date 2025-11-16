package application;

import java.util.List;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as id, userName, password, role, name, and email.
 */
public class User {
    private int id;
    private String userName;
    private String password;
    private String role;
    private String name;
    private String email;
    private String tempPw;

    public static User createUser(int id, String userName, String password, String role, String name, String email, String tempPw) {
        return new User(id, userName, password, role, name, email, tempPw);
    }

    public static User createUser(String userName, String password, String role, String name, String email, String tempPw) {
        return new User(0, userName, password, role, name, email, tempPw);
    }

    public static User createUser(String userName, String password, String role) {
        return new User(0, userName, password, role, "", "", null);
    }

    protected User(int id, String userName, String password, String role, String name, String email, String tempPw) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.tempPw = tempPw;
    }

    protected User(String userName, String password, String role, String name, String email, String tempPw) {
        this(0, userName, password, role, name, email, tempPw); // id 0 indicates not set yet
    }

    protected User(String userName, String password, String role) {
        this(0, userName, password, role, "", "", null);
    }

    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getRole() { return role == null ? "user" : role; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTempPw() { return tempPw; }

    public List<Question> getUserQuestions() {
        return StatusData.databaseHelper.searchQuestions(null, null, getName());
    }

    public void setId(int id) { this.id = id; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setTempPw(String tempPw) { this.tempPw = tempPw; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", role='" + getRole() + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
