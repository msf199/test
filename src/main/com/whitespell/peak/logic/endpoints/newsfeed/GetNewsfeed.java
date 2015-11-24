package main.com.whitespell.peak.logic.endpoints.newsfeed;


import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.NewsfeedObject;

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
    private static final String NEWSFEED_CATEGORY_KEY = "categoryId";

    private static final String FIND_USER_FOLLOWING_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";

    //content keys
    private static final String CONTENT_ID_KEY = "content_id";

    @Override
    protected void setUserInputs() {
        urlInput.put(PROCESSING_URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        queryStringInput.put(NEWSFEED_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(NEWSFEED_OFFSET_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(NEWSFEED_CEIL_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(NEWSFEED_CATEGORY_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
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
        int categoryId = -1;
        boolean categorySelector = false;
        if(context.getQueryString().get(NEWSFEED_CATEGORY_KEY) != null){
            categoryId = Integer.parseInt(context.getQueryString().get(NEWSFEED_CATEGORY_KEY)[0]);
            categorySelector = true;
        }

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));
        boolean isMe = user_id == a.getUserId();

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        ContentWrapper contentWrapper = new ContentWrapper(context, user_id);

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
                    System.out.println("add userFollowing: " +results.getInt("following_id"));
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

            /**
             * Add newsfeed requesting user to followerIds to get your published content on your newsfeed
             */
            followerIds.add(user_id);

            int count = 1;

            /**
             * We only want to show videos that have been processed
             */
            String processedString = "AND `processed` = 1";

            /**
             * We only want to show videos that do not have any parents (are part of a bundle). We will show those by themselves.
             * Now handled when content is filled.
             */
            String parentString = " ";
                    //" AND `parent` IS NULL";

            /**
             * We only want to show newsfeed results from the category specified (if applicable)
             */
            String categoryString = " AND ct.`category_id` = " + categoryId + " ";

            for (Integer s : followerIds) {
                String ceilString = "";
                if (ceil > 0) {
                    ceilString = "AND ct.`content_id` < " + ceil;
                }

                /**
                 * If offset is set, use it. Otherwise check for content_id > 0
                 */
                if(offset > 0){
                    selectString.append("ct.`content_id` < " + offset + " " + ceilString + " " + processedString + " " + parentString + " AND ut.`user_id` = " + s + " ");

                }else{
                    selectString.append("ct.`content_id` > 0 " + ceilString + " " + processedString + " " + parentString + " AND ut.`user_id` = " + s + " ");
                }

                if(categorySelector){
                    selectString.append(categoryString);
                }
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
                ArrayList<Integer> bundleContentIds = new ArrayList<>();
                int[] newsfeedId = {0};
                StatementExecutor executor = new StatementExecutor(GET_FOLLOWERS_CONTENT_QUERY);
                executor.execute(ps -> {
                    ContentObject newsfeedContent;

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {

                        int currentContentId = results.getInt(CONTENT_ID_KEY);

                        /**
                         * Do not add intro video to newsfeed
                         */
                        if (currentContentId == Config.INTRO_CONTENT_ID) {
                            continue;
                        }

                        newsfeedContent = contentWrapper.wrapContent(results);

                        newsfeedId[0] = newsfeedContent.getContentId();

                        /**
                         * If current contentObject has a parent, check the id's of its contents. If any of the id's are greater than
                         * the bundle's contentId, move newsfeedContent up in the list.
                         */
                        int[] largestContentId = {0};
                        /**
                         * Only applies to children of a bundle
                         */
                        if (newsfeedContent.getParent() > 0) {
                            /**
                             * We already checked this bundle
                             */
                            if(bundleContentIds.contains(newsfeedContent.getParent())){
                                continue;
                            }

                            ContentHelper g = new ContentHelper();
                            try {
                                /**
                                 * Get the parent of the current contentObject
                                 */

                                ContentObject parent = g.getContentById(context, newsfeedContent.getParent(), a.getUserId());

                                /**
                                 * Return the parent bundle on the newsfeed since it has been updated since it was
                                 * uploaded.
                                 */
                                newsfeedContent = parent;

                                /**
                                 * For each child, if the child is newer than the bundle,
                                 * save the largest child and use that contentId to represent the bundle,
                                 * therefore moving it up in the newsfeed list (and maintaining offset order).
                                 */
                                for (ContentObject i : parent.getChildren()) {
                                    if (i.getContentId() > parent.getContentId()) {

                                        /**
                                         * Save the largest contentId in the bundle for updating the newsfeedId.
                                         */
                                        if (largestContentId[0] < i.getContentId()) {
                                            largestContentId[0] = i.getContentId();
                                        }

                                        /**
                                         * Set the newsfeedId to the largest child's contentId to maintain newsfeed order
                                         */
                                        newsfeedId[0] = largestContentId[0];
                                    }
                                }
                            } catch (UnirestException e) {
                                Logging.log("High", e);
                            }
                        }

                        /**
                         * Only show content that is standalone or a bundle. Show each content only once.
                         */
                        if (newsfeedContent.getParent() > 0 || bundleContentIds.contains(newsfeedContent.getContentId())) {
                            continue;
                        }

                        if (newsfeedContent.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE && newsfeedContent.getChildren().isEmpty()) {
                            // send notification to add videos to bundle todo(cmcan) to publisher
                        } else {
                            newsfeedResponse.add(new NewsfeedObject(newsfeedId[0], newsfeedContent));

                            /**
                             * Add the bundle to the bundle list to prevent duplicates.
                             */
                            if (newsfeedContent.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                                bundleContentIds.add(newsfeedContent.getContentId());
                            }
                        }
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
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
