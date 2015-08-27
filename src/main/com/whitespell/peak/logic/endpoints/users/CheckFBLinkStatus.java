package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.User;
import facebook4j.conf.ConfigurationBuilder;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/26/2015
 *         whitespell.model
 */
public class CheckFBLinkStatus extends EndpointHandler {

    private static final String ACCESS_TOKEN_KEY = "accessToken";

    private static final String RETRIEVE_FB_USER_QUERY = "SELECT `link_timestamp` FROM `fb_user` WHERE `user_id` = ?";
    private static final String RETRIEVE_USERID_QUERY = "SELECT `user_id`, `username`, `email` from `user` WHERE `username` = ? OR `email` = ?";

    @Override
    protected void setUserInputs() {
        payloadInput.put(ACCESS_TOKEN_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();
        boolean[] newPeakUser = {false};
        boolean[] newFbUser = {false};

        int[] userId = {0};
        String username;
        String email;
        String accessToken = payload.get(ACCESS_TOKEN_KEY).getAsString();

        /**
         * Configure Facebook API for check
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
             * Check for peak user
             */
            StatementExecutor executor = new StatementExecutor(RETRIEVE_USERID_QUERY);

            executor.execute(ps -> {
                ps.setString(1, username);
                ps.setString(2, email);
                final ResultSet s = ps.executeQuery();

                if (!s.next()) {
                    newPeakUser[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        try {
            /**
             * Check for existing fb_user record
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

        FBLinkStatusResponse status = new FBLinkStatusResponse();

        if(newPeakUser[0] && !newFbUser[0]){
            //only peak user, type 1
            status.setPasswordRequired(true);
        }
        else if(newPeakUser[0] && newFbUser[0]){
            //100% new user, type 2
            status.setPasswordRequired(false);
        } else if(!newFbUser[0] && !newPeakUser[0]){
            //merged user, type 3
            status.setPasswordRequired(false);
        }

        Gson g = new Gson();
        String json = g.toJson(status);
        try {
            context.getResponse().getWriter().write(json);
        } catch (IOException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
            return;
        }
    }

    public class FBLinkStatusResponse {

        public FBLinkStatusResponse(){
            passwordRequired = false;
        }

        public boolean isPasswordRequired() {
            return passwordRequired;
        }

        public void setPasswordRequired(boolean passwordRequired) {
            this.passwordRequired = passwordRequired;
        }

        private boolean passwordRequired;
    }
}
