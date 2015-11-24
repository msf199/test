package main.com.whitespell.peak.logic.endpoints.videoprocessing;

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
public class RemoveInstanceLock extends EndpointHandler {

    private static final String INSERT_LOCK = "DELETE FROM `processing_lock` WHERE `content_id` = ? AND `instance_id` = ?";

    private static final String URL_CONTENT_ID = "contentId";

    private static final String PAYLOAD_INSTANCE_ID = "instanceId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_INSTANCE_ID, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        int content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));
        final String instance_id = payload.get(PAYLOAD_INSTANCE_ID).getAsString();


        LockResponse lr = new LockResponse();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated() || a.getUserId() != 134) { // admin
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_LOCK);
            executor.execute(ps -> {
                ps.setInt(1, content_id);
                ps.setString(2, instance_id);


                int rows = ps.executeUpdate();
                if(rows > 0) {
                    lr.setSuccess(true);
                } else {
                    lr.setSuccess(false);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }


        /**
         * If the action taken was successfully performed then write the response.
         */
        if (lr.isSuccess()) {

            context.getResponse().setStatus(HttpStatus.OK_200);
            Gson g = new Gson();
            String json = g.toJson(lr);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    public static class LockResponse {

        private boolean success;

        public LockResponse() {
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
