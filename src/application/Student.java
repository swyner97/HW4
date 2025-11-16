package application;

public class Student extends User {

    public Student(int id, String userName, String password, String name, String email, String tempPw) {
        super(id, userName, password, "student", name, email, tempPw);
    }

    public Student(String userName, String password, String name, String email, String tempPw) {
        super(userName, password, "student", name, email, tempPw);
    }

    public Student(String userName, String password) {
        super(userName, password, "student");
    }

    @Override
    public String getRole() {
        return "student";
    }
}
