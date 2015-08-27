package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import facebook4j.*;
import facebook4j.conf.ConfigurationBuilder;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;
import main.com.whitespell.peak.security.PasswordHash;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/26/2015
 *         whitespell.model
 */
public class LinkFB extends EndpointHandler {

    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String PASSWORD_KEY = "password";

    private static final String RETRIEVE_FB_USER_QUERY = "SELECT `link_timestamp` FROM `fb_user` WHERE `user_id` = ?";
    private static final String RETRIEVE_USERID_QUERY = "SELECT `user_id`, `username`, `email` from `user` WHERE `username` = ? OR `email` = ?";

    private static final String UPDATE_FB_LINK_QUERY = "UPDATE `user` SET `fb_link` = ? WHERE `user_id` = ?";

    private static final String INSERT_USER_QUERY = "INSERT INTO `user`(`username`,`password`,`email`) VALUES (?,?,?)";
    private static final String INSERT_FB_USER_QUERY = "INSERT INTO `fb_user`(`user_id`,`link_timestamp`) VALUES (?,?)";

    @Override
    protected void setUserInputs() {
        payloadInput.put(ACCESS_TOKEN_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();
        com.mashape.unirest.http.HttpResponse<String> stringResponse;
        boolean[] newUser = {false};
        boolean[] newFbUser = {false};

        int[] userId = {0};
        String username;
        String email;
        String accessToken = payload.get(ACCESS_TOKEN_KEY).getAsString();
        String payloadPass = null;
        if(payload.get(PASSWORD_KEY) != null){
            payloadPass = payload.get(PASSWORD_KEY).getAsString();
        }

        String passHash;

        /**
         * Get the temp password for the fb user
         */
        try {
            passHash = PasswordHash.createHash(accessToken);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Configure Facebook API for login
         */
        /**
         * Get the facebook account and user's information
         */
        try {
            //version v2.4
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthAppId(Config.FB_APP_ID)
                    .setOAuthAppSecret(Config.FB_APP_SECRET)
                    .setOAuthAccessToken(accessToken)
                    .setOAuthPermissions("email,public_profile");
            FacebookFactory ff = new FacebookFactory(cb.build());
            Facebook facebook = ff.getInstance();

            User user = facebook.getUser(facebook.getId(), new Reading().fields("email"));
            email = user.getEmail();

            String split[] = email.split("@");
            username = split[0];
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_RETRIEVE_FACEBOOK);
            return;
        }

        try {
            /**
             * Check for user, create one if it doesn't exist
             */
            StatementExecutor executor = new StatementExecutor(RETRIEVE_USERID_QUERY);

            executor.execute(ps -> {
                ps.setString(1, username);
                ps.setString(2, email);
                final ResultSet s = ps.executeQuery();

                if (s.next()) {
                    userId[0] = s.getInt("user_id");
                } else {
                    /**
                     * Create new account
                     */
                    newUser[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }


        if (newUser[0]) {
            /**
             * Create a new user
             */
            try {
                final String finalUsername = username;
                final String finalPassword = passHash;
                final String finalEmail = email;
                StatementExecutor executor2 = new StatementExecutor(INSERT_USER_QUERY);

                executor2.execute(ps2 -> {
                    ps2.setString(1, finalUsername);
                    ps2.setString(2, finalPassword);
                    ps2.setString(3, finalEmail);
                    int rows = ps2.executeUpdate();

                    /**
                     * User inserted with email, 'password' and username
                     */
                    if (rows <= 0) {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                });
            } catch (Exception e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }

            /**
             * Get the userId
             */
            try {
                StatementExecutor executor = new StatementExecutor(RETRIEVE_USERID_QUERY);

                executor.execute(ps -> {
                    ps.setString(1, username);
                    ps.setString(2, email);
                    final ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        userId[0] = s.getInt("user_id");
                    }
                });
            } catch (Exception e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }

        try {
            /**
             * Check for existing fb_user record, if no add a record
             */
            StatementExecutor executor = new StatementExecutor(RETRIEVE_FB_USER_QUERY);

            executor.execute(ps -> {
                ps.setInt(1, userId[0]);
                final ResultSet s = ps.executeQuery();

                if (!s.next()) {
                    newFbUser[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        if(newFbUser[0]) {
            /**
             * Insert fb_users record
             */
            try {
                StatementExecutor executor = new StatementExecutor(INSERT_FB_USER_QUERY);

                executor.execute(ps -> {
                    ps.setInt(1, userId[0]);
                    ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    int rows = ps.executeUpdate();

                    /**
                     * User inserted into fb_users table
                     */
                    if (rows <= 0) {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }


        try {
            final int fbLink = 1;

            StatementExecutor executor = new StatementExecutor(UPDATE_FB_LINK_QUERY);

            executor.execute(ps -> {
                ps.setInt(1, fbLink);
                ps.setInt(2, userId[0]);
                int rows = ps.executeUpdate();

                /**
                 * fb_link updated in user table
                 */
                if (rows <= 0) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Authenticate the user using the Fb access token
         */
        if (newUser[0] && newFbUser[0] || !newUser[0] && !newFbUser[0]) {
            try {
                stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                        .header("accept", "application/json")
                        .body("{\n" +
                                "\"userName\":\"" + username + "\",\n" +
                                "\"password\" : \"" + accessToken + "\"\n" +
                                "}")
                        .asString();

                Gson g = new Gson();
                AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
                String json = g.toJson(a);
                try {
                    context.getResponse().getWriter().write(json);
                } catch (IOException e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            } catch (Exception e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }
        else if(payloadPass != null){
            try {
                stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                        .header("accept", "application/json")
                        .body("{\n" +
                                "\"userName\":\"" + username + "\",\n" +
                                "\"password\" : \"" + payloadPass + "\"\n" +
                                "}")
                        .asString();

                Gson g = new Gson();
                AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
                String json = g.toJson(a);
                try {
                    context.getResponse().getWriter().write(json);
                } catch (IOException e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            } catch (Exception e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }
    }
}
