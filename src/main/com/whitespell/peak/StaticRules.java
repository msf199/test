package main.com.whitespell.peak;

import org.eclipse.jetty.http.HttpStatus;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/3/15
 *         whitespell
 */
public class StaticRules {



    /** Old input checking system */

    public static final int MIN_USERNAME_LENGTH = 1;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MIN_EMAIL_LENGTH = 6;
    public static final int MAX_EMAIL_LENGTH = 512;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 512;
    public static final int MAX_CONTENT_TYPE_LENGTH = 10;
    public static final int MAX_CATEGORY_LENGTH = 45;
    public static final int MAX_THUMBNAIL_URL_LENGTH = 255;
    public static final int MAX_CONTENT_DESCRIPTION_LENGTH = 100;
    public static final int MIN_AUTHENTICATION_HEADER_LENGTH = 1;
    public static final int MAX_AUTHENTICATION_HEADER_LENGTH = 255;
    public static final int DEFAULT_MAX_LIMIT = 50;
    public static final int DEFAULT_MIN_OFFSET = 0;



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
        USERNAME_TOO_LONG(110, "Username is too long (" + StaticRules.MAX_USERNAME_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TOO_LONG(111, "Email is too long (" + StaticRules.MAX_EMAIL_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        PASSWORD_TOO_LONG(112, "Password is too long (" + StaticRules.MAX_PASSWORD_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        NULL_VALUE_FOUND(113, "One (or more) of the required parameters was null, please check your documentation and the request parameters.", HttpStatus.BAD_REQUEST_400
        ),
        USERNAME_TOO_SHORT(114, "Username is too short (" + StaticRules.MIN_USERNAME_LENGTH + " is the minimum length)", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TOO_SHORT(115, "Email is too short (" + StaticRules.MIN_EMAIL_LENGTH + " is the min)", HttpStatus.UNAUTHORIZED_401),
        PASSWORD_TOO_SHORT(116, "Password is too short (" + StaticRules.MIN_PASSWORD_LENGTH + " is the min)", HttpStatus.UNAUTHORIZED_401),
        ALREADY_FOLLOWING_USER(117, "You are already following this user!", HttpStatus.UNAUTHORIZED_401),
        NOT_FOLLOWING_USER(118, "You are not following this user!", HttpStatus.UNAUTHORIZED_401),
        CONTENT_TYPE_TOO_LONG(119, "Content type is too long (" + StaticRules.MAX_CONTENT_TYPE_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        CATEGORY_TOO_LONG(119, "Category is too long (" + StaticRules.MAX_CATEGORY_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        THUMBNAIL_URL_TOO_LONG(119, "Thumbnail URL is too long (" + StaticRules.MAX_THUMBNAIL_URL_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        CONTENT_DESCRIPTION_TOO_LONG(120, "Content description is too long (" + StaticRules.MAX_CONTENT_DESCRIPTION_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        NO_SUCH_CATEGORY(121, "The content type you are attempting to insert does not exist", HttpStatus.UNAUTHORIZED_401),
        DUPLICATE_CONTENT_TYPE(122, "The content type already exists", HttpStatus.UNAUTHORIZED_401),
        DUPLICATE_CATEGORY(123, "The content type already exists", HttpStatus.UNAUTHORIZED_401),
        NO_ENDPOINT_FOUND(124, "There was no endpoint found on this path. Make sure you're using the right method (GET,POST,etc.)", HttpStatus.NOT_FOUND_404),
        NOT_VALID_JSON_PAYLOAD(125, "All payloads are expected to be JSON only. The payload you entered was not valid JSON. Use http://jsonlint.com/ to validate your JSON if you are testing.", HttpStatus.BAD_REQUEST_400),
        ALREADY_FOLLOWING_CATEGORY(126, "You are already following this category!", HttpStatus.UNAUTHORIZED_401),
        ALREADY_PUBLISHING_CATEGORY(127, "You are already publishing in this category!", HttpStatus.UNAUTHORIZED_401),
        NOT_PUBLISHING_CATEGORY(128, "You are not publishing in this category!", HttpStatus.UNAUTHORIZED_401),
        USERID_NOT_NUMERIC(129, "User ID In URL is not a numeric value", HttpStatus.BAD_REQUEST_400),
        USER_NOT_FOUND(130, "User was not found", HttpStatus.NOT_FOUND_404),
		USER_NOT_EDITED(131, "User was not edited in profile edit", HttpStatus.BAD_REQUEST_400);
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

    public enum InputTypes {
        REG_INT_REQUIRED("int", true, 1,Integer.MAX_VALUE),
        REG_STRING_REQUIRED("string", true, 1,255),
        REG_INT_OPTIONAL("int", false, 1,Integer.MAX_VALUE),
        REG_STRING_OPTIONAL("string", false, 1,255),
        JSON_ARRAY_REQUIRED("jsonarray", false, 1,10000);

        private final String type;
        private final boolean isRequired;
        private final int minLength;
        private final int maxLength;

        InputTypes(String type, boolean isRequired, int minLength, int maxLength) {
            this.type = type;
            this.isRequired = isRequired;
            this.minLength = minLength;
            this.maxLength = maxLength;
        }


        public int getMinLength() {
            return minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public String getType() {
            return type;
        }
    }
}
