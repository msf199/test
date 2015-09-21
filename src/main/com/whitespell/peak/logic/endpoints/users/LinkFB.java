package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.User;
import facebook4j.conf.ConfigurationBuilder;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EmailSend;
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
 */
public class LinkFB extends EndpointHandler {

    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String PASSWORD_KEY = "password";
    private static final String PAYLOAD_DEVICE_NAME_KEY = "deviceName";
    private static final String PAYLOAD_DEVICE_TYPE_KEY = "deviceType";
    private static final String PAYLOAD_DEVICE_UUID_KEY = "deviceUUID";

    private static final String RETRIEVE_FB_USER_QUERY = "SELECT `link_timestamp` FROM `fb_user` WHERE `user_id` = ?";
    private static final String RETRIEVE_USERID_QUERY = "SELECT `user_id`, `username`, `email` from `user` WHERE `email` = ?";

    private static final String UPDATE_FB_LINK_QUERY = "UPDATE `user` SET `fb_link` = ? WHERE `user_id` = ?";
    private static final String UPDATE_USER_PASS_QUERY = "UPDATE `user` SET `password` = ? WHERE `user_id` = ?";
    private static final String UPDATE_EMAIL_VERIFICATION = "UPDATE `user` SET `email_verified` = ?, `email_token` = ?, `email_expiration` = ? WHERE `username` = ?";

    private static final String INSERT_USER_QUERY = "INSERT INTO `user`(`username`,`password`,`email`) VALUES (?,?,?)";
    private static final String INSERT_FB_USER_QUERY = "INSERT INTO `fb_user`(`user_id`,`link_timestamp`) VALUES (?,?)";

    @Override
    protected void setUserInputs() {
        payloadInput.put(ACCESS_TOKEN_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED_UNLIMITED);
        payloadInput.put(PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_DEVICE_NAME_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_DEVICE_UUID_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_DEVICE_TYPE_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL_ZERO);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();
        com.mashape.unirest.http.HttpResponse<String> stringResponse;
        boolean[] newPeakUser = {false};
        boolean[] newFbUser = {false};

        int[] userId = {0};
        String[] authUsername ={null};
        String username = "testuser";
        String email = "test@thisisatestemail101.com";
        String accessToken = payload.get(ACCESS_TOKEN_KEY).getAsString();
        String payloadPass = null;
        String[] deviceName = {"unknown"};
        String[] deviceUUID = {"unknown" + System.currentTimeMillis()};
        int[] deviceType = {-1};
        boolean device1 = false, device2 = false, device3 = false;

        if(payload.get(PASSWORD_KEY) != null){
            payloadPass = payload.get(PASSWORD_KEY).getAsString();
        }

        if(payload.get(PAYLOAD_DEVICE_NAME_KEY) != null) {
            deviceName[0] = payload.get(PAYLOAD_DEVICE_NAME_KEY).getAsString();
            device1 = true;
        }

        if(payload.get(PAYLOAD_DEVICE_UUID_KEY) != null) {
            deviceUUID[0] = payload.get(PAYLOAD_DEVICE_UUID_KEY).getAsString();
            device2 = true;
        }

        if(payload.get(PAYLOAD_DEVICE_TYPE_KEY) != null){
            deviceType[0] = payload.get(PAYLOAD_DEVICE_TYPE_KEY).getAsInt();
            device3 = true;
        }

        String passHash;

        /**
         * Ensure all device details are provided
         */
        if(device1 || device2 || device3){
            if(!device1 || !device2 || !device3){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PROVIDE_DEVICE_DETAILS);
                return;
            }
        }

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
            if(user.getEmail() != null) {
                email = user.getEmail();
                String split[] = email.split("@");
                username = split[0];
            }
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
            final String finalEmail = email;
            executor.execute(ps -> {
                ps.setString(1, finalEmail);
                final ResultSet s = ps.executeQuery();

                if (s.next()) {
                    userId[0] = s.getInt("user_id");
                    authUsername[0] = s.getString("username");
                } else {
                    /**
                     * Create new account
                     */
                    newPeakUser[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }


        if (newPeakUser[0]) {
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
                final String finalEmail = email;
                executor.execute(ps -> {
                    ps.setString(1, finalEmail);
                    final ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        userId[0] = s.getInt("user_id");
                        authUsername[0] = s.getString("username");
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

        /**
         * Ensure a Peak only user linking to FB uses their Peak password.
         */
        if(!newPeakUser[0] && newFbUser[0] && payloadPass == null){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PEAK_PASSWORD_REQUIRED);
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

        /**
         * Ensure that any user that has already linked their account to FB can log in with 1 click
         */
        if(!newFbUser[0] && !newPeakUser[0]){
            try {
                final String finalPassword = passHash;
                StatementExecutor executor = new StatementExecutor(UPDATE_USER_PASS_QUERY);

                executor.execute(ps -> {
                    ps.setString(1, finalPassword);
                    ps.setInt(2, userId[0]);
                    int rows = ps.executeUpdate();

                    /**
                     * User password updated to accessToken because they are a merged account
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
         * If user is Peak only and logging in with FB for first time,
         * use provided Peak password to authenticate.
         */
        if(payloadPass != null && (!newPeakUser[0] && newFbUser[0])){
            try {
                stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                        .header("accept", "application/json")
                        .body("{\n" +
                                "\"userName\":\"" + authUsername[0] + "\",\n" +
                                "\"password\" : \"" + payloadPass + "\",\n" +
                                "\"deviceName\":\"" + deviceName[0] + "\",\n" +
                                "\"deviceUUID\":\"" + deviceUUID[0] + "\",\n" +
                                "\"deviceType\":\"" + deviceType[0] + "\"\n" +
                                "}")
                        .asString();

                Gson g = new Gson();
                AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);

                if(a.getKey() == null){
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
                    return;
                }

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
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_PROCESS_FB_LOGIN);
                return;
            }
        }else if ((newPeakUser[0] && newFbUser[0]) || (!newPeakUser[0] && !newFbUser[0])) {
            /**
             * If user is either completely new to Peak or already has merged their account with FB
             * authenticate using the FB access token.
             */
            if((newPeakUser[0] && newFbUser[0])){
                /**
                 * Update the user's email verification status in the database, reset email expiration and token to null.
                 */
                try {
                    StatementExecutor executor = new StatementExecutor(UPDATE_EMAIL_VERIFICATION);
                    final String finalUsername = username;
                    final int finalEmailVerification = 1;
                    final String finalEmailToken = null;
                    final String finalEmailExpiration = null;
                    executor.execute(ps -> {
                        ps.setInt(1, finalEmailVerification);
                        ps.setString(2, finalEmailToken);
                        ps.setString(3, finalEmailExpiration);
                        ps.setString(4, finalUsername);

                        ps.executeUpdate();
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            }
            /**
             * Authenticate the user using FB Access Token
             */
            try {
                stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                        .header("accept", "application/json")
                        .body("{\n" +
                                "\"userName\":\"" + authUsername[0] + "\",\n" +
                                "\"password\" : \"" + accessToken + "\",\n" +
                                "\"deviceName\":\"" + deviceName[0] + "\",\n" +
                                "\"deviceUUID\":\"" + deviceUUID[0] + "\",\n" +
                                "\"deviceType\":\"" + deviceType[0] + "\"\n" +
                                "}")
                        .asString();

                Gson g = new Gson();
                AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);

                if(a.getKey() == null){
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
                    return;
                }

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
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_PROCESS_FB_LOGIN);
                return;
            }
        }
        else{
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_PROCESS_FB_LOGIN);
            return;
        }
    }
}
