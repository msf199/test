package main.com.whitespell.peak.logic.notifications;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.GenericAPIActions;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         9/23/2015
 */
public class GetUserNotifications extends EndpointHandler {

    private static final String GET_USER_NOTIFICATIONS_QUERY = "SELECT * FROM `notification` WHERE `user_id` = ? AND `notification_id` > ? LIMIT ?";

    private static final String URL_USER_ID = "userId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (user_id == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        final getUserNotificationsResponse userNotifications = new getUserNotificationsResponse();
        try {
            StatementExecutor executor = new StatementExecutor(GET_USER_NOTIFICATIONS_QUERY);
            final int finalUser_id = user_id;

            executor.execute(ps -> {
                ps.setInt(1, finalUser_id);
                ps.setInt(2, GenericAPIActions.getOffset(context.getQueryString()));
                ps.setInt(3, GenericAPIActions.getLimit(context.getQueryString()));

                ResultSet results = ps.executeQuery();

                while (results.next()) {
                    UserNotification n = new UserNotification(finalUser_id, results.getString("notification_text"), results.getString("notification_action"));
                    n.setNotificationBadge(results.getInt("notification_badge"));
                    n.setNotificationSound(results.getInt("notification_sound"));
                    n.setNotificationStatus(results.getInt("notification_status"));

                    userNotifications.addToUserNotifications(n);
                }
                Gson g = new Gson();
                String response = g.toJson(userNotifications);
                context.getResponse().setStatus(200);
                try {
                    context.getResponse().getWriter().write(response);
                } catch (Exception e) {
                    Logging.log("High", e);
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

    public class getUserNotificationsResponse {

        public getUserNotificationsResponse() {
            this.userNotifications = new ArrayList<>();
        }

        public ArrayList<UserNotification> getUserNotifications() {
            return userNotifications;
        }

        public void addToUserNotifications(UserNotification notification) {
            userNotifications.add(notification);
        }

        public ArrayList<UserNotification> userNotifications;
    }
}
