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
public class PingInstance extends EndpointHandler {

    private static final String UPDATE_INSTANCE = "UPDATE `avcpvm_monitoring` SET `last_ping` = ?, `queue_size` = ? WHERE `instance_id` = ? LIMIT 1";

    private static final String PAYLOAD_INSTANCE_ID = "instanceId";
    private static final String PAYLOAD_QUEUE_SIZE = "queueSize";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_INSTANCE_ID, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_QUEUE_SIZE, StaticRules.InputTypes.REG_INT_OPTIONAL_ZERO);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        final String instance_id = payload.get(PAYLOAD_INSTANCE_ID).getAsString();

        int queue_size = 0;

        if(payload.get(PAYLOAD_QUEUE_SIZE) != null) {
            queue_size = payload.get(PAYLOAD_QUEUE_SIZE).getAsInt();
            System.out.println(queue_size);
        }

        final Timestamp now = new Timestamp(Server.getMilliTime()); // 15 mins max

        PingResponse cir = new PingResponse();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated() || a.getUserId() != 134) { // admin
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }
        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_INSTANCE);
            final int finalQueue_size = queue_size;
            executor.execute(ps -> {
                ps.setTimestamp(1, now);
                ps.setInt(2, finalQueue_size);
                ps.setString(3, instance_id);

                int rows = ps.executeUpdate();
                if(rows > 0) {
                    cir.setSuccess(true);
                } else {
                    cir.setSuccess(false);
                }
            });
        } catch (SQLException e) {
            if(e.getMessage().contains("Duplicate")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHORIZED);
                return;
            }
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }


        /**
         * If the action taken was successfully performed then write the response.
         */
        if (cir.isSuccess()) {

            context.getResponse().setStatus(HttpStatus.OK_200);
            Gson g = new Gson();
            String json = g.toJson(cir);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    public static class PingResponse {

        private boolean success;

        public PingResponse() {
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
