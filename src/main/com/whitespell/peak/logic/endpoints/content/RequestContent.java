package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Pim de Witte & Cory McAn(cmcan), Whitespell Inc.
 *         5/4/2015
 */
public class RequestContent extends EndpointHandler {

    private static final String CONTENT_CATEGORY_ID = "category_id";
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
    private static final String QS_CATEGORY_ID = "categoryId";

    @Override
    protected void setUserInputs() {
        queryStringInput.put(CONTENT_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(CONTENT_OFFSET, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_USER_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int temp = 0, temp2 = 0;
        ArrayList<String> queryKeys = new ArrayList<>();
        ArrayList<String> queryKeys2 = new ArrayList<>();
        ArrayList<Integer> queryValues = new ArrayList<>();
        ArrayList<Integer> queryValues2 = new ArrayList<>();
        Map<String, String[]> urlQueryString = context.getQueryString();

        if (urlQueryString.get(QS_USER_ID) != null) {
            if (Safety.isInteger(urlQueryString.get(QS_USER_ID)[0])) {
                temp = Integer.parseInt(urlQueryString.get(QS_USER_ID)[0]);
                queryKeys.add("user_id");
                queryValues.add(temp);
            }
        }
        if (urlQueryString.get(QS_CATEGORY_ID) != null) {
            if (Safety.isInteger(urlQueryString.get(QS_CATEGORY_ID)[0])) {
                temp2 = Integer.parseInt(urlQueryString.get(QS_CATEGORY_ID)[0]);
                queryKeys2.add("category_id");
                queryValues2.add(temp2);
            }
        }
        int userId = temp;
        int categoryId = temp2;

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Construct the SELECT FROM CONTENT query based on the the desired query output.
         */
        StringBuilder selectString = new StringBuilder();
        selectString.append("SELECT * FROM `content` WHERE `content_id` > ? ");
        for (String s : queryKeys) {
            selectString.append("AND `" + s + "` = ? ");
        }
        selectString.append("LIMIT ?");
        final String REQUEST_CONTENT = selectString.toString();
        System.out.println(REQUEST_CONTENT);

        /**
         * Construct the SELECT FROM CONTENT_CATEGORY query based on categoryId
         */
        selectString = new StringBuilder();
        selectString.append("SELECT * FROM `content_category` WHERE `content_id` > ? ");
        for (String s : queryKeys2) {
            selectString.append("AND `" + s + "` = ? ");
        }
        selectString.append("LIMIT ?");
        final String SELECT_CONTENT_CATEGORY = selectString.toString();
        System.out.println(SELECT_CONTENT_CATEGORY);

        /**
         * Request the content based on query string
         */
        try {
            ArrayList<ContentObject> contents = new ArrayList<>();
            final int finalLimit = GenericAPIActions.getLimit(context.getQueryString());
            final int finalOffset = GenericAPIActions.getOffset(context.getQueryString());
            final int finalCategoryId = categoryId;
            StatementExecutor executor2 = new StatementExecutor(REQUEST_CONTENT);
            final int finalUserId = userId;
            System.out.println("finalCategoryId: " + categoryId);
            System.out.println("finalUserId: "+userId);
            executor2.execute(ps -> {
                ps.setInt(1, finalOffset);
                int count = 2;

                if (queryValues.contains(finalUserId)) {
                    ps.setInt(count, finalUserId);
                    count++;
                }

                ps.setInt(count, finalLimit);

                ResultSet results = ps.executeQuery();

                //display results
                while (results.next()) {
                    StatementExecutor executor = new StatementExecutor(SELECT_CONTENT_CATEGORY);
                    executor.execute(ps2 -> {
                        ps2.setInt(1, finalOffset);
                        int count2 = 2;
                        if (queryValues2.contains(finalCategoryId)) {
                            ps2.setInt(count2, finalCategoryId);
                            count2++;
                        }
                        ps2.setInt(count2, finalLimit);

                        ResultSet results2 = ps2.executeQuery();

                        //display results
                        while (results2.next()) {
                            if (results.getInt(CONTENT_ID_KEY) == results2.getInt(CONTENT_ID_KEY)) {
                                ContentObject content = new ContentObject(results2.getInt(CONTENT_CATEGORY_ID), results.getInt("user_id"), results.getInt(CONTENT_ID_KEY), results.getInt(CONTENT_TYPE_ID), results.getString(CONTENT_TITLE),
                                        results.getString(CONTENT_URL), results.getString(CONTENT_DESCRIPTION), results.getString(CONTENT_THUMBNAIL));
                                contents.add(content);
                            }
                        }
                    });
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
