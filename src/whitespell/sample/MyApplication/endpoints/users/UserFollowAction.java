package whitespell.sample.MyApplication.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.Safety;
import whitespell.logic.sql.Pool;
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

    private static final String INSERT_FOLLOW_QUERY = "INSERT INTO `following`(`user_id`, `followed_id`, `timestamp`) " + "VALUES (?,?,?)";
    private static final String DELETE_FOLLOWED_QUERY = "DELETE FROM `following` WHERE `user_id` = ? AND `followed_id` = ?";


    public void call(RequestContext context) throws IOException {
        String context_user_id = context.getUrlVariables().get("user_id");

        JsonObject payload = context.getPayload().getAsJsonObject();
        String following_user_string = payload.get(FOLLOWING_USER_ID_KEY).getAsString();

        /**
         * Check that the user id, following id, and action are valid.
         */
        if (!Safety.isNumeric(context_user_id) || payload.get(FOLLOWING_USER_ID_KEY) == null || !Safety.isNumeric(following_user_string) || payload.get(ACTION_KEY) == null) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        //local variables
        final int user_id = Integer.parseInt(context_user_id);
        final int following_user_id = Integer.parseInt(following_user_string);
        final String action = payload.get(ACTION_KEY).getAsString();
        Timestamp now = new Timestamp(new Date().getTime());

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

        boolean success = false;
        String action_taken = null;

        Connection con = null;
        PreparedStatement p = null;

        boolean alreadyFollowing = false;
        try {
            con = Pool.getConnection();
            try {
                p = con.prepareStatement(CHECK_FOLLOWING_QUERY);
                p.setString(1, String.valueOf(user_id));
                p.setString(2, String.valueOf(following_user_id));

                ResultSet results = p.executeQuery();
                if (results.next()) {
                    alreadyFollowing = true;
                }
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        switch (action) {
            case "follow":
                if (alreadyFollowing) {
                    context.throwHttpError(StaticRules.ErrorCodes.ALREADY_FOLLOWING_USER);
                    return;
                }
                try {
                    con = Pool.getConnection();
                    try {
                        p = con.prepareStatement(INSERT_FOLLOW_QUERY);
                        p.setString(1, String.valueOf(user_id));
                        p.setString(2, String.valueOf(following_user_id));
                        p.setString(3, now.toString());

                        p.executeUpdate();

                        success = true;
                        action_taken = "followed";
                    } finally {
                        if (con != null) {
                            con.close();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "unfollow":
                if (!alreadyFollowing) {
                    context.throwHttpError(StaticRules.ErrorCodes.NOT_FOLLOWING_USER);
                    return;
                }
                try {
                    con = Pool.getConnection();
                    try {
                        p = con.prepareStatement(DELETE_FOLLOWED_QUERY);
                        p.setString(1, String.valueOf(user_id));
                        p.setString(2, String.valueOf(following_user_id));

                        p.executeUpdate();

                        success = true;
                        action_taken = "unfollowed";
                    } finally {
                        if (con != null) {
                            con.close();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }


        if (success) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            FollowActionObject followObject = new FollowActionObject();
            followObject.setActionTaken(action_taken);
            Gson g = new Gson();
            String json = g.toJson(followObject);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

}
