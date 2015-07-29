package main.com.whitespell.peak.logic.endpoints.newsfeed;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by cory on 15/07/15.
 */
public class GetEmptyNewsfeed extends EndpointHandler {

    private static final String USER_ID_KEY = "user_id";
    private static String EMPTY_NEWSFEED_QUERY = "SELECT `user_id` FROM `user` WHERE `user`.`user_id` NOT IN (SELECT `user_id` FROM (`newsfeed`)) AND `email` NOT LIKE '%temporary.email'";


    @Override
    public void setUserInputs() {
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        ArrayList<Integer> emptyNewsfeedUsers = new ArrayList<>();

        try {
            StatementExecutor executor = new StatementExecutor(EMPTY_NEWSFEED_QUERY);
            executor.execute(ps -> {
                    final ResultSet results = ps.executeQuery();

                    while (results.next()) {
                        emptyNewsfeedUsers.add(results.getInt(USER_ID_KEY));
                    }

                    context.getResponse().setStatus(200);
                    try {
                        context.getResponse().getWriter().write(emptyNewsfeedUsers.toString());
                    } catch (Exception e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                });
            }catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }
}


