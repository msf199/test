package main.com.whitespell.peak.logic.endpoints.authentication;

import com.google.gson.Gson;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.Timestamp;
import java.sql.SQLException;

/**
 * Created by cory on 15/07/15.
 */
public class ExpireAuthentication extends EndpointHandler {

    private static final String USER_ID = "userId";

    public static final String LOGOUT_QUERY = "UPDATE `authentication` SET `expires` = ? WHERE `key` = ? AND `user_id` = ? LIMIT 1";

    @Override
    protected void setUserInputs() {
        urlInput.put(USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        final boolean[] success = {false};

        try {
            StatementExecutor executor = new StatementExecutor(LOGOUT_QUERY);
            executor.execute(ps -> {
                ps.setTimestamp(1, new Timestamp(Server.getCalendar().getTimeInMillis()));
                ps.setString(2, a.getKey());
                ps.setInt(3, a.getUserId());


               int rows =  ps.executeUpdate();
                if(rows > 0) {
                    success[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }



        if (success[0]) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            LogoutObject object = new LogoutObject();
            object.setLoggedOut(true);
            Gson g = new Gson();
            String json = g.toJson(object);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    public class LogoutObject {

        private boolean loggedOut;

        public boolean isLoggedOut() {
            return this.loggedOut;
        }

        public void setLoggedOut(boolean loggedOut) {
            this.loggedOut = loggedOut;
        }

    }
}

