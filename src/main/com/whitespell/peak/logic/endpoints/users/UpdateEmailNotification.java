package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         9/28/2015
 */
public class UpdateEmailNotification extends EndpointHandler {

    private static final String UPDATE_EMAIL_NOTIFICATION = "UPDATE `user` SET `email_notifications` = ? WHERE `user_id` = ?";

    private static final String URL_USER_ID_KEY = "userId";

    private static final String PAYLOAD_EMAIL_NOTIFICATION_KEY = "emailNotifications";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_EMAIL_NOTIFICATION_KEY, StaticRules.InputTypes.REG_INT_REQUIRED_BOOL);
    }

    private static final String ENSURE_NOTIFICATION_QUERY = "SELECT `username`, `email_notifications` FROM `user` WHERE `user_id` = ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        int userId = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID_KEY));
        int emailNotification = payload.get(PAYLOAD_EMAIL_NOTIFICATION_KEY).getAsInt();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated() || userId != a.getUserId()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Update the user's email notification status in the database.
         */

        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_EMAIL_NOTIFICATION);
            final int finalUserId = userId;
            final int finalEmailNotification = emailNotification;
            executor.execute(ps -> {
                ps.setInt(1, finalEmailNotification);
                ps.setInt(2, finalUserId);

                ps.executeUpdate();
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Ensure user has verified their email and return the response.
         */

        try {
            StatementExecutor executor = new StatementExecutor(ENSURE_NOTIFICATION_QUERY);
            final int finalUserId = userId;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setInt(1, finalUserId);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        if(emailNotification != s.getInt("email_notifications")){
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOTIFICATION_UPDATE_FAILED);
                            return;
                        }

                        context.getResponse().setStatus(HttpStatus.OK_200);
                        UserObject uo = new UserObject();
                        uo.setUserName(s.getString("username"));
                        uo.setEmailNotifications(s.getInt("email_notifications"));
                        uo.setEmailVerified(-1);
                        uo.setUserId(finalUserId);
                        uo.setPublisher(-1);

                        Gson g = new Gson();
                        String json = g.toJson(uo);
                        try {
                            context.getResponse().getWriter().write(json);
                        } catch (IOException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                            return;
                        }
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }
}
