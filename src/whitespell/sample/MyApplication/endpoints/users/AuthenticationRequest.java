package whitespell.sample.MyApplication.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import sun.security.util.Password;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.SessionIdentifierGenerator;
import whitespell.logic.sql.Pool;
import whitespell.model.AuthenticationObject;
import whitespell.security.PasswordHash;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class AuthenticationRequest implements ApiInterface {


    private static final String RETRIEVE_PASSWORD = "SELECT `user_id`,`password` FROM `users` WHERE `username` = ? LIMIT 1";

    private static final String INSERT_AUTHENTICATION = "INSERT INTO `authentication`(`user_id`, `key`) " +
                                                        "VALUES (?,?)";

    public void call(RequestContext context) throws IOException {

        Connection con = null;
        String username;
        String password;

        JsonObject payload = context.getPayload().getAsJsonObject();

        /**
         * 400 Bad Request: Check if all data is valid
         */

        // Check if all parameters are present and contain the right characters, if not throw a 400
        if (payload == null || payload.get("username") == null || payload.get("password") == null) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        } else {
            username = payload.get("username").getAsString();
            password = payload.get("password").getAsString();
            // check against lengths for security and UX reasons.

            //check if values are too long
            if (username.length() > StaticRules.MAX_USERNAME_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.USERNAME_TOO_LONG);
                return;
            } else if (password.length() > StaticRules.MAX_PASSWORD_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.PASSWORD_TOO_LONG);
                return;
            } else if (username.length() < StaticRules.MIN_USERNAME_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.USERNAME_TOO_SHORT);
                return;
            } else if (password.length() < StaticRules.MIN_PASSWORD_LENGTH) {
                context.throwHttpError(StaticRules.ErrorCodes.PASSWORD_TOO_SHORT);
                return;
            }
        }

        con = null;

        try {
            con = Pool.getConnection();

            PreparedStatement p = null;
            try {

                p = con.prepareStatement(RETRIEVE_PASSWORD);
                p.setString(1, username);
                ResultSet s = p.executeQuery();
                if (s.next()) {
                    try {
                        boolean isVerified = PasswordHash.validatePassword(password, s.getString("password"));
                        if(isVerified) {
                            AuthenticationObject ao = new AuthenticationObject();
                            ao.setKey(SessionIdentifierGenerator.nextSessionId());
                            PreparedStatement insert_auth = con.prepareStatement(INSERT_AUTHENTICATION);
                            insert_auth.setInt(1, s.getInt("user_id"));
                            insert_auth.setString(2, ao.getKey());
                            insert_auth.executeUpdate();
                            Gson g = new Gson();
                            String jsonAo = g.toJson(ao);
                            // write the authentication object
                            context.getResponse().getWriter().write(jsonAo);
                            return;
                        } else {
                            context.throwHttpError(StaticRules.ErrorCodes.INVALID_USERNAME_OR_PASS);
                            return;
                        }
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                } else {
                    context.throwHttpError(StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                    return;
                }
            } finally {
                if (con != null)
                    con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

}
