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
    public static final int MAX_COMMENT_LENGTH = 255;




    public enum ErrorCodes {

        //0-10 are unknown issues
        UNKNOWN_SERVER_ISSUE(0, "Sorry! We've encountered an unknown error.", HttpStatus.INTERNAL_SERVER_ERROR_500),

        // 100-110 is taken
        USERNAME_TAKEN(100, "Username is already taken", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TAKEN(101, "Email is already taken", HttpStatus.UNAUTHORIZED_401),
        USERNAME_AND_EMAIL_TAKEN(102, "Username and email are already taken", HttpStatus.UNAUTHORIZED_401),
        ACCOUNT_NOT_FOUND(103, "Account not found", HttpStatus.UNAUTHORIZED_401),
        INVALID_USERNAME_OR_PASS(104, "Invalid username or Password", HttpStatus.UNAUTHORIZED_401),
        NOT_AUTHENTICATED(105, "User is not authenticated", HttpStatus.UNAUTHORIZED_401),

        //110-120 is style error
        USERNAME_TOO_LONG(110, "Username is too long (" + StaticRules.MAX_USERNAME_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TOO_LONG(111, "Email is too long (" + StaticRules.MAX_EMAIL_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        PASSWORD_TOO_LONG(112, "Password is too long (" + StaticRules.MAX_PASSWORD_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        NULL_VALUE_FOUND(113, "One (or more) of the required parameters was null, please check your documentation and the request parameters.", HttpStatus.BAD_REQUEST_400),
        USERNAME_TOO_SHORT(114, "Username is too short (" + StaticRules.MIN_USERNAME_LENGTH + " is the minimum length)", HttpStatus.UNAUTHORIZED_401),
        EMAIL_TOO_SHORT(115, "Email is too short (" + StaticRules.MIN_EMAIL_LENGTH + " is the min)", HttpStatus.UNAUTHORIZED_401),
        PASSWORD_TOO_SHORT(116, "Password is too short (" + StaticRules.MIN_PASSWORD_LENGTH + " is the min)", HttpStatus.UNAUTHORIZED_401),
        ALREADY_FOLLOWING_USER(117, "You are already following this user!", HttpStatus.UNAUTHORIZED_401),
        NOT_FOLLOWING_USER(118, "You are not following this user!", HttpStatus.UNAUTHORIZED_401),
        CONTENT_TYPE_TOO_LONG(119, "Content type is too long (" + StaticRules.MAX_CONTENT_TYPE_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        CATEGORY_TOO_LONG(120, "Category is too long (" + StaticRules.MAX_CATEGORY_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        THUMBNAIL_URL_TOO_LONG(121, "Thumbnail URL is too long (" + StaticRules.MAX_THUMBNAIL_URL_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        CONTENT_DESCRIPTION_TOO_LONG(122, "Content description is too long (" + StaticRules.MAX_CONTENT_DESCRIPTION_LENGTH + " is the max)", HttpStatus.UNAUTHORIZED_401),
        NO_SUCH_CATEGORY(123, "The content type you are attempting to insert does not exist", HttpStatus.UNAUTHORIZED_401),
        DUPLICATE_CONTENT_TYPE(124, "The content type already exists", HttpStatus.UNAUTHORIZED_401),
        DUPLICATE_CATEGORY(125, "The content type already exists", HttpStatus.UNAUTHORIZED_401),
        NO_ENDPOINT_FOUND(126, "There was no endpoint found on this path. Make sure you're using the right method (GET,POST,etc.)", HttpStatus.NOT_FOUND_404),
        NOT_VALID_JSON_PAYLOAD(127, "All payloads are expected to be JSON only. The payload you entered was not valid JSON. Use http://jsonlint.com/ to validate your JSON if you are testing.", HttpStatus.BAD_REQUEST_400),
        ALREADY_FOLLOWING_CATEGORY(128, "You are already following this category!", HttpStatus.UNAUTHORIZED_401),
        ALREADY_PUBLISHING_CATEGORY(129, "You are already publishing in this category!", HttpStatus.UNAUTHORIZED_401),
        NOT_PUBLISHING_CATEGORY(130, "You are not publishing in this category!", HttpStatus.UNAUTHORIZED_401),
        USERID_NOT_NUMERIC(131, "User ID In URL is not a numeric value", HttpStatus.BAD_REQUEST_400),
        USER_NOT_FOUND(132, "User was not found", HttpStatus.NOT_FOUND_404),
		USER_NOT_EDITED(133, "User was not edited in profile edit", HttpStatus.BAD_REQUEST_400),
        CONTENT_NOT_FOUND(134, "The content you are trying to add to your list was not found", HttpStatus.NOT_FOUND_404),
        CONTENT_ALREADY_IN_BUNDLE(135, "The content you are trying to add to your list is already in the list", HttpStatus.BAD_REQUEST_400),
        COMMENT_TOO_LONG(136, "The comment you are trying to post is too long", HttpStatus.BAD_REQUEST_400),
        COMMENT_NOT_POSTED(137, "The comment could not be posted", HttpStatus.NOT_FOUND_404),
        COMMENTS_NOT_FOUND(138, "The comments for this video could not be loaded", HttpStatus.NOT_FOUND_404),
        EMAIL_IS_INVALID(139, "The provided email was formatted incorrectly.", HttpStatus.BAD_REQUEST_400),
        EMAIL_VERIFICATION_INVALID(140, "Email verification value must be 1 in payload", HttpStatus.BAD_REQUEST_400),
        EMAIL_TOKEN_INVALID(141, "The email token you have provided is invalid", HttpStatus.BAD_REQUEST_400),
        EMAIL_TOKEN_EXPIRED(142, "The email token you have provided is expired", HttpStatus.BAD_REQUEST_400),
        EMAIL_ALREADY_VERIFIED(143, "The user account has already validated their email", HttpStatus.BAD_REQUEST_400),
        EMAIL_VERIFICATION_NOT_SENT(144, "The email verification email needs to be resent", HttpStatus.NOT_FOUND_404),
        INVALID_ACTION(145, "The action you submitted in the payload is invalid", HttpStatus.BAD_REQUEST_400),
        RESET_FAILED(146, "The password reset was unsuccessful, please try again", HttpStatus.NOT_FOUND_404),
        RESET_TOKEN_INVALID(147, "The reset token provided was invalid.", HttpStatus.BAD_REQUEST_400),
        COULD_NOT_COUNT_FOLLOWERS(148, "Unable to count category followers.", HttpStatus.NOT_FOUND_404),
        COULD_NOT_RETRIEVE_FACEBOOK(149, "Unable to retrieve user's facebook information.", HttpStatus.NOT_FOUND_404),
        PEAK_PASSWORD_REQUIRED(150, "The user's Peak password is required, please provide it.", HttpStatus.BAD_REQUEST_400),
        COULD_NOT_PROCESS_FB_LOGIN(151, "Unable to process Peak authentication using Facebook", HttpStatus.NOT_FOUND_404),
        CONTENT_FOLLOWER_EMAIL_NOT_SENT(152, "Unable to send an email to the follower of the uploading user regarding new content.", HttpStatus.NOT_FOUND_404),
        COULD_NOT_INSERT_FEEDBACK(153, "Unable to insert feedback for this user", HttpStatus.NOT_FOUND_404),
        COULD_NOT_ADD_REPORTING_TYPE(154, "Unable to add new reporting type", HttpStatus.NOT_FOUND_404),
        DUPLICATE_REPORTING_TYPE(155, "This reporting type already exists", HttpStatus.NOT_FOUND_404),
        COULD_NOT_REPORT_USER(154, "Unable to insert report on this user", HttpStatus.NOT_FOUND_404),
        PROVIDE_DEVICE_DETAILS(155, "Unable to authenticate, please provide device details", HttpStatus.BAD_REQUEST_400),
        COULD_NOT_RETRIEVE_DEVICE_DETAILS(156, "Unable to retrieve details about user's device", HttpStatus.NOT_FOUND_404),
        COULD_NOT_SEND_DEVICE_NOTIFICATION(157, "Unable to send push notification to user's device", HttpStatus.NOT_FOUND_404);

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
        REG_INT_REQUIRED_ZERO("int", true, -1,Integer.MAX_VALUE),
        REG_STRING_REQUIRED("string", true, 1,255),
        REG_STRING_REQUIRED_UNLIMITED("string", true, 1,Integer.MAX_VALUE),
        REG_INT_OPTIONAL("int", false, 1,Integer.MAX_VALUE),
        REG_INT_OPTIONAL_ZERO("int", false, 0, Integer.MAX_VALUE),
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
