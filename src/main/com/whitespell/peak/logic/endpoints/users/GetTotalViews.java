package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/3/15
 */
public class GetTotalViews extends EndpointHandler {

    private static final String GET_TOTAL_VIEWS_QUERY = "SELECT `content_views` FROM `content` WHERE `user_id` = ?";

    private static final String URL_USER_ID = "userId";

    private static final String CONTENT_VIEWS_KEY = "content_views";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        int[] totalViews = {0};

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (user_id == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Get the total views for this user's uploaded content
         */
        final TotalViewsResponse totalViewsResponse = new TotalViewsResponse();
        try {
            StatementExecutor executor = new StatementExecutor(GET_TOTAL_VIEWS_QUERY);
            final int finalUser_id = user_id;

            executor.execute(ps -> {
                ps.setInt(1, finalUser_id);

                ResultSet results = ps.executeQuery();

                while (results.next()) {
                    totalViews[0] += results.getInt(CONTENT_VIEWS_KEY);
                }

                Gson g = new Gson();
                totalViewsResponse.setTotalViews(totalViews[0]);
                String response = g.toJson(totalViewsResponse);
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

    public class TotalViewsResponse {

        public TotalViewsResponse() {
            this.totalViews = 0;
        }

        public int getTotalViews() {
            return totalViews;
        }

        public void setTotalViews(int totalViews) {
            this.totalViews = totalViews;
        }

        public int totalViews;
    }
}
