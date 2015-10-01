package main.com.whitespell.peak.logic.endpoints.content;

import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         10/1/2015
 */
public class ContentHelper {

    private static final String CONTENT_CATEGORY_ID = "category_id";
    private static final String CONTENT_ID_KEY = "content_id";
    private static final String CONTENT_TYPE_ID = "content_type";
    private static final String CONTENT_TITLE = "content_title";
    private static final String CONTENT_URL = "content_url";
    private static final String CONTENT_DESCRIPTION = "content_description";
    private static final String CONTENT_THUMBNAIL = "thumbnail_url";
    private static final String CONTENT_PRICE = "content_price";

    private static final String GET_BUNDLE_CHILDREN = "SELECT * FROM bundle_match INNER JOIN `content` ON content.content_id=bundle_match.child_content_id where parent_content_id = ?";

    private static final String GET_ACCESS_QUERY = "SELECT * from `content_access` WHERE `user_id` = ? AND `content_id` = ?";
    private static final String GET_LIKES_QUERY = "SELECT `user_id` from `content_likes` WHERE `content_id` = ?";
    private static final String GET_USER_LIKED_QUERY = "SELECT `like_datetime` from `content_likes` WHERE `user_id` = ? AND `content_id` = ?";
    private static final String USER_OBJECT_QUERY = "SELECT * FROM `user` WHERE `user_id` = ?";

