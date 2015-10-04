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
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/3/15
 */
public class ContentViewAction extends EndpointHandler {

    private static final String INSERT_CONTENT_VIEW_QUERY = "INSERT INTO `content_views`(`user_id`, `content_id`, `view_datetime`) VALUES (?,?,?)";

    private static final String PLUS_VIEW_QUERY = "UPDATE `content` SET `content_views` = `content_views` + 1 WHERE `content_id` = ?";

    private static final String URL_CONTENT_LIKE_ID = "contentId";

    private static final String PAYLOAD_USER_ID_KEY = "userId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_LIKE_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        int content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_LIKE_ID));
        final int user_id = payload.get(PAYLOAD_USER_ID_KEY).getAsInt();
        final Timestamp now = new Timestamp(Server.getCalendar().getTimeInMillis());

        ViewResponse vr = new ViewResponse();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = a.getUserId() == user_id;

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CONTENT_VIEW_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, user_id);
                ps.setInt(2, content_id);
                ps.setString(3, now.toString());

                ps.executeUpdate();
                vr.setSuccess(true);
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(PLUS_VIEW_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, content_id);

                ps.executeUpdate();
            });
        } catch (SQLException e) {
            Logging.log("High", e); // crash doesn't matter but we need to log it
        }

        /**
         * If the action taken was successfully performed then write the response.
         */
        if (vr.isSuccess()) {

            context.getResponse().setStatus(HttpStatus.OK_200);
            Gson g = new Gson();
            String json = g.toJson(vr);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    public static class ViewResponse {

        private boolean success;

        public ViewResponse() {
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
