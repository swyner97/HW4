package application;

public class Admin extends User {

    public Admin(int id, String userName, String password, String name, String email, String tempPw) {
        super(id, userName, password, "admin", name, email, tempPw);
    }

    public Admin(String userName, String password, String name, String email, String tempPw) {
        super(userName, password, "admin", name, email, tempPw);
    }

    public Admin(String userName, String password) {
        super(userName, password, "admin");
    }

    @Override
    public String getRole() {
        return "admin";
    }
}
