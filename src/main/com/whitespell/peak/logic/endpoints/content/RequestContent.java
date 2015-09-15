package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

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

    private static final String CONTENT_CATEGORY_ID = "category_id";
    private static final String CONTENT_SIZE_LIMIT = "limit";
    private static final String CONTENT_OFFSET = "offset";
    private static final String CONTENT_ID_KEY = "content_id";
    private static final String CONTENT_TYPE_ID = "content_type";
    private static final String CONTENT_TITLE = "content_title";
    private static final String CONTENT_URL = "content_url";
    private static final String CONTENT_DESCRIPTION = "content_description";
    private static final String CONTENT_THUMBNAIL = "thumbnail_url";
    private static final String CONTENT_CURATION_ACCEPTED = "curation_accepted";

    private static final String GET_LIKES_QUERY = "SELECT `user_id` from `content_likes` WHERE `content_id` = ?";
    private static final String GET_USER_LIKED_QUERY = "SELECT `like_datetime` from `content_likes` WHERE `user_id` = ? AND `content_id` = ?";
    private static final String USER_OBJECT_QUERY = "SELECT * FROM `user` WHERE `user_id` = ?";

    private static final String QS_USER_ID = "userId";
    private static final String QS_CATEGORY_ID = "categoryId";
    private static final String QS_NOT_CURATED = "notCurated";

    @Override
    protected void setUserInputs() {
        queryStringInput.put(CONTENT_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(CONTENT_OFFSET, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_USER_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_CATEGORY_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_NOT_CURATED, StaticRules.InputTypes.REG_INT_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int temp = 0, temp2 = 0;
        ArrayList<String> queryKeys = new ArrayList<>();
        ArrayList<Integer> queryValues = new ArrayList<>();
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
                queryKeys.add("category_id");
                queryValues.add(temp2);
            }
        }
        if (urlQueryString.get(QS_NOT_CURATED) != null) {
            if(Integer.parseInt(urlQueryString.get(QS_NOT_CURATED)[0]) == 1) {
                queryKeys.add("curation_accepted");
            }
        }
        int userId = temp;
        int categoryId = temp2;
        int[] userLiked = {0};

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));
        int currentUser = a.getUserId();

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
            if(s.contains("curation_accepted")){
                selectString.append("AND `" + s + "` = ? ");
            }else{
                selectString.append("AND `" + s + "` = ? ");
            }
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
            StatementExecutor executor = new StatementExecutor(REQUEST_CONTENT);
            final int finalUserId = userId;
            executor.execute(ps -> {
                ps.setInt(1, finalOffset);
                int count = 2;

                if (queryValues.contains(finalUserId)) {
                    ps.setInt(count, finalUserId);
                    count++;
                }
                if (queryValues.contains(finalCategoryId)) {
                    ps.setInt(count, finalCategoryId);
                    count++;
                }
                if (queryKeys.contains("curation_accepted")) {
                    ps.setInt(count, 0);
                    count++;
                }

                ps.setInt(count, finalLimit);

                ResultSet results = ps.executeQuery();
                //display results
                while (results.next()) {
                    int[] contentLikes = {0};

                    /**
                     * Get the content likes
                     */
                    try {
                        StatementExecutor executor1 = new StatementExecutor(GET_LIKES_QUERY);
                        executor1.execute(ps2 -> {
                            ps2.setInt(1, results.getInt(CONTENT_ID_KEY));

                            ResultSet results1 = ps2.executeQuery();

                            //display results
                            while (results1.next()) {
                                contentLikes[0]++;
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                        return;
                    }

                    /**
                     * Check if the user has liked this content
                     */
                    try {
                        StatementExecutor executor1 = new StatementExecutor(GET_USER_LIKED_QUERY);
                        executor1.execute(ps2 -> {
                            ps2.setInt(1, currentUser);
                            ps2.setInt(2, results.getInt(CONTENT_ID_KEY));

                            ResultSet results1 = ps2.executeQuery();

                            //display results
                            if (results1.next()) {
                                userLiked[0] = 1;
                            } else {
                                userLiked[0] = 0;
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                        return;
                    }

                    UserObject contentPoster = new UserObject();
                    try {
                        StatementExecutor executor1 = new StatementExecutor(USER_OBJECT_QUERY);
                        executor1.execute(ps1 -> {
                            final int posterUserId = results.getInt("user_id");
                            ps1.setInt(1, posterUserId);

                            ResultSet results2 = ps1.executeQuery();
                            if (results2.next()) {
                                contentPoster.setUserId(posterUserId);
                                contentPoster.setUserName(results2.getString("username"));
                                contentPoster.setThumbnail(results2.getString("thumbnail"));
                            } else {
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                                return;
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                        return;
                    }

                    ContentObject content = new ContentObject(results.getInt(CONTENT_CATEGORY_ID), results.getInt("user_id"), results.getInt(CONTENT_ID_KEY), results.getInt(CONTENT_TYPE_ID), results.getString(CONTENT_TITLE),
                            results.getString(CONTENT_URL), results.getString(CONTENT_DESCRIPTION), results.getString(CONTENT_THUMBNAIL));
                    content.setPoster(contentPoster);
                    content.setLikes(contentLikes[0]);
                    content.setCurationAccepted(results.getInt(CONTENT_CURATION_ACCEPTED));
                    content.setUserLiked(userLiked[0]);
                    contents.add(content);
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
