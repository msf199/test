package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
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
public class CategoryFollowAction implements EndpointInterface {

    private static final String CATEGORY_ID_KEY = "category_id";
    private static final String ACTION_KEY = "action";

    private static final String CHECK_FOLLOWING_CATEGORY_QUERY = "SELECT 1 FROM `category_following` WHERE `user_id` = ? AND `category_id` = ? LIMIT 1";

    private static final String INSERT_FOLLOW_CATEGORY_QUERY = "INSERT INTO `category_following`(`user_id`, `category_id`, `timestamp`) VALUES (?,?,?)";
    private static final String DELETE_FOLLOW_CATEGORY_QUERY = "DELETE FROM `category_following` WHERE `user_id` = ? AND `category_id` = ?";

    @Override
    public void call(RequestObject context) throws IOException {
        String context_user_id = context.getUrlVariables().get("user_id");

        JsonObject payload = context.getPayload().getAsJsonObject();

        /**
         * Check that the user id, following id, and action are valid.
         */
        if (!main.com.whitespell.peak.logic.Safety.isNumeric(context_user_id) || payload.get(CATEGORY_ID_KEY) == null || !main.com.whitespell.peak.logic.Safety.isNumeric(payload.get(CATEGORY_ID_KEY).getAsString()) || payload.get(ACTION_KEY) == null) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }


        String category_id_string = payload.get(CATEGORY_ID_KEY).getAsString();

        //local variables
        final int user_id = Integer.parseInt(context_user_id);
        final int category_id = Integer.parseInt(category_id_string);
        final String action = payload.get(ACTION_KEY).getAsString();
        final Timestamp now = new Timestamp(new Date().getTime());

        /**
         * Check that the user id and following user id are valid (>= 0 && <= Integer.MAX_VALUE).
         */
        if (!main.com.whitespell.peak.logic.Safety.isValidUserId(user_id) || !main.com.whitespell.peak.logic.Safety.isValidUserId(category_id)) {
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
         * Create the {@link CategoryFollowAction.ActionResponse}.
         */
        final ActionResponse response = new ActionResponse();

        /**
         * Check to see if the user is already following the followed_user_id.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_FOLLOWING_CATEGORY_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, String.valueOf(user_id));
                    ps.setString(2, String.valueOf(category_id));

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
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ALREADY_FOLLOWING_CATEGORY);
                    return;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(INSERT_FOLLOW_CATEGORY_QUERY);
                    executor.execute(new ExecutionBlock() {
                        @Override
                        public void process(PreparedStatement ps) throws SQLException {
                            ps.setString(1, String.valueOf(user_id));
                            ps.setString(2, String.valueOf(category_id));
                            ps.setString(3, now.toString());

                            ps.executeUpdate();

                            response.setSuccess(true);
                            response.setActionTaken("followed");
                        }
                    });
                } catch (MySQLIntegrityConstraintViolationException e) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_SUCH_CATEGORY);
                } catch (SQLException e) {
                    if (e.getMessage().contains(""))
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
                    StatementExecutor executor = new StatementExecutor(DELETE_FOLLOW_CATEGORY_QUERY);
                    executor.execute(new ExecutionBlock() {
                        @Override
                        public void process(PreparedStatement ps) throws SQLException {
                            ps.setString(1, String.valueOf(user_id));
                            ps.setString(2, String.valueOf(category_id));

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
            FollowCategoryActionObject followObject = new FollowCategoryActionObject();
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

    public class FollowCategoryActionObject {

        String action_taken;

        public String getActionTaken() {
            return this.action_taken;
        }

        public void setActionTaken(String action_taken) {
            this.action_taken = action_taken;
        }

    }


}
