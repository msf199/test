package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte, Whitespell Inc.
 *         5/4/2015
 */
public class RequestContent extends EndpointHandler {

    private static final String CONTENT_SIZE_LIMIT = "limit";
    private static final String CONTENT_OFFSET = "offset";
    private static final String FOLLOWING_ID = "following_id";
    private static final String CONTENT_ID_KEY = "content_id";
    private static final String CONTENT_TYPE_ID = "content_type";
    private static final String CONTENT_TITLE = "content_title";
    private static final String CONTENT_URL = "content_url";
    private static final String CONTENT_DESCRIPTION = "content_description";
    private static final String CONTENT_THUMBNAIL = "thumbnail_url";

    private static final String QS_USER_ID = "userId";

    @Override
    protected void setUserInputs() {
        queryStringInput.put(CONTENT_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(CONTENT_OFFSET, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_USER_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
    }

    private static final String SELECT_CONTENT_BY_USER_ID = "SELECT * FROM `content` WHERE `content_id` > ? AND `user_id` = ? LIMIT ?";
    private static final String SELECT_CONTENT_FOR_ID_QUERY = "SELECT * FROM `content` WHERE `content_id` > ? LIMIT ?";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int userId = 0;
        boolean getContentByUserId = false;
        String REQUEST_CONTENT = SELECT_CONTENT_FOR_ID_QUERY;
        if(context.getQueryString().get(QS_USER_ID) != null){
            if(Safety.isInteger(context.getQueryString().get(QS_USER_ID)[0])){
                REQUEST_CONTENT = SELECT_CONTENT_BY_USER_ID;
                userId = Integer.parseInt(context.getQueryString().get(QS_USER_ID)[0]);
                getContentByUserId = true;
            }
        }

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Request the content of the followed ids.
         */

            try {
                StatementExecutor executor = new StatementExecutor(REQUEST_CONTENT);
                final int finalLimit = GenericAPIActions.getLimit(context.getQueryString());
                final int finalOffset = GenericAPIActions.getOffset(context.getQueryString());
                final int finalUserId = userId;
                final boolean finalContentUserId = getContentByUserId;
                executor.execute(ps -> {
                    ArrayList<ContentObject> contents = new ArrayList<>();
                    if(finalContentUserId){
                        ps.setInt(1, finalOffset);
                        ps.setInt(2, finalUserId);
                        ps.setInt(3, finalLimit);
                    }else{
                        ps.setInt(1, finalOffset);
                        ps.setInt(2, finalLimit);
                    }

                    ResultSet results = ps.executeQuery();

                    //display results
                    while (results.next()) {
                        if(finalUserId != 0){
                            ContentObject content = new ContentObject(finalUserId, results.getInt(CONTENT_ID_KEY),results.getInt(CONTENT_TYPE_ID), results.getString(CONTENT_TITLE),
                                    results.getString(CONTENT_URL), results.getString(CONTENT_DESCRIPTION), results.getString(CONTENT_THUMBNAIL));
                            contents.add(content);
                        }
                        else{
                            ContentObject content = new ContentObject(results.getInt(CONTENT_ID_KEY),results.getInt(CONTENT_TYPE_ID), results.getString(CONTENT_TITLE),
                                    results.getString(CONTENT_URL), results.getString(CONTENT_DESCRIPTION), results.getString(CONTENT_THUMBNAIL));
                            contents.add(content);
                        }
                    }

                    Gson g = new Gson();
                    String response = g.toJson(contents);
                    context.getResponse().setStatus(200);
                    try {
                        context.getResponse().getWriter().write(response);
                    } catch (Exception e) {
                        Logging.log("High", e);
                        return;
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                return;
            }
    }
}
