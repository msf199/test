package main.com.whitespell.peak.logic.endpoints.export;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class ExportEmails extends EndpointHandler {


        private static final String GET_EMAILS = "SELECT `email`, `email_verified`, `subscriber` from user";

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        /**
         * Get the signups by day
         */
        try {
            StringBuilder output = new StringBuilder();
            StatementExecutor executor = new StatementExecutor(GET_EMAILS);
            executor.execute(ps -> {

                final ResultSet results = ps.executeQuery();
                while (results.next()) {
                output.append(results.getString("email"));
                output.append(",");
                    output.append(results.getString("email_verified"));
                    output.append(",");
                    output.append(results.getString("subscriber"));
                    output.append("\n");
                }

                // put the array list into a JSON array and write it as a response

                Gson g = new Gson();
                String response = output.toString();
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

    @Override
    protected void setUserInputs() {

    }

    public class DayResult {
        String day = null;
        int data = -1;

        public DayResult(String day, int data) {
            this.day = day;
            this.data = data;
        }


    }

}