    public static ArrayList<ContentObject> recursiveGetChildren(ContentObject parent, RequestObject context, int currentUserId) {
        int[] userLiked = {0};
        int[] hasAccess = {0};

        try {
            StatementExecutor executor1 = new StatementExecutor(GET_BUNDLE_CHILDREN);
            executor1.execute(ps -> {

                ps.setInt(1, parent.getContentId());

                ResultSet results = ps.executeQuery();
                while (results.next()) {


                    int[] contentLikes = {0};

                    /**
                     * Get the content likes
                     */
                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_LIKES_QUERY);
                        executor2.execute(ps2 -> {
                            ps2.setInt(1, results.getInt(CONTENT_ID_KEY));

                            ResultSet results1 = ps2.executeQuery();

                            //display results
                            while (results1.next()) {
                                contentLikes[0]++;
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError("recursiveGetChildren", StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                        return;
                    }

                    /**
                     * Check if the user has liked this content
                     */
                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_USER_LIKED_QUERY);
                        executor2.execute(ps2 -> {
                            ps2.setInt(1, currentUserId);
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
                        context.throwHttpError("recursiveGetChildren", StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                        return;
                    }

                    /**
                     * Check if the user has access to this content
                     */
                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_ACCESS_QUERY);
                        executor2.execute(ps1 -> {
                            ps1.setInt(1, currentUserId);
                            ps1.setInt(2, results.getInt(CONTENT_ID_KEY));

                            ResultSet results1 = ps1.executeQuery();

                            //display results
                            if (results1.next()) {
                                hasAccess[0] = 1;
                            } else {
                                hasAccess[0] = 0;
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError("recursiveGetChildren", StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                        return;
                    }

                    /**
                     * Get userObject of user that published this content
                     */
                    UserObject contentPoster = new UserObject();
                    try {
                        StatementExecutor executor2 = new StatementExecutor(USER_OBJECT_QUERY);
                        executor2.execute(ps1 -> {
                            final int posterUserId = results.getInt("user_id");
                            ps1.setInt(1, posterUserId);

                            ResultSet results2 = ps1.executeQuery();
                            if (results2.next()) {
                                contentPoster.setUserId(posterUserId);
                                contentPoster.setUserName(results2.getString("username"));
                                contentPoster.setThumbnail(results2.getString("thumbnail"));
                            } else {
                                context.throwHttpError("recursiveGetChildren", StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                                return;
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError("recursiveGetChildren", StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                        return;
                    }

                    String contentUrl = results.getString(CONTENT_URL);

                    /**
                     * If user doesn't have access, don't display the content url.
                     */
                    if(hasAccess[0] == 0){
                        contentUrl = null;
                    }

                    ContentObject child = new ContentObject(results.getInt(CONTENT_CATEGORY_ID), results.getInt("user_id"), results.getInt(CONTENT_ID_KEY), results.getInt(CONTENT_TYPE_ID), results.getString(CONTENT_TITLE),
                            contentUrl, results.getString(CONTENT_DESCRIPTION), results.getString(CONTENT_THUMBNAIL));

                    child.setContentPrice(results.getDouble(CONTENT_PRICE));

                    /**
                     * Set content access. If free, user has access
                     */
                    if(results.getDouble(CONTENT_PRICE) == 0.00){
                        child.setHasAccess(1);
                    }else{
                        child.setHasAccess(hasAccess[0]);
                    }

                    if(child.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                        // we are entering a nested recursiveGetChildren loop
                        child.setChildren(recursiveGetChildren(child, context, currentUserId));
                    }
                    child.setPoster(contentPoster);
                    child.setLikes(contentLikes[0]);
                    child.setUserLiked(userLiked[0]);

                    parent.addChild(child);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError("recursiveGetChildren", StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
            return null;
        }
        return parent.getChildren();
    }

    public static ContentObject constructContent(ResultSet contentResults, RequestObject context, int contentId, int currentUser){
        int[] contentLikes = {0};
        int[] userLiked = {0};
        int[] hasAccess = {0};
        ContentObject content = null;

        /**
         * Get the content likes
         */
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_LIKES_QUERY);
            executor1.execute(ps2 -> {
                ps2.setInt(1, contentId);

                ResultSet results1 = ps2.executeQuery();

                //display results
                while (results1.next()) {
                    contentLikes[0]++;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError("constructContent", StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return null;
        }

        /**
         * Check if the user has liked this content
         */
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_USER_LIKED_QUERY);
            executor1.execute(ps2 -> {
                ps2.setInt(1, currentUser);
                ps2.setInt(2, contentId);

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
            context.throwHttpError("constructContent", StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return null;
        }

        /**
         * Check if the user has access to this content
         */
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_ACCESS_QUERY);
            executor1.execute(ps1 -> {
                ps1.setInt(1, currentUser);
                ps1.setInt(2, contentResults.getInt(CONTENT_ID_KEY));

                ResultSet results1 = ps1.executeQuery();

                //display results
                if (results1.next()) {
                    hasAccess[0] = 1;
                } else {
                    hasAccess[0] = 0;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError("constructContent", StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return null;
        }

        /**
         * Get userObject of user that published this content
         */
        UserObject contentPoster = new UserObject();
        try {
            StatementExecutor executor1 = new StatementExecutor(USER_OBJECT_QUERY);
            executor1.execute(ps1 -> {
                final int posterUserId = contentResults.getInt("user_id");
                ps1.setInt(1, posterUserId);

                ResultSet results2 = ps1.executeQuery();
                if (results2.next()) {
                    contentPoster.setUserId(posterUserId);
                    contentPoster.setUserName(results2.getString("username"));
                    contentPoster.setThumbnail(results2.getString("thumbnail"));
                } else {
                    context.throwHttpError("constructContent", StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError("constructContent", StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
            return null;
        }

        try {
            String contentUrl = contentResults.getString(CONTENT_URL);

            /**
             * If user doesn't have access, don't display the content url.
             */
            if(hasAccess[0] == 0){
                contentUrl = null;
            }

            content = new ContentObject(contentResults.getInt(CONTENT_CATEGORY_ID), contentResults.getInt("user_id"), contentResults.getInt(CONTENT_ID_KEY), contentResults.getInt(CONTENT_TYPE_ID), contentResults.getString(CONTENT_TITLE),
                    contentUrl, contentResults.getString(CONTENT_DESCRIPTION), contentResults.getString(CONTENT_THUMBNAIL));
            content.setContentPrice(contentResults.getDouble(CONTENT_PRICE));

            /**
             * Set content access. If free, user has access
             */
            if (contentResults.getDouble(CONTENT_PRICE) == 0.00) {
                content.setHasAccess(1);
            } else {
                content.setHasAccess(hasAccess[0]);
            }

            if(content.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                // we are entering a nested recursiveGetChildren loop
                content.setChildren(recursiveGetChildren(content, context, currentUser));
            }
            content.setPoster(contentPoster);
            content.setLikes(contentLikes[0]);
            content.setUserLiked(userLiked[0]);
        }
        catch(SQLException e){
            Logging.log("High", e);
            context.throwHttpError("constructContent", StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return null;
        }

        return content;
    }
}
