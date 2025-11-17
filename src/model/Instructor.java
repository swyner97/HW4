package model;

/**********
 * The Instructor class represents a Instructor entity in the system.
 * It contains the Instructor's details such as userName, password, and role.
 */
public class Instructor extends User {

    public Instructor(int id, String userName, String password, String name, String email, String tempPw) {
        super(id, userName, password, Role.INSTRUCTOR, name, email, tempPw);
    }

    public Instructor(String userName, String password, String name, String email, String tempPw) {
        super(userName, password, Role.INSTRUCTOR, name, email, tempPw);
    }

    public Instructor(String userName, String password) {
        super(userName, password, Role.INSTRUCTOR, "", "", null);
    }

    @Override
    public Role getRole() {
        return Role.INSTRUCTOR;
    }
}
