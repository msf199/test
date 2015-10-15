package main.com.whitespell.peak.logic.endpoints.newsfeed;


import com.google.gson.Gson;
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

    private static final String FIND_USER_FOLLOWING_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";

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
             * We only want to show videos that do not have any parents (are part of a bundle). We will show those by themselves
             */
            String parentString = " AND `parent` IS NULL";

            for (Integer s : followerIds) {
                String ceilString = "";
                if (ceil > 0) {
                    ceilString = "AND ct.`content_id` < " + ceil;
                }
                selectString.append("ct.`content_id` > " + offset + " " + ceilString + " " + processedString + " " + parentString + " AND ut.`user_id` = " + s + " ");
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
                    ContentObject newsfeedContent;

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {


                        int currentContentId = results.getInt(CONTENT_ID_KEY);

                        /**
                         * Do not add intro video to newsfeed
                         */
                        if(currentContentId == Config.INTRO_CONTENT_ID){
                            continue;
                        }

                        newsfeedContent = contentWrapper.wrapContent(results);

                        contentIdSet.add(currentContentId);
                        if(newsfeedContent.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE && newsfeedContent.getChildren().isEmpty()) {
                            // send notification to add videos to bundle todo(cmcan) to publisher
                        } else {
                            newsfeedResponse.add(new NewsfeedObject(newsfeedContent.getContentId(), newsfeedContent));
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
