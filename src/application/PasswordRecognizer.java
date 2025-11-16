package application;


public class PasswordRecognizer {
	/**
	 * <p> Title: Directed Graph-translated Password Assessor. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Directed Graph 
	 * diagram into an executable Java program using the Password Evaluator Directed Graph. 
	 * The code detailed design is based on a while loop with a cascade of if statements</p>
	 * 
	 * <p> Copyright: Lynn Robert Carter © 2022 </p>
	 * 
	 * @author Lynn Robert Carter
	 * 
	 * @version 0.00		2018-02-22	Initial baseline 
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	
	//boolean flags for all 6 criteria (added otherChar boolean)
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	public static boolean otherChar = false;
	
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running
	private static int charCounter;						//charCounter added for semantic actions
	
	/**********
	 * This private method display the input line and then on a line under it displays an up arrow
	 * at the point where an error should one be detected.  This method is designed to be used to 
	 * display the error message on the console terminal.
	 * 
	 * @param input				The input string
	 * @param currentCharNdx	The location where an error was found
	 * @return					Two lines, the entire input line followed by a line with an up arrow
	 */
	private static void displayInputState() {
		// Display the entire input line
		System.out.println(inputLine);
		//System.out.println(inputLine.substring(0,currentCharNdx) + "?");
		//System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + 
				//currentCharNdx + "  |  The currentChar: \"" + currentChar + "\"");
	}

	/**********
	 * This method is a mechanical transformation of a Directed Graph diagram into a Java
	 * method.
	 * 
	 * @param input		The input string for directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a help description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		
		if(input.length() <= 0) return "*** Error *** The password is empty!";
		
		// The input is not empty, so we can access the first character
		//Semantic Action [0] Set currentChar to first inputchar
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state

		passwordInput = input;				// Save a copy of the input
		
		//Semantic Action [0]
		foundUpperCase = false;				// Reset the Boolean flag
		foundLowerCase = false;				// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundSpecialChar = false;			// Reset the Boolean flag
		foundLongEnough = false;			// Reset the Boolean flag
		otherChar = false;					// Reset the Boolean flag
		charCounter = 0;					// Set CharCounter to 0
		running = true;						// Start the loop

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition
		while (running) {
			displayInputState();
			// The cascading if statement sequentially tries the current character against all of the
			// valid transitions
			if (currentChar >= 'A' && currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				//Semantic Action [1.2]
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				System.out.println("Lower case letter found");
				//Semantic Action [2.2]
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				System.out.println("Digit found");
				//Semantic Action [3.2]
				foundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) {
				System.out.println("Special character found");
				//Semantic Action [4.2]
				foundSpecialChar = true;
			} else {
				passwordIndexofError = currentCharNdx;
				
				//Semantic Action [5]
				otherChar = true;
				return "*** Error *** An invalid character has been found!";
			}
			
			if (currentCharNdx >= 7) {
				System.out.println("At least 8 characters found");
				foundLongEnough = true;
			}
			
			// Go to the next character if there is one
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length()) {
				running = false;
			}
			else {
				//Smenatic Actions: increment charCounter, set currentChar to next input
				charCounter++;
				currentChar = input.charAt(currentCharNdx);	
			}
			
			System.out.println();
		}
		
		String errMessage = "";
		if (!foundUpperCase)
			errMessage += "The password must have at least one upper case character.\n";
		
		if (!foundLowerCase)
			errMessage += "The password must have at least one lower case character.\n";
		
		if (!foundNumericDigit)
			errMessage += "The password must have at least one number.\n";
			
		if (!foundSpecialChar)
			errMessage += "The password must have at least one special character "
					+ "(~`!@#$%^&*()_-+{}[]|:,.?/).\n";
			
		if (!foundLongEnough)
			errMessage += "The password must be at least 8 characters long.\n";
		
		if (otherChar)
			errMessage += "An invalid character was found.\n";
		
		if (errMessage == "")
			return "";
		
		passwordIndexofError = currentCharNdx;
		return errMessage + "Try again...";

	}
	//main for debugging and testing
	public static void main(String[] args) {
		String[] testPasswords = {
				"",					//empty
				"short1!", 			//too short
				"alllowercase1@",	//all lowercase
				"ALLUPPERCASE2#",	//all uppercase
				"noDigits$",		//no numbers
				"NoSpecial5",		//no specialchar
				"Other7ch@r╡",		//other char found
				"6Valid_Pass",		//valid
				"vw",				//more than one error
		};
		
		for (String password : testPasswords) {
			System.out.println("Testing password: \"" + password + "\"");
			String result = evaluatePassword(password);
			if (result.isEmpty()) {
				System.out.println("Valid password \n");
			}
			else {
				System.out.println("Invalid:\n" + result + "\n");
			}
		}
	}

}

