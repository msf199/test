package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.ContentTypeObject;
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
    private static final String FOLLOWING_ID = "following_id";
    private static final String CONTENT_TYPE_ID = "content_type";
    private static final String CONTENT_TITLE = "content_title";
    private static final String CONTENT_URL = "content_url";
    private static final String CONTENT_DESCRIPTION = "content_description";


    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }


    private static final String SELECT_FOLLOWING_IDS_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";
    private static final String SELECT_CONTENT_FOR_ID_QUERY = "SELECT * FROM `content` WHERE `user_id` = ?";

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        String context_user_id = context.getUrlVariables().get(URL_USER_ID_KEY);

        final int user_id = Integer.parseInt(context_user_id);
        final List<Integer> followedIds = new ArrayList<>();

        System.out.println("print before list IDs");
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
                        followedIds.add(results.getInt(FOLLOWING_ID));
                    }
                    System.out.println("inside list ids");

                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }

        /**
         * Request the content of the followed ids.
         */

        System.out.println("Print before followedIDs");
        for (final int followedId : followedIds) {
            try {
                StatementExecutor executor = new StatementExecutor(SELECT_CONTENT_FOR_ID_QUERY);
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {
                        ps.setString(1, String.valueOf(followedId));
                        ArrayList<ContentObject> contents = new ArrayList<>();
                        ResultSet results = ps.executeQuery();

                        //display results
                        while (results.next()) {
                            ContentObject content = new ContentObject(results.getInt(CONTENT_TYPE_ID), results.getString(CONTENT_TITLE),
                                    results.getString(CONTENT_URL), results.getString(CONTENT_DESCRIPTION));
                            contents.add(content);
                        }

                        Gson g = new Gson();
                        String response = g.toJson(contents);
                        context.getResponse().setStatus(200);
                        try {
                            context.getResponse().getWriter().write(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (SQLException e) {
                System.err.println("FOLLOWED ID: " + followedId);
                Logging.log("High", e);
            }
        }
    }

}
