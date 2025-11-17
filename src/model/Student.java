package model;


/**
 * Represents a student user with access to questions, answers, and reviews features.
 * All instances of this class are created with the {@link User.Role#STUDENT} role.
 */
public class Student extends User {

    public Student(int id, String userName, String password, String name, String email, String tempPw) {
        super(id, userName, password, Role.STUDENT, name, email, tempPw);
    }

    public Student(String userName, String password, String name, String email, String tempPw) {
        super(userName, password, Role.STUDENT, name, email, tempPw);
    }

    public Student(String userName, String password) {
        super(userName, password, Role.STUDENT, "", "", null);
    }

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }
}
