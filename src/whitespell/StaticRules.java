package whitespell;

import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/3/15
 *         whitespell
 */
public class StaticRules {

    public static final int MAX_USERNAME_LENGTH = 12;
    public static final int MAX_EMAIL_LENGTH = 512;
    public static final int MAX_PASSWORD_LENGTH = 512;

    public static final ArrayList<String> errorCodes = new ArrayList<>(100);

    public enum ErrorCodes {

        USERNAME_TAKEN(100, "Username is already taken"),
        EMAIL_TAKEN(101, "Email is already taken"),
        USERNAME_AND_EMAIL_TAKEN(102, "Username and email are already taken");

        int errorId;
        String errorMessage;

        ErrorCodes(int errorId, String errorMessage) {
            this.errorId = errorId;
            this.errorMessage = errorMessage;
        }

        public int getErrorId() {
            return this.errorId;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
    }

    ;

    static {
        // Account Registration
        errorCodes.add(0, "Username is already taken");
        errorCodes.add(1, "Email is already taken");
        errorCodes.add(2, "Email is already taken");
        errorCodes.add(3, "Phone Number is already taken");
        errorCodes.add(4, "Phone Number is already taken");
        errorCodes.add(5, "Data is missing");

        //Login
    }
}
