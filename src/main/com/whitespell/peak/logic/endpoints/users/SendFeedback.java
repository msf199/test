package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.uservoice.Client;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/1/2015
 */

public class SendFeedback extends EndpointHandler {

    private static final String FEEDBACK_USER_ID_KEY = "userId";
    private static final String FEEDBACK_EMAIL_KEY = "email";
    private static final String FEEDBACK_MESSAGE_KEY = "message";

    private static final String INSERT_FEEDBACK = "INSERT INTO `feedback`(`feedback_message`,`email`) VALUES(?,?)";

    @Override
    protected void setUserInputs() {
        urlInput.put(FEEDBACK_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(FEEDBACK_EMAIL_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(FEEDBACK_MESSAGE_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject json = context.getPayload().getAsJsonObject();

        int userId = Integer.parseInt(context.getUrlVariables().get(FEEDBACK_USER_ID_KEY));
        String email = json.get(FEEDBACK_EMAIL_KEY).getAsString();
        String message = json.get(FEEDBACK_MESSAGE_KEY).getAsString();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (a.getUserId() == userId);

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Update DB with feedback from user
         */
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_FEEDBACK);

            executor.execute(ps -> {
                ps.setString(1, message);
                ps.setString(2, email);
                int rows = ps.executeUpdate();

                /**
                 * feedback details updated in table
                 */
                if (rows <= 0) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_INSERT_FEEDBACK);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Authenticate into UserVoice API, post the suggestion to Peak Feedback forum
         */
        try {
            Client client = new Client(Config.USERVOICE_SUBDOMAIN,
                    Config.USERVOICE_APP_KEY, Config.USERVOICE_APP_SECRET);

            Client accessToken = client.loginAs(email);

            /**
             * Get forumId of most recently created forum.
             */
            Integer forumId = accessToken.getCollection("/api/v1/forums", 1).get(0).getInt("id");

            /**
             * Post the feedback as a "suggestion" to the forum
             */
            JSONObject suggestion = accessToken.post("/api/v1/forums/" + forumId + "/suggestions",
                    new HashMap<String, Object>() {
                        {
                            put("suggestion", new HashMap<String, Object>() {
                                {
                                    put("title", message);
                                }
                            });
                        }
                    }).getJSONObject("suggestion");
            }catch(Exception e){
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_INSERT_FEEDBACK);
                return;
        }

        feedbackSuccessObject f = new feedbackSuccessObject();
        f.setSuccess(true);

        Gson g = new Gson();
        String response = g.toJson(f);
        context.getResponse().setStatus(200);
        try {
            context.getResponse().getWriter().write(response);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public static class feedbackSuccessObject {

        boolean success;

        feedbackSuccessObject(){
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

    }
}
