package model;


/**
 * Represents a reviewer user with permissions to provide feedback
 * and evaluate other users' submissions in the system.
 * All instances of this class are created with the {@link User.Role#REVIEWER} role.
 */
public class Reviewer extends User {
		public Reviewer(int id, String userName, String password, String name, String email, String tempPw) {
	        super(id, userName, password, Role.REVIEWER, name, email, tempPw);
	    }

	    public Reviewer(String userName, String password, String name, String email, String tempPw) {
	        super(userName, password, Role.REVIEWER, name, email, tempPw);
	    }

	    public Reviewer(String userName, String password) {
	        super(userName, password, Role.REVIEWER, "", "", null);
	    }

	    @Override
	    public Role getRole() {
	        return Role.REVIEWER;
	    }

}
