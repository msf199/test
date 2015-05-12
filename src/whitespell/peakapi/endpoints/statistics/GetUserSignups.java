package whitespell.peakapi.endpoints.statistics;

import com.google.gson.Gson;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.logging.Logging;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.StatementExecutor;
import whitespell.model.DayResult;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetUserSignups implements ApiInterface {


    private static final String GET_SIGNUP_DATASET = "SELECT COUNT(1) as count, DATE(`registration_timestamp_utc`) as day FROM `users` GROUP BY DAY(`registration_timestamp_utc`)";

    @Override
    public void call(final RequestContext context) throws IOException {
        /**
        * Get the signups by day
        */
        try {
            StatementExecutor executor = new StatementExecutor(GET_SIGNUP_DATASET);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

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
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }

}
