package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointInterface;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Pim de Witte(wwadewitte), Pim de Witte, Whitespell Inc., Whitespell LLC
 *         5/4/2015
 *         whitespell.model
 */
public class UserFollowAction implements EndpointInterface {

    private static final String FOLLOWING_USER_ID_KEY = "following_id";
    private static final String ACTION_KEY = "action";

    private static final String CHECK_FOLLOWING_QUERY = "SELECT 1 FROM `user_following` WHERE `user_id` = ? AND `following_id` = ? LIMIT 1";

    private static final String INSERT_FOLLOW_QUERY = "INSERT INTO `user_following`(`user_id`, `following_id`, `timestamp`) VALUES (?,?,?)";
    private static final String DELETE_FOLLOWED_QUERY = "DELETE FROM `user_following` WHERE `user_id` = ? AND `following_id` = ?";

    @Override
    public void call(RequestObject context) throws IOException {
        String context_user_id = context.getUrlVariables().get("user_id");

        JsonObject payload = context.getPayload().getAsJsonObject();

        /**
         * Check that the user id, following id, and action are valid.
         */
        if (!main.com.whitespell.peak.logic.Safety.isNumeric(context_user_id) || payload.get(FOLLOWING_USER_ID_KEY) == null || !main.com.whitespell.peak.logic.Safety.isNumeric(payload.get(FOLLOWING_USER_ID_KEY).getAsString()) || payload.get(ACTION_KEY) == null) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }


        String following_user_string = payload.get(FOLLOWING_USER_ID_KEY).getAsString();

        //local variables
        final int user_id = Integer.parseInt(context_user_id);
        final int following_user_id = Integer.parseInt(following_user_string);
        final String action = payload.get(ACTION_KEY).getAsString();
        final Timestamp now = new Timestamp(new Date().getTime());

        /**
         * Check that the user id and following user id are valid (>= 0 && <= Integer.MAX_VALUE).
         */
        if (!main.com.whitespell.peak.logic.Safety.isValidUserId(user_id) || !main.com.whitespell.peak.logic.Safety.isValidUserId(following_user_id)) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        /**
         * Check that the action being performed is valid.
         */

        boolean validAction = action.equalsIgnoreCase("follow") || action.equalsIgnoreCase("unfollow");

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

        if (!a.isAuthenticated() || a.getUserId() != user_id) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Create the {@link UserFollowAction.ActionResponse}.
         */
        final ActionResponse response = new ActionResponse();

        /**
         * Check to see if the user is already following the followed_user_id.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_FOLLOWING_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, String.valueOf(user_id));
                    ps.setString(2, String.valueOf(following_user_id));

                    ResultSet results = ps.executeQuery();
                    if (results.next()) {
                        response.setCurrentlyFollowing(true);
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }

        switch (action) {

            case "follow":

                /**
                 * If already following, throw error.
                 */

                if (response.isCurrentlyFollowing()) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ALREADY_FOLLOWING_USER);
                    return;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(INSERT_FOLLOW_QUERY);
                    executor.execute(new ExecutionBlock() {
                        @Override
                        public void process(PreparedStatement ps) throws SQLException {
                            ps.setString(1, String.valueOf(user_id));
                            ps.setString(2, String.valueOf(following_user_id));
                            ps.setString(3, now.toString());

                            ps.executeUpdate();

                            response.setSuccess(true);
                            response.setActionTaken("followed");
                        }
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                }
                break;

            case "unfollow":

                /**
                 * If not currently following, throw error.
                 */

                if (!response.isCurrentlyFollowing()) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_FOLLOWING_USER);
                    return;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(DELETE_FOLLOWED_QUERY);
                    executor.execute(new ExecutionBlock() {
                        @Override
                        public void process(PreparedStatement ps) throws SQLException {
                            ps.setString(1, String.valueOf(user_id));
                            ps.setString(2, String.valueOf(following_user_id));

                            ps.executeUpdate();

                            response.setSuccess(true);
                            response.setActionTaken("unfollowed");
                        }
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                }
                break;
        }


        /**
         * If the action taken was successfully performed then write the response.
         */
        if (response.isSuccess()) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            FollowActionObject followObject = new FollowActionObject();
            followObject.setActionTaken(response.getActionTaken());
            Gson g = new Gson();
            String json = g.toJson(followObject);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    private static class ActionResponse {

        private boolean success;
        private String actionTaken;
        private boolean currentlyFollowing;

        public ActionResponse() {
            this.success = false;
            this.currentlyFollowing = false;
            this.actionTaken = null;
        }

        public boolean isCurrentlyFollowing() {
            return currentlyFollowing;
        }

        public void setCurrentlyFollowing(boolean currentlyFollowing) {
            this.currentlyFollowing = currentlyFollowing;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getActionTaken() {
            return actionTaken;
        }

        public void setActionTaken(String actionTaken) {
            this.actionTaken = actionTaken;
        }

    }

    public class FollowActionObject {

        String action_taken;

        public String getActionTaken() {
            return this.action_taken;
        }

        public void setActionTaken(String action_taken) {
            this.action_taken = action_taken;
        }

    }


}