package main.com.whitespell.peak.logic.endpoints.statistics;

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
public class GetUserSignups extends EndpointHandler {


    private static final String GET_SIGNUP_DATASET = "SELECT COUNT(1) as count, DATE(`registration_timestamp_utc`) as day FROM `user` GROUP BY DAY(`registration_timestamp_utc`)";

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        /**
         * Get the signups by day
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_SIGNUP_DATASET);
            executor.execute(ps -> {

                final ResultSet results = ps.executeQuery();
                ArrayList<DayResult> dayResults = new ArrayList<>();
                while (results.next()) {

                    DayResult d = new DayResult(results.getString("day"), results.getInt("count"));

                    dayResults.add(d);
                }

                // put the array list into a JSON array and write it as a response

                Gson g = new Gson();
                String response = g.toJson(dayResults);
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
