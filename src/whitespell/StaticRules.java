package whitespell;

import org.eclipse.jetty.http.HttpStatus;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/3/15
 *         whitespell
 */
public class StaticRules {

    public static final int MIN_USERNAME_LENGTH = 1;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MIN_EMAIL_LENGTH = 6;
    public static final int MAX_EMAIL_LENGTH = 512;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 512;
    public static final int MAX_CONTENT_TYPE_LENGTH = 10;
    public static final int MAX_CONTENT_DESCRIPTION_LENGTH = 100;
    public static final int MIN_AUTHENTICATION_HEADER_LENGTH = 1;
    public static final int MAX_AUTHENTICATION_HEADER_LENGTH = 255;


    public enum ErrorCodes {

        //0-10 are unknown issues
        UNKNOWN_SERVER_ISSUE(100, "Sorry! We've encountered an unknown error.", HttpStatus.INTERNAL_SERVER_ERROR_500),

        // 100-110 is taken
        USERNAME_TAKEN(100, "Username is already taken", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TAKEN(101, "Email is already taken", HttpStatus.UNAUTHORIZED_401),
        USERNAME_AND_EMAIL_TAKEN(102, "Username and email are already taken", HttpStatus.UNAUTHORIZED_401),
        ACCOUNT_NOT_FOUND(103, "Account not found", HttpStatus.UNAUTHORIZED_401),
        INVALID_USERNAME_OR_PASS(103, "Invalid username or Password", HttpStatus.UNAUTHORIZED_401),
        NOT_AUTHENTICATED(103, "User is not authenticated", HttpStatus.UNAUTHORIZED_401),

        //110-120 is style error
        USERNAME_TOO_LONG(110, "Username is too long ("+StaticRules.MAX_USERNAME_LENGTH+" is the max)", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TOO_LONG(111, "Email is too long ("+StaticRules.MAX_EMAIL_LENGTH+" is the max)", HttpStatus.UNAUTHORIZED_401),
        PASSWORD_TOO_LONG(112, "Password is too long ("+StaticRules.MAX_PASSWORD_LENGTH+" is the max)", HttpStatus.UNAUTHORIZED_401),
        NULL_VALUE_FOUND(113, "One (or more) of the required parameters was null, please check your documentation and the request parameters.", HttpStatus.UNAUTHORIZED_401),
        USERNAME_TOO_SHORT(114, "Username is too short ("+StaticRules.MIN_USERNAME_LENGTH+" is the minimum length)", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TOO_SHORT(115, "Email is too short ("+StaticRules.MIN_EMAIL_LENGTH+" is the min)", HttpStatus.UNAUTHORIZED_401),
        PASSWORD_TOO_SHORT(116, "Password is too short ("+StaticRules.MIN_PASSWORD_LENGTH+" is the min)", HttpStatus.UNAUTHORIZED_401),
        ALREADY_FOLLOWING_USER(117, "You are already following this user!", HttpStatus.UNAUTHORIZED_401),
        NOT_FOLLOWING_USER(118, "You are not following this user!", HttpStatus.UNAUTHORIZED_401),
        CONTENT_TYPE_TOO_LONG(119, "Content type is too long ("+StaticRules.MAX_CONTENT_TYPE_LENGTH+" is the max)", HttpStatus.UNAUTHORIZED_401),
        CONTENT_DESCRIPTION_TOO_LONG(120, "Content description is too long ("+StaticRules.MAX_CONTENT_DESCRIPTION_LENGTH+" is the max)", HttpStatus.UNAUTHORIZED_401),
        NO_SUCH_CATEGORY(121, "The content type you are attempting to insert does not exist", HttpStatus.UNAUTHORIZED_401),
        DUPLICATE_CONTENT_TYPE(122, "The content type already exists", HttpStatus.UNAUTHORIZED_401),
        ;

        int errorId;
        String errorMessage;
        int httpStatusCode;

        ErrorCodes(int errorId, String errorMessage, int httpStatusCode) {
            this.errorId = errorId;
            this.errorMessage = errorMessage;
            this.httpStatusCode = httpStatusCode;
        }

        public int getErrorId() {
            return this.errorId;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
        public int getHttpStatusCode() {
            return this.httpStatusCode;
        }
    }
}
