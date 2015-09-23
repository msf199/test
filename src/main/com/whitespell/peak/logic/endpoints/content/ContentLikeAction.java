package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/14/15
 */
public class ContentLikeAction extends EndpointHandler {

    private static final String INSERT_LIKE_ACTION_QUERY = "INSERT INTO `content_likes`(`user_id`, `content_id`, `like_datetime`) VALUES (?,?,?)";

    private static final String URL_CONTENT_LIKE_ID = "contentId";
    private static final String PAYLOAD_ACTION_KEY = "action";

    private static final String PAYLOAD_USER_ID_KEY = "userId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_LIKE_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_ACTION_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    private static final String CHECK_LIKE_QUERY = "SELECT * FROM `content_likes` WHERE `user_id` = ? AND `content_id` = ? LIMIT 1";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM `content_likes` WHERE `user_id` = ? AND `content_id` = ?";

    @Override
    public void safeCall(RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        int content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_LIKE_ID));

        //local variables
        final int user_id = payload.get(PAYLOAD_USER_ID_KEY).getAsInt();
        final String action = payload.get(PAYLOAD_ACTION_KEY).getAsString();
        final Timestamp now = new Timestamp(Server.getCalendar().getTimeInMillis());

        /**
         * Check that the action being performed is valid.
         */

        boolean validAction = action.equalsIgnoreCase("like") || action.equalsIgnoreCase("unlike");
        ActionResponse ar = new ActionResponse();

        /**
         * If the action is invalid throw a null value error.
         */

        if (!validAction) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = a.getUserId() == user_id;

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Check to see if the user already liked the contentId.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_LIKE_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, user_id);
                ps.setInt(2, content_id);

                ResultSet results = ps.executeQuery();
                if (results.next()) {
                    ar.setCurrentlyLiked(true);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        switch (action) {

            case "like":
                if(ar.isCurrentlyLiked()){
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INVALID_ACTION);
                    return;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(INSERT_LIKE_ACTION_QUERY);
                    executor.execute(ps -> {
                        ps.setInt(1, user_id);
                        ps.setInt(2, content_id);
                        ps.setString(3, now.toString());

                        ps.executeUpdate();
                        ar.setSuccess(true);
                        ar.setActionTaken("like");
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
                break;

            case "unlike":
                try {
                    if(!ar.isCurrentlyLiked()){
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INVALID_ACTION);
                        return;
                    }
                    StatementExecutor executor = new StatementExecutor(DELETE_LIKE_QUERY);
                    executor.execute(ps -> {
                        ps.setString(1, String.valueOf(user_id));
                        ps.setString(2, String.valueOf(content_id));

                        ps.executeUpdate();

                        ar.setSuccess(true);
                        ar.setActionTaken("unlike");
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
                break;
        }

        /**
         * If the action taken was successfully performed then write the response.
         */
        if (ar.isSuccess()) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            LikeActionObject likeActionObject = new LikeActionObject();
            likeActionObject.setActionTaken(ar.getActionTaken());
            Gson g = new Gson();
            String json = g.toJson(likeActionObject);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    private static class ActionResponse {

        private boolean success;
        private String actionTaken;
        private boolean currentlyLiked;

        public ActionResponse() {
            this.success = false;
            this.actionTaken = null;
            this.currentlyLiked = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }


        public boolean isCurrentlyLiked() {
            return currentlyLiked;
        }

        public void setCurrentlyLiked(boolean currentlyLiked) {
            this.currentlyLiked = currentlyLiked;
        }

        public String getActionTaken() {
            return actionTaken;
        }

        public void setActionTaken(String actionTaken) {
            this.actionTaken = actionTaken;
        }

    }

    public class LikeActionObject {

        String action_taken;

        public String getActionTaken() {
            return this.action_taken;
        }

        public void setActionTaken(String action_taken) {
            this.action_taken = action_taken;
        }
    }
}
