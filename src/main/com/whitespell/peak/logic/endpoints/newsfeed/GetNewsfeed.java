package main.com.whitespell.peak.logic.endpoints.newsfeed;


import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.GenericAPIActions;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.ContentWrapper;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.NewsfeedObject;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         7/15/2015
 */
public class GetNewsfeed extends EndpointHandler {

    private static final String PROCESSING_URL_USER_ID = "userId";
    private static final String NEWSFEED_SIZE_LIMIT = "limit";
    private static final String NEWSFEED_OFFSET_KEY = "offset";
    private static final String NEWSFEED_CEIL_KEY = "ceil";

    private static final String FIND_USER_FOLLOWING_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";
    private static final String FIND_CATEGORY_FOLLOWING_QUERY = "SELECT `category_id` FROM `category_following` WHERE `user_id` = ?";

    //user keys
    private static final String USER_ID_KEY = "user_id";
    private static final String USERNAME_KEY = "username";
    private static final String DISPLAYNAME_KEY = "displayname";
    private static final String EMAIL_KEY = "email";
    private static final String THUMBNAIL_KEY = "thumbnail";
    private static final String COVER_PHOTO_KEY = "cover_photo";
    private static final String SLOGAN_KEY = "slogan";
    private static final String PUBLISHER_KEY = "publisher";

    //content keys
    private static final String CONTENT_ID_KEY = "content_id";

