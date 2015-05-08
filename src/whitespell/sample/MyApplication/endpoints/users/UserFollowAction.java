package whitespell.sample.MyApplication.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.Safety;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.StatementExecutor;
import whitespell.model.FollowActionObject;

import java.io.IOException;
import java.sql.*;
import java.util.Date;

/**
 * @author Pim de Witte(wwadewitte), Josh Lipson(mrgalkon), Whitespell LLC
 *         5/4/2015
 *         whitespell.model
 */
public class UserFollowAction implements ApiInterface {

    private static final String FOLLOWING_USER_ID_KEY = "following_id";
    private static final String ACTION_KEY = "action";

    private static final String CHECK_FOLLOWING_QUERY = "SELECT * FROM `following` WHERE `user_id` = ? AND `followed_id` = ? LIMIT 1";

    private static final String INSERT_FOLLOW_QUERY = "INSERT INTO `following`(`user_id`, `followed_id`, `timestamp`) VALUES (?,?,?)";
    private static final String DELETE_FOLLOWED_QUERY = "DELETE FROM `following` WHERE `user_id` = ? AND `followed_id` = ?";

    @Override
    public void call(RequestContext context) throws IOException {
        String context_user_id = context.getUrlVariables().get("user_id");

        JsonObject payload = context.getPayload().getAsJsonObject();

        /**
         * Check that the user id, following id, and action are valid.
         */
        if (!Safety.isNumeric(context_user_id) || payload.get(FOLLOWING_USER_ID_KEY) == null || !Safety.isNumeric(payload.get(FOLLOWING_USER_ID_KEY).getAsString()) || payload.get(ACTION_KEY) == null) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
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
        if (!Safety.isValidUserId(user_id) || !Safety.isValidUserId(following_user_id)) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
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
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        /**
         * Create the {@link whitespell.sample.MyApplication.endpoints.users.UserFollowAction.ActionResponse}.
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
            e.printStackTrace();
        }

        switch (action) {

            case "follow":
                /**
                 * If already following, throw error.
                 */
                if (response.isCurrentlyFollowing()) {
                    context.throwHttpError(StaticRules.ErrorCodes.ALREADY_FOLLOWING_USER);
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
                    e.printStackTrace();
                }
                break;

            case "unfollow":
                /**
                 * If not currently following, throw error.
                 */
                if (!response.isCurrentlyFollowing()) {
                    context.throwHttpError(StaticRules.ErrorCodes.NOT_FOLLOWING_USER);
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
                    e.printStackTrace();
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
            context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    private static class ActionResponse {

        private boolean success;
        private String actionTaken;

        public boolean isCurrentlyFollowing() {
            return currentlyFollowing;
        }

        public void setCurrentlyFollowing(boolean currentlyFollowing) {
            this.currentlyFollowing = currentlyFollowing;
        }

        private boolean currentlyFollowing;

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

        public ActionResponse() {
            this.success = false;
            this.currentlyFollowing = false;
            this.actionTaken = null;
        }

    }


}
