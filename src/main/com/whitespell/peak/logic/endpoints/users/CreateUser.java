package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;
import main.com.whitespell.peak.security.PasswordHash;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import static main.com.whitespell.peak.logic.EmailSend.*;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */

public class CreateUser extends EndpointHandler {

    private static final String INSERT_USER_QUERY = "INSERT INTO `user`(`password`, `email`, `username`, `publisher`) " +
            "VALUES (?,?,?,?)";

    private static final String PAYLOAD_USERNAME_KEY = "userName";
    private static final String PAYLOAD_PASSWORD_KEY = "password";
    private static final String PAYLOAD_EMAIL_KEY = "email";
    private static final String PAYLOAD_PUBLISHER_KEY = "publisher";


    @Override
    protected void setUserInputs() {

        payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_EMAIL_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    private static final String CHECK_USERNAME_QUERY = "SELECT `user_id`, `username`, `email`, `publisher` FROM `user` WHERE `username` = ? LIMIT 1";
    private static final String CHECK_USERNAME_OR_EMAIL_QUERY = "SELECT `username`, `email`, `publisher` FROM `user` WHERE `username` = ? OR `email` = ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();
        boolean isValid;

        // common variables
        String username = null;
        String password = null;
        String email = null;
        String passHash = null;
        int publisher = 0;

        //initialize as arrays so that they can be allocated
        final int[] user_id = {-1};

        /**
         * 400 Bad Request: Check if all data is valid
         */

        username = payload.get(PAYLOAD_USERNAME_KEY).getAsString();
        password = payload.get(PAYLOAD_PASSWORD_KEY).getAsString();
        email = payload.get(PAYLOAD_EMAIL_KEY).getAsString();
        isValid = Pattern.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", email);

        if (payload.get(PAYLOAD_PUBLISHER_KEY) != null && payload.get(PAYLOAD_PUBLISHER_KEY).getAsInt() == 1) {
            publisher = 1;
        }

        /**
         * Check username, email and password requirements
         */

        if (username.length() > StaticRules.MAX_USERNAME_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERNAME_TOO_LONG);
            return;
        } else if (password.length() > StaticRules.MAX_PASSWORD_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_LONG);
            return;
        } else if (username.length() < StaticRules.MIN_USERNAME_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERNAME_TOO_SHORT);
            return;
        } else if (password.length() < StaticRules.MIN_PASSWORD_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_SHORT);
            return;
        } else if (!isValid){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_IS_INVALID);
        }

        /**
         * 401 Unauthorized: Check if username exists
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_OR_EMAIL_QUERY);
            final String finalUsername = username;
            final String finalEmail = email;
            final boolean[] returnCall = {false};
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalUsername);
                    ps.setString(2, finalEmail);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        if (s.getString("username").equalsIgnoreCase(finalUsername)) {
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERNAME_TAKEN);
                        } else if (s.getString("email").equalsIgnoreCase(finalEmail)) {
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_TAKEN);
                        }
                        returnCall[0] = true;
                        return;
                    }
                }
            });

            /**
             * This check is put in place if the request should be closed and no more logic should be executed after the query result is received. Because it is a nested function, we need to check with a finalized array
             * whether we should return, and do so if necessary.
             */
            if (returnCall[0]) {
                return;
            }
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }



        // Generate hash the user's password hash string (Which will result ITERATION:SALT:HASH). When we check against the password, we check it like this:
        // isValid({userpass}, databaseResult(ITERATION:SALT:HASH) and the function checks against the same salt and hash as in the database result.
        try {
            passHash = PasswordHash.createHash(password);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Insert the user in the database and return the user object on success, fail with 500 if it fails
         */

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_USER_QUERY);
            final String finalUsername = username;
            final String finalEmail = email;
            final String finalPassHash = passHash;
            final int finalPublisher = publisher;
            executor.execute(ps -> {
                ps.setString(1, finalPassHash);
                ps.setString(2, finalEmail);
                ps.setString(3, finalUsername);
                ps.setInt(4, finalPublisher);

                /**
                 * //todo(pim) check if email checker returned positively, if so, insert into DB, if not, reject request with 403 forbidden and set success to false.
                 */
                ps.executeUpdate();
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_QUERY);
            final String finalUsername = username;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalUsername);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        context.getResponse().setStatus(HttpStatus.OK_200);
                        UserObject uo = new UserObject();
                        uo.setUserId(s.getInt("user_id"));
                        uo.setEmail(s.getString("email"));
                        uo.setUserName(s.getString("username"));
                        uo.setPublisher(s.getInt("publisher"));
                        Gson g = new Gson();
                        String json = g.toJson(uo);
                        try {
                            context.getResponse().getWriter().write(json);
                        } catch (IOException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                            return;
                        }
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * We will use a verification email that sends a token in a link which the user will click to verify their email.
         * Until they verify their email they will be unable to access certain features of the app.
         */
        /**
         * Update the user's email verification status in the database.
         */
        updateDBandSendWelcomeEmail(username, email);
    }
}