    @Override
    protected void setUserInputs() {
        urlInput.put(PROCESSING_URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        queryStringInput.put(NEWSFEED_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(NEWSFEED_OFFSET_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(NEWSFEED_CEIL_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        final int user_id = Integer.parseInt(context.getUrlVariables().get(PROCESSING_URL_USER_ID));
        int limit = GenericAPIActions.getLimit(context.getQueryString());
        int offset = GenericAPIActions.getOffset(context.getQueryString());
        int ceil = GenericAPIActions.getCeil(context.getQueryString());
        ArrayList<Integer> followerIds = new ArrayList<>();
        ArrayList<Integer> categoryIds = new ArrayList<>();
        ArrayList<NewsfeedObject> newsfeedResponse = new ArrayList<>();
        Set<Integer> contentIdSet = new HashSet<>();

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
         * Get the userIds current user is following.
         */
        try {
            StatementExecutor executor = new StatementExecutor(FIND_USER_FOLLOWING_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, user_id);

                ResultSet results = ps.executeQuery();
                while (results.next()) {
                    followerIds.add(results.getInt("following_id"));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Get the userIds current user is following.
         */
        try {
            StatementExecutor executor = new StatementExecutor(FIND_USER_FOLLOWING_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, user_id);

                ResultSet results = ps.executeQuery();
                while (results.next()) {
                    followerIds.add(results.getInt("following_id"));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Ensure user is following another user
         */
        if(followerIds != null && followerIds.size() > 0) {
            /**
             * Construct the SELECT FROM CONTENT query based on the the desired query output.
             */
            StringBuilder selectString = new StringBuilder();
            selectString.append("SELECT DISTINCT * FROM `content` as ct " +
                    "INNER JOIN `user` as ut ON ct.`user_id` = ut.`user_id` WHERE ");
            int count = 1;
            for (Integer s : followerIds) {
                String ceilString = "";
                if (ceil > 0) {
                    ceilString = "AND ct.`content_id` < " + ceil;
                }
                selectString.append("ct.`content_id` > " + offset + " " + ceilString + " AND ut.`user_id` = " + s + " ");
                if (count < followerIds.size()) {
                    selectString.append(" OR ");
                    count++;
                }
            }
            selectString.append("ORDER BY ct.`content_id` DESC LIMIT " + limit);

            final String GET_FOLLOWERS_CONTENT_QUERY = selectString.toString();

            /**
             * Get content based on users you are following and construct newsfeed
             */
            try {
                StatementExecutor executor = new StatementExecutor(GET_FOLLOWERS_CONTENT_QUERY);
                executor.execute(ps -> {
                    UserObject followedUser;
                    ContentObject newsfeedContent;

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        if (results.getInt("is_child") == 1) {
                            continue;
                        }

                        int currentContentId = results.getInt(CONTENT_ID_KEY);

                        followedUser = new UserObject(results.getInt(USER_ID_KEY), results.getString(USERNAME_KEY),
                                results.getString(DISPLAYNAME_KEY), results.getString(EMAIL_KEY), results.getString(THUMBNAIL_KEY),
                                results.getString(COVER_PHOTO_KEY), results.getString(SLOGAN_KEY), results.getInt(PUBLISHER_KEY));

                        newsfeedContent = contentWrapper.wrapContent(results);

                        contentIdSet.add(currentContentId);
                        newsfeedResponse.add(new NewsfeedObject(newsfeedContent.getContentId(), followedUser, newsfeedContent));
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }

        /**
         * If newsfeed size returned is smaller than limit, populate remaining space with category following
         */
        if(newsfeedResponse.size() < limit) {
            int remaining = limit - newsfeedResponse.size();

            /**
             * Get the categoryIds current user is following.
             */
            try {
                StatementExecutor executor = new StatementExecutor(FIND_CATEGORY_FOLLOWING_QUERY);
                executor.execute(ps -> {
                    ps.setInt(1, user_id);

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        categoryIds.add(results.getInt("category_id"));
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }

            if (categoryIds != null && categoryIds.size() > 0) {
                /**
                 * Construct the SELECT FROM CONTENT query based on the the desired query output.
                 */
                StringBuilder selectString1 = new StringBuilder();
                selectString1.append("SELECT DISTINCT * FROM `content` as ct " +
                        "INNER JOIN `user` as ut ON ct.`user_id` = ut.`user_id` WHERE ");
                int count1 = 1;
                for (Integer s : categoryIds) {
                    String ceilString = "";
                    if (ceil > 0) {
                        ceilString = " AND ct.`content_id` < " + ceil;
                    }
                    selectString1.append("(ct.`content_id` > " + offset + " " + ceilString + " AND ct.`category_id` = " + s + ") ");
                    if (count1 < categoryIds.size()) {
                        selectString1.append(" OR ");
                        count1++;
                    }
                }
                selectString1.append("ORDER BY ct.`content_id` DESC LIMIT " + remaining);
                final String GET_CATEGORY_FOLLOWING_CONTENT_QUERY = selectString1.toString();

                /**
                 * Get content based on categories you are following and append to newsfeed
                 */
                try {
                    StatementExecutor executor = new StatementExecutor(GET_CATEGORY_FOLLOWING_CONTENT_QUERY);
                    executor.execute(ps -> {
                        UserObject followedUser;
                        ContentObject newsfeedContent;

                        ResultSet results = ps.executeQuery();
                        while (results.next()) {
                            int currentContentId = results.getInt(CONTENT_ID_KEY);

                            if (results.getInt("is_child") == 1 || contentIdSet.contains(currentContentId)) {
                                continue;
                            }

                            followedUser = new UserObject(results.getInt(USER_ID_KEY), results.getString(USERNAME_KEY),
                                    results.getString(DISPLAYNAME_KEY), results.getString(EMAIL_KEY), results.getString(THUMBNAIL_KEY),
                                    results.getString(COVER_PHOTO_KEY), results.getString(SLOGAN_KEY), results.getInt(PUBLISHER_KEY));

                            newsfeedContent = contentWrapper.wrapContent(results);
                            newsfeedContent.setRecommended(1);

                            contentIdSet.add(currentContentId);
                            newsfeedResponse.add(new NewsfeedObject(newsfeedContent.getContentId(), followedUser, newsfeedContent));
                        }
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            }
        }

        if(newsfeedResponse.size() == 0){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMPTY_NEWSFEED);
            return;
        }

        final Gson f = new Gson();
        String response = f.toJson(newsfeedResponse);
        context.getResponse().setStatus(200);
        try {
            context.getResponse().getWriter().write(response);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }
}
