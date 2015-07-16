package main.com.whitespell.peak.logic.endpoints.newsfeed;


import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.NewsfeedObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by cory on 15/07/15.
 */
public class GetNewsfeed extends EndpointHandler {

        private static final String GET_NEWSFEED_QUERY = "SELECT `newsfeed_object` FROM `newsfeed` WHERE `user_id` = ?";
        private static final String URL_USER_ID = "userId";

        @Override
        protected void setUserInputs() {
            urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        }

        @Override
        public void safeCall(final RequestObject context) throws IOException {
            ArrayList<String> output = new ArrayList<>();
            int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));

            try {
                StatementExecutor executor = new StatementExecutor(GET_NEWSFEED_QUERY);
                final int finalUser_id = user_id;
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {

                        ps.setInt(1, finalUser_id);

                        NewsfeedObject newsfeed = null;

                        final ResultSet results = ps.executeQuery();

                        if (results.next()) {
                            newsfeed = new NewsfeedObject(results.getString("newsfeed_object"));
                        }

                        Gson g = new Gson();
                        String response = g.toJson(newsfeed);
                        context.getResponse().setStatus(200);
                        try {
                            context.getResponse().getWriter().write(response);
                        } catch (Exception e) {
                            Logging.log("High", e);
                            return;
                        }
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                return;
            }

        }
    }
