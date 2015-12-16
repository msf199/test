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
import java.util.Map;

/**
 * @author Pim de Witte & Cory McAn(cmcan), Whitespell Inc.
 *         5/4/2015
 */
public class RequestContent extends EndpointHandler {

    private static final String CONTENT_SIZE_LIMIT = "limit";
    private static final String CONTENT_OFFSET = "offset";

    private static final String QS_USER_ID = "userId";
    private static final String QS_CONTENT_ID = "contentId";
    private static final String QS_CONTENT_TYPE_ID = "contentType";
    private static final String QS_CATEGORY_ID = "categoryId";
    private static final String QS_PROCESSED = "processed";
    private static final String QS_PARENT = "parent";

    @Override
    protected void setUserInputs() {
        queryStringInput.put(CONTENT_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(CONTENT_OFFSET, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_USER_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_CONTENT_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_CONTENT_TYPE_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_CATEGORY_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_PROCESSED, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_PARENT, StaticRules.InputTypes.REG_INT_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int temp_user_id = -1,
                temp_content_id = -1,
                temp_content_type_id = -1,
                temp_category_id = -1,
                temp_processed=1,
                temp_parent=-1;
        final boolean[] getUserVideos = {false};
        final boolean[] getCategoryVideos = {false};

        ArrayList<String> queryKeys = new ArrayList<>();
        Map<String, String[]> urlQueryString = context.getQueryString();


        if (urlQueryString.get(QS_USER_ID) != null) {
            getUserVideos[0] = true;
            if (Safety.isInteger(urlQueryString.get(QS_USER_ID)[0])
                    && Integer.parseInt(urlQueryString.get(QS_USER_ID)[0]) > 0) {
                temp_user_id = Integer.parseInt(urlQueryString.get(QS_USER_ID)[0]);
                queryKeys.add("user_id");
            }
        }
        if (urlQueryString.get(QS_CONTENT_ID) != null) {
            if (Safety.isInteger(urlQueryString.get(QS_CONTENT_ID)[0])
                    && Integer.parseInt(urlQueryString.get(QS_CONTENT_ID)[0]) > 0) {
                temp_content_id = Integer.parseInt(urlQueryString.get(QS_CONTENT_ID)[0]);
                /**
                 * ContentId 0 doesn't exist and would cause unusual behavior.
                 */
                if(temp_content_id == 0){
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_ID_0_DOESNT_EXIST);
                    return;
                }
                queryKeys.add("content_id");
            }
        }
        if (urlQueryString.get(QS_CONTENT_TYPE_ID) != null) {
            if (Safety.isInteger(urlQueryString.get(QS_CONTENT_TYPE_ID)[0])
                    && Integer.parseInt(urlQueryString.get(QS_CONTENT_TYPE_ID)[0]) > 0) {
                temp_content_type_id = Integer.parseInt(urlQueryString.get(QS_CONTENT_TYPE_ID)[0]);
                queryKeys.add("content_type");
            }
        }
        if (urlQueryString.get(QS_CATEGORY_ID) != null) {
            getCategoryVideos[0] = true;
            if (Safety.isInteger(urlQueryString.get(QS_CATEGORY_ID)[0])
                    && Integer.parseInt(urlQueryString.get(QS_CATEGORY_ID)[0]) > 0) {
                temp_category_id = Integer.parseInt(urlQueryString.get(QS_CATEGORY_ID)[0]);
                queryKeys.add("category_id");
            }
        }

        /** We always only want to return processed videos unless specified otherwise **/



        if (urlQueryString.get(QS_PROCESSED) != null) {
            if (Safety.isInteger(urlQueryString.get(QS_PROCESSED)[0])) {
                temp_processed = Integer.parseInt(urlQueryString.get(QS_PROCESSED)[0]);
                queryKeys.add("processed");
            }
        }
        /** We always only want to return processed videos unless specified otherwise **/



        if (urlQueryString.get(QS_PARENT) != null) {
            if (Safety.isInteger(urlQueryString.get(QS_PARENT)[0])
                    && Integer.parseInt(urlQueryString.get(QS_PARENT)[0]) > 0) {
                temp_parent = Integer.parseInt(urlQueryString.get(QS_PARENT)[0]);
                queryKeys.add("parent");
            }
        }

        int userId = temp_user_id;
        int contentId = temp_content_id;
        int contentType = temp_content_type_id;
        int categoryId = temp_category_id;
        int processed = temp_processed;
        int parent = temp_parent;

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

        /**
         * Construct the SELECT FROM CONTENT query based on the the desired query output.
         */
        StringBuilder selectString = new StringBuilder();
        selectString.append("SELECT * FROM `content` as ct INNER JOIN `user` as ut ON ct.`user_id` = ut.`user_id` WHERE `content_id` > ? ");

        for (String s : queryKeys) {
            selectString.append("AND `ct`.`" + s + "` = ? ");
        }

        selectString.append("LIMIT ?");
        final String REQUEST_CONTENT = selectString.toString();

        /**
         * Request the content based on query string
         */
        try {
            ArrayList<ContentObject> contents = new ArrayList<>();
            final int finalLimit = GenericAPIActions.getLimit(context.getQueryString());
            final int finalOffset = GenericAPIActions.getOffset(context.getQueryString());
            final int finalCategoryId = categoryId;
            final int finalUserId = userId;
            final int finalContentId = contentId;
            final int finalContentType = contentType;
            final int finalProcessed = processed;
            final int finalParent = parent;

            StatementExecutor executor = new StatementExecutor(REQUEST_CONTENT);

            executor.execute(ps -> {
                
                ps.setInt(1, finalOffset);
                
                int count = 2;

                //todo(cmcan) turn this into a loop, this is bad code

                if (queryKeys.contains("user_id")) {
                    ps.setInt(count, finalUserId);
                    count++;
                }

                if (queryKeys.contains("content_id")) {
                    ps.setInt(count, finalContentId);
                    count++;
                }

                if (queryKeys.contains("content_type")) {
                    ps.setInt(count, finalContentType);
                    count++;
                }

                if (queryKeys.contains("category_id")) {
                    ps.setInt(count, finalCategoryId);
                    count++;
                }

                if (queryKeys.contains("processed")) {
                    ps.setInt(count, finalProcessed);
                    count++;
                }
                if (queryKeys.contains("parent")) {
                    ps.setInt(count, finalParent);
                    count++;
                }

                ps.setInt(count, finalLimit);

                ResultSet results = ps.executeQuery();
                //display results
                while (results.next()) {

                    ContentObject content = contentWrapper.wrapContent(results);

                    if(((getUserVideos[0] && a.getUserId() != content.getUserId()) || getCategoryVideos[0])
                            && content.getContentType() != StaticRules.BUNDLE_CONTENT_TYPE){
                        continue;
                    }else{
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
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return;
        }
    }
}
