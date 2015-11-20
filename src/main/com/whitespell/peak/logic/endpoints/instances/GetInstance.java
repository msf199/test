package main.com.whitespell.peak.logic.endpoints.instances;

import com.google.gson.Gson;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         10/17/15
 *         main.com.whitespell.peak.logic.endpoints.videoprocessing
 */
public class GetInstance extends EndpointHandler {


    private static final String GET_PROCESSING_LOCKS = "SELECT `instance_id` FROM `avcpvm_processing` WHERE `ipv4_address` = ? AND `expires` > ? ";
    private final static String URL_CONTENT_ID = "contentId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        final int content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));

        try {
            StatementExecutor executor = new StatementExecutor(GET_PROCESSING_LOCKS);
            executor.execute(ps -> {
                ps.setInt(1, content_id);
                ps.setTimestamp(2, new java.sql.Timestamp(Server.getMilliTime()));
                final ResultSet results = ps.executeQuery();
                ProcessingLockedResponse p = new ProcessingLockedResponse();


                if (results.next()) {
                    p.setLocked(true);
                }

                Gson g = new Gson();
                String response = g.toJson(p);
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

    public class ProcessingLockedResponse {

        private boolean isLocked;

        public boolean isLocked() {
            return this.isLocked;
        }

        public void setLocked(boolean locked) {
            this.isLocked = locked;
        }
    }
}


