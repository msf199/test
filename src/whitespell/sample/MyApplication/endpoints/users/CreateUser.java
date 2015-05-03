package whitespell.sample.MyApplication.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.sql.Pool;
import whitespell.model.ErrorObject;
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

    private static final String INSERT_USER_QUERY = "INSERT INTO `users`(`password`, `email`, `username`) " +
            "VALUES (?,?,?)";

    private static final String CHECK_USERNAME_QUERY = "SELECT `user_id` FROM `users` WHERE `username` = ? LIMIT 1";
    private static final String CHECK_USERNAME_OR_EMAIL_QUERY = "SELECT `username`, `email` FROM `users` WHERE `username` = ? OR `email` = ? LIMIT 1";


    public void call(RequestContext context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();


        String username = null;
        String password = null;
        String email = null;
        String passHash = null;
        int user_id = -1;
        boolean usernameExists = false;
        boolean emailExists = false;
        boolean success = false;

        /**
         * 400 Bad Request: Check if all data is valid
         */

        // Check if all parameters are present and contain the right characters, if not throw a 400
        if (payload == null || payload.get("username") == null || payload.get("email") == null || payload.get("password") == null) {
            context.getResponse().setStatus(HttpStatus.BAD_REQUEST_400);
            return;
        } else {
            username = payload.get("username").getAsString();
            password = payload.get("password").getAsString();
            email = payload.get("email").getAsString();

            // check against lengths for security and UX reasons.

            if (username.length() > StaticRules.MAX_USERNAME_LENGTH
                    || email.length() > StaticRules.MAX_EMAIL_LENGTH
                    || password.length() > StaticRules.MAX_PASSWORD_LENGTH) {
                context.getResponse().setStatus(HttpStatus.BAD_REQUEST_400);
                return;
            }
        }

        /**
         * //todo(pim) set off email to a separate email checker thread that validates the email through mailserver checks and such.
         */

        /**
         * 401 Unauthorized: Check if username exists
         */

        // Get a connection from the pool
        Connection con = null;
        try {
            con = Pool.getConnection();

            PreparedStatement p = null;
            try {
                p = con.prepareStatement(CHECK_USERNAME_OR_EMAIL_QUERY);
                p.setString(1, username);
                p.setString(2, email);
                ResultSet s = p.executeQuery();
                if (s.next()) {
                    if (s.getString("username").equalsIgnoreCase(username)) {
                        usernameExists = true;
                    } else if (s.getString("email").equalsIgnoreCase(email)) {
                        emailExists = true;
                    } else {
                        // this should never happen
                        throw new Exception("Query to check username or email came back with results, but doesn't match username or email");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null)
                    con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // set response to 401 if username exists.
        if (usernameExists || emailExists) {
            ErrorObject eo = new ErrorObject();
            if(usernameExists && emailExists) {
                eo.setErrorId(StaticRules.ErrorCodes.USERNAME_AND_EMAIL_TAKEN.getErrorId());
                eo.setErrorMessage(StaticRules.ErrorCodes.USERNAME_AND_EMAIL_TAKEN.getErrorMessage());
            } else if(usernameExists) {
                eo.setErrorId(StaticRules.ErrorCodes.USERNAME_TAKEN.getErrorId());
                eo.setErrorMessage(StaticRules.ErrorCodes.USERNAME_TAKEN.getErrorMessage());
            } else if(emailExists) {
                eo.setErrorId(StaticRules.ErrorCodes.EMAIL_TAKEN.getErrorId());
                eo.setErrorMessage(StaticRules.ErrorCodes.EMAIL_TAKEN.getErrorMessage());
            }

            context.getResponse().setStatus(HttpStatus.UNAUTHORIZED_401);

            Gson g = new Gson();
            String errorObject = g.toJson(eo);
            context.getResponse().getWriter().write(errorObject);
            return;
        }

        // Generate hash the user's password hash string (Which will result ITERATION:SALT:HASH). When we check against the password, we check it like this:
        // isValid({userpass}, databaseResult(ITERATION:SALT:HASH) and the function checks against the same salt and hash as in the database result.

        try {
            passHash = PasswordHash.createHash(password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * Insert the user in the database and return the user object on success, fail with 500 if it fails
         */

        // Get a connection from the pool
        con = null;

        try {
            con = Pool.getConnection();

            PreparedStatement p = null;
            try {
                p = con.prepareStatement(INSERT_USER_QUERY);
                p.setString(1, password);
                p.setString(2, email);
                p.setString(3, username);

                /**
                 * //todo(pim) check if email checker returned positively, if so, insert into DB, if not, reject request with 403 forbidden and set success to false.
                 */
                p.executeUpdate();

                p = con.prepareStatement(CHECK_USERNAME_QUERY);
                p.setString(1, username);
                ResultSet s = p.executeQuery();
                if (s.next()) {
                    success = true;
                    user_id = s.getInt("user_id");
                }
            } finally {
                if (con != null)
                    con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (success) {
            // successful, set authentication session that logs the IP address and other details, and write the JSON object.
            context.getResponse().setStatus(HttpStatus.OK_200);
            UserObject uo = new UserObject();
            uo.setUserId(user_id);
            uo.setEmail(email);
            uo.setUsername(username);
            Gson g = new Gson();
            String json = g.toJson(uo);
            context.getResponse().getWriter().write(json);
            System.out.println(json);
        } else {
            // something failed, but we don't know what, write internal server error and log error.
            context.getResponse().setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }


    }
}
