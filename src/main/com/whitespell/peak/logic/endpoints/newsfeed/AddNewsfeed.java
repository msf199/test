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
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by cory on 15/07/15.
 */
public class AddNewsfeed extends EndpointHandler {

    private static final String POST_NEWSFEED = "newsfeed";
    private static final String USER_ID = "userId";

    public static final String DELETE_NEWSFEED_QUERY = "DELETE FROM `newsfeed` WHERE `user_id` = ? LIMIT 1";
    public static final String ADD_NEWSFEED_QUERY = "INSERT INTO `newsfeed`(`user_id`,`newsfeed_object`) VALUES(?,?)";

    @Override
    protected void setUserInputs() {
        payloadInput.put(POST_NEWSFEED, StaticRules.InputTypes.LONG_STRING_REQUIRED);
        urlInput.put(USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        final boolean[] success = {false};

        try {
            StatementExecutor executor = new StatementExecutor(DELETE_NEWSFEED_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, Integer.parseInt(context.getUrlVariables().get(USER_ID)));
                ps.executeUpdate();
                success[0] = true;

            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }


        try {
            StatementExecutor executor = new StatementExecutor(ADD_NEWSFEED_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, Integer.parseInt(context.getUrlVariables().get(USER_ID)));
                ps.setString(2, context.getPayload().getAsJsonObject().get(POST_NEWSFEED).getAsString());
                ps.executeUpdate();
                success[0] = true;

            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }

        if (success[0]) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            AddNewsfeedObject object = new AddNewsfeedObject();
            object.setNewsfeedAdded(true);
            Gson g = new Gson();
            String json = g.toJson(object);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        //insert the newsfeed

    }


    public class AddNewsfeedObject {

        private boolean newsfeed_added;

        public boolean isNewsfeedAdded() {
            return this.newsfeed_added;
        }

        public void setNewsfeedAdded(boolean newsfeed_added) {
            this.newsfeed_added = newsfeed_added;
        }

    }
}

