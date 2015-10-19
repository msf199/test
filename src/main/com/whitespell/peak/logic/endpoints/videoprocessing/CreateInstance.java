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
public class CreateInstance extends EndpointHandler {

    private static final String INSERT_INSTANCE = "INSERT INTO `avcpvm_monitoring`(`instance_id`, `ipv4_address`, `creation_time`) VALUES (?,?,?)";

    private static final String PAYLOAD_INSTANCE_ID = "instanceId";
    private static final String PAYLOAD_INSTANCE_IPV4 = "ipv4Address";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_INSTANCE_ID, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_INSTANCE_IPV4, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        final String instance_id = payload.get(PAYLOAD_INSTANCE_ID).getAsString();
        final String ipv4_address = payload.get(PAYLOAD_INSTANCE_IPV4).getAsString();

        final Timestamp now = new Timestamp(Server.getCalendar().getTimeInMillis()); // 15 mins max

        CreateInstanceResponse cir = new CreateInstanceResponse();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated() || a.getUserId() != -1) { // admin
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_INSTANCE);
            executor.execute(ps -> {
                ps.setString(1, instance_id);
                ps.setString(2, ipv4_address);
                ps.setTimestamp(3, now);

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


    public static class CreateInstanceResponse {

        private boolean success;

        public CreateInstanceResponse() {
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
