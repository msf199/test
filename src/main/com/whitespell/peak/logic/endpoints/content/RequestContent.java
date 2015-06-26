package main.com.whitespell.peak.logic.endpoints.content;

import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pim de Witte, Whitespell Inc.
 *         5/4/2015
 */
public class RequestContent extends EndpointHandler {

    private static final String URL_USER_ID_KEY = "user_id";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }


    private static final String SELECT_FOLLOWING_IDS_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";
    private static final String SELECT_CONTENT_FOR_ID_QUERY = "SELECT * FROM `content` WHERE `user_id` = ?";

    @Override
    public void safeCall(RequestObject context) throws IOException {
        String context_user_id = context.getUrlVariables().get("user_id");


        final int user_id = Integer.parseInt(context_user_id);
        final List<Integer> followedIds = new ArrayList<>();

        /**
         * Request the list of followed ids.
         */
        try {
            StatementExecutor executor = new StatementExecutor(SELECT_FOLLOWING_IDS_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, String.valueOf(user_id));

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        followedIds.add(results.getInt("following_id"));
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }

        /**
         * Request the content of the followed ids.
         */
        for (final int followedId : followedIds) {
            try {
                StatementExecutor executor = new StatementExecutor(SELECT_CONTENT_FOR_ID_QUERY);
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {
                        ps.setString(1, String.valueOf(followedId));

                        ResultSet results = ps.executeQuery();
                        while (results.next()) {
                            //TODO display results
                        }
                    }
                });
            } catch (SQLException e) {
                System.err.println("FOLLOWED ID: " + followedId);
                Logging.log("High", e);
            }
        }

        context.getResponse().setStatus(HttpStatus.OK_200);
        context.getResponse().getWriter().write("{}");
    }

}
