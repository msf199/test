package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.ContentWrapper;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetContent extends EndpointHandler {


    private static final String GET_CONTENT = "SELECT * FROM `content` as ct INNER JOIN `user` as ut ON ct.`user_id` = ut.`user_id` WHERE `content_id` = ? ";

    private final static String URL_CONTENT_ID = "contentId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        final int content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));
        int currentUser = a.getUserId();

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        ContentWrapper contentWrapper = new ContentWrapper(context, currentUser);

        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT);

            executor.execute(ps -> {
                ps.setInt(1, content_id);

                final ResultSet results = ps.executeQuery();
                ContentObject content = null;
                while (results.next()) {
                    content = contentWrapper.wrapContent(results);
                }

                // put the array list into a JSON array and write it as a response

                Gson g = new Gson();
                String response = g.toJson(content);
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




}
