package model;


/**
 * Represents a staff user with permissions to create and manage announcements visible to all users
 * mark or remove questions as FAQs (Frequently Asked Questions)
 * All instances of this class are created with the {@link User.Role#STAFF} role.
 */
public class Staff extends User {
		public Staff(int id, String userName, String password, String name, String email, String tempPw) {
	        super(id, userName, password, Role.STAFF, name, email, tempPw);
	    }

	    public Staff(String userName, String password, String name, String email, String tempPw) {
	        super(userName, password, Role.STAFF, name, email, tempPw);
	    }

	    public Staff(String userName, String password) {
	        super(userName, password, Role.STAFF, "", "", null);
	    }

	    @Override
	    public Role getRole() {
	        return Role.STAFF;
	    }

}
