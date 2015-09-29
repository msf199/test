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
    private static final String CONTENT_PRICE = "content_price";

    private static final String GET_BUNDLE_CHILDREN = "SELECT * FROM bundle_match INNER JOIN `content` ON content.content_id=bundle_match.child_content_id where parent_content_id = ?";

    private static final String GET_LIKES_QUERY = "SELECT `user_id` from `content_likes` WHERE `content_id` = ?";
    private static final String GET_USER_LIKED_QUERY = "SELECT `like_datetime` from `content_likes` WHERE `user_id` = ? AND `content_id` = ?";
    private static final String USER_OBJECT_QUERY = "SELECT * FROM `user` WHERE `user_id` = ?";

    private static final String QS_USER_ID = "userId";
    private static final String QS_CONTENT_ID = "contentId";
    private static final String QS_CONTENT_TYPE_ID = "contentType";
    private static final String QS_CATEGORY_ID = "categoryId";

    @Override
    protected void setUserInputs() {
        queryStringInput.put(CONTENT_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(CONTENT_OFFSET, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_USER_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_CONTENT_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_CONTENT_TYPE_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(QS_CATEGORY_ID, StaticRules.InputTypes.REG_INT_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int temp_user_id = 0, temp_content_id = 0, temp_content_type_id = 0, temp_category_id = 0;
        ArrayList<String> queryKeys = new ArrayList<>();
        Map<String, String[]> urlQueryString = context.getQueryString();


        if (urlQueryString.get(QS_USER_ID) != null) {
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
            if (Safety.isInteger(urlQueryString.get(QS_CATEGORY_ID)[0])
                    && Integer.parseInt(urlQueryString.get(QS_CATEGORY_ID)[0]) > 0) {
                temp_category_id = Integer.parseInt(urlQueryString.get(QS_CATEGORY_ID)[0]);
                queryKeys.add("category_id");
            }
        }

        int userId = temp_user_id;
        int contentId = temp_content_id;
        int contentType = temp_content_type_id;
        int categoryId = temp_category_id;
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
            selectString.append("AND `" + s + "` = ? ");
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

            StatementExecutor executor = new StatementExecutor(REQUEST_CONTENT);

            executor.execute(ps -> {
                ps.setInt(1, finalOffset);
                int count = 2;

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
                    content.setPrice(results.getDouble(CONTENT_PRICE));

                    if(content.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                        // we are entering a nested recursiveGetChildren loop
                          content.setChildren(recursiveGetChildren(content, context));
                    }
                    content.setPoster(contentPoster);
                    content.setLikes(contentLikes[0]);
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

    public ArrayList<ContentObject> recursiveGetChildren(ContentObject parent, RequestObject context) {
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_BUNDLE_CHILDREN);
            executor1.execute(ps -> {

                ps.setInt(1, parent.getContentId());

                ResultSet results = ps.executeQuery();
                while (results.next()) {
                    ContentObject child = new ContentObject(results.getInt(CONTENT_CATEGORY_ID), results.getInt("user_id"), results.getInt(CONTENT_ID_KEY), results.getInt(CONTENT_TYPE_ID), results.getString(CONTENT_TITLE),
                            results.getString(CONTENT_URL), results.getString(CONTENT_DESCRIPTION), results.getString(CONTENT_THUMBNAIL));

                    if(child.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                        child.setChildren(recursiveGetChildren(child, context));
                    }
                    parent.addChild(child);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
            return null;
        }
        return parent.getChildren();
    }

}
