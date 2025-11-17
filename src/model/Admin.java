package model;


/**
 * Represents an administrative user with full privileged access in the system.
 * All instances of this class are created with the {@link User.Role#ADMIN} role.
 */
public class Admin extends User {

    public Admin(int id, String userName, String password, String name, String email, String tempPw) {
        super(id, userName, password, Role.ADMIN, name, email, tempPw);
    }

    public Admin(String userName, String password, String name, String email, String tempPw) {
        super(userName, password, Role.ADMIN, name, email, tempPw);
    }

    public Admin(String userName, String password) {
        super(userName, password, Role.ADMIN, "", "", null);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
