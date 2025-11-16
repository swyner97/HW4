package application;

import java.util.regex.Pattern;

public final class EmailRecognizer {
	
	public static String emailErrorMessage = "";
	public static String emailInput = "";
	public static int emailErrorIndex = -1;
	
	public static String inputLine = "";
	private static char currentChar;
	private static int currentCharNdx;
	private static boolean running;
	
	// regex for valid email
	// accepts alphanumeric, periods, dashes
	// rejects if double period or starting/ending dashes/periods
	// only domains checked are com, edu, org, net
	private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9.-]*[A-Za-z0-9])?@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)(?:\\.(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?))*\\.(com|edu|org|net)$", Pattern.CASE_INSENSITIVE);
	
	// prevent instantiation
	private EmailRecognizer() { }
	
	public static String validate(String s) {
		emailErrorMessage = "";
		emailInput = s;
		emailErrorIndex = -1;
		
		//empty input
		if(s == null || s.isBlank()) {
			return "Email is required";
		}
		
		inputLine = s;
		currentCharNdx = 0;
		currentChar = inputLine.charAt(0);
		running = true;
		
		int atCount = 0;
		int atIndex = -1;
		char prev = '\0';
		boolean hasConsecutiveDots = false;
		
		while (running) {
			if(Character.isWhitespace(currentChar)) {
				emailErrorIndex = currentCharNdx;
				return "Email cannot contain whitespace characters";
			}
		
			//consecutive periods
			if(currentChar == '.' && prev == '.') {
				hasConsecutiveDots = true;
			}
			
			if (currentChar == '@') {
				atCount++;
				if(atCount == 1) atIndex = currentCharNdx;
			}
			
			prev = currentChar;
			currentCharNdx++;
			if(currentCharNdx >= inputLine.length()) {
				running = false;
			} else {
				currentChar = inputLine.charAt(currentCharNdx);
			}
		}
			
			if(hasConsecutiveDots) {
				int i = s.indexOf("..");
				emailErrorIndex = (i >= 0 ? i : 0);
				return "Email cannot contain consecutive periods";
			}
			
			if(atCount != 1) {
				emailErrorIndex = (atCount == 0 ? 0: atIndex);
				return(atCount == 0) ? "Email must contain exactly one '@'" : "Email cannot contain more than one '@'";
			}
			
			// splitting for leading or trailing periods and dashes
			String user = s.substring(0, atIndex);
			String domain = s.substring(atIndex + 1);
			
			if(user.isEmpty()) {
				emailErrorIndex = 0;
				return "Invalid email: empty local part before '@'";
			}
			if(domain.isEmpty()) {
				emailErrorIndex = atIndex + 1;
				return "Invalid email: empty domain address after '@'";
			}

			// check before @ 
			if(user.charAt(0) == '.' || user.charAt(0) == '-' || user.charAt(user.length() - 1) == '.' || user.charAt(user.length() - 1) == '-') {
				emailErrorIndex = 0;
				return ("Email cannot start or end with \".\" or \"-\"\nInvalid input: " + user + "\n");
			}
			
			// check domain
			String[] labels = domain.split("\\.");
			for(String label : labels) {
				if(label.isEmpty()) {
					return "Invalid domain: empty address";
				}
				if(label.charAt(0) == '-' || label.charAt(label.length() - 1) == '-') {
					return "Invalid domain: domain cannot start or end with '-'";
				}	
			}
			
			if(labels.length < 2 || labels[labels.length - 2].isEmpty()) {
				return "Invalid domain: empty domain";
			}
			
			//domain extension
			if(!EMAIL.matcher(s).matches()) {
				return "Invalid domain extension. Accepted: .com, .net, .org, .edu";
			}
			
		emailErrorIndex = -1;
		emailErrorMessage = "";
		return "";
	}
	
	public static boolean isValid(String s) {
		return validate(s).isEmpty();
	}

}
