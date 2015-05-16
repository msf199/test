package whitespell.peakapi.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.logging.Logging;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.Pool;
import whitespell.logic.sql.StatementExecutor;
import whitespell.model.UserObject;
import whitespell.security.PasswordHash;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 *         https://docs.google.com/document/d/1j62zQ3AIfh7XW0nbftRy_hNXTAnhy5r48yBRHm7kYZA/edit
 */

public class CreateUser implements ApiInterface {

    private static final String INSERT_USER_QUERY = "INSERT INTO `users`(`password`, `email`, `username`, `publisher`) " +
            "VALUES (?,?,?, ?)";

    private static final String CHECK_USERNAME_QUERY = "SELECT `user_id` FROM `users` WHERE `username` = ? LIMIT 1";
    private static final String CHECK_USERNAME_OR_EMAIL_QUERY = "SELECT `username`, `email` FROM `users` WHERE `username` = ? OR `email` = ? LIMIT 1";

    @Override
    public void call(final RequestContext context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

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

        // Check if all parameters are present and contain the right characters, if not throw a 400
        if (payload == null || payload.get("username") == null || payload.get("email") == null || payload.get("password") == null) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        } else {
            username = payload.get("username").getAsString();
            password = payload.get("password").getAsString();
            email = payload.get("email").getAsString();

            if(payload.get("publisher") != null && payload.get("publisher").getAsInt() == 1) {
                publisher = 1;
            }

            // check against lengths for security and UX reasons.

            //check if values are too long
            if (username.length() > StaticRules.MAX_USERNAME_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.USERNAME_TOO_LONG);
                return;
            } else if (email.length() > StaticRules.MAX_EMAIL_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.EMAIL_TOO_LONG);
                return;
            } else if (password.length() > StaticRules.MAX_PASSWORD_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.PASSWORD_TOO_LONG);
                return;
            } else if (username.length() < StaticRules.MIN_USERNAME_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.USERNAME_TOO_SHORT);
                return;
            } else if (email.length() < StaticRules.MIN_EMAIL_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.EMAIL_TOO_SHORT);
                return;
            } else if (password.length() < StaticRules.MIN_PASSWORD_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.PASSWORD_TOO_SHORT);
                return;
            }
        }

        /**
         * //todo(pim) set off email to a separate email checker thread that validates the email through mailserver checks and such.
         */

        /**
         * 401 Unauthorized: Check if username exists
         */


        try {
            StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_OR_EMAIL_QUERY);
            final String finalUsername = username;
            final String finalEmail = email;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalUsername);
                    ps.setString(2, finalEmail);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        if (s.getString("username").equalsIgnoreCase(finalUsername)) {
                            context.throwHttpError(StaticRules.ErrorCodes.USERNAME_TAKEN);
                        } else if (s.getString("email").equalsIgnoreCase(finalEmail)) {
                            context.throwHttpError(StaticRules.ErrorCodes.EMAIL_TAKEN);
                        }
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            return;
        }


        // Generate hash the user's password hash string (Which will result ITERATION:SALT:HASH). When we check against the password, we check it like this:
        // isValid({userpass}, databaseResult(ITERATION:SALT:HASH) and the function checks against the same salt and hash as in the database result.

        try {
            passHash = PasswordHash.createHash(password);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
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
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalPassHash);
                    ps.setString(2, finalEmail);
                    ps.setString(3, finalUsername);
                    ps.setInt(4, finalPublisher);

                    /**
                     * //todo(pim) check if email checker returned positively, if so, insert into DB, if not, reject request with 403 forbidden and set success to false.
                     */
                    ps.executeUpdate();
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_QUERY);
            final String finalUsername = username;
            final String finalEmail = email;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalUsername);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        context.getResponse().setStatus(HttpStatus.OK_200);
                        UserObject uo = new UserObject();
                        uo.setUserId(s.getInt("user_id"));
                        uo.setEmail(finalEmail);
                        uo.setUsername(finalUsername);
                        Gson g = new Gson();
                        String json = g.toJson(uo);
                        try {
                            context.getResponse().getWriter().write(json);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

}
