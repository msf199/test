package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.StaticRules;
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
public class ContentWrapper {


    /** content table keys */
    private static final String CONTENT_CATEGORY_ID = "category_id";
    private static final String CONTENT_ID_KEY = "content_id";
    private static final String CONTENT_TYPE_ID = "content_type";
    private static final String CONTENT_TITLE = "content_title";
    private static final String CONTENT_LIKES_KEY = "content_likes";
    private static final String CONTENT_VIEWS_KEY = "content_views";
    private static final String CONTENT_URL = "content_url";


    // content urls
    private static final String CONTENT_URL_1080P = "content_url_1080p";
    private static final String CONTENT_URL_720P = "content_url_720p";
    private static final String CONTENT_URL_480P = "content_url_480p";
    private static final String CONTENT_URL_360P = "content_url_360p";
    private static final String CONTENT_URL_240P = "content_url_240p";
    private static final String CONTENT_URL_144P = "content_url_144p";

    // content previews
    private static final String CONTENT_PREVIEW_1080P = "content_preview_1080p";
    private static final String CONTENT_PREVIEW_720P = "content_preview_720p";
    private static final String CONTENT_PREVIEW_480P = "content_preview_480p";
    private static final String CONTENT_PREVIEW_360P = "content_preview_360p";
    private static final String CONTENT_PREVIEW_240P = "content_preview_240p";
    private static final String CONTENT_PREVIEW_144P = "content_preview_144p";

    // content thumbnails
    private static final String THUMBNAIL_1080P = "thumbnail_1080p";
    private static final String THUMBNAIL_720P = "thumbnail_720p";
    private static final String THUMBNAIL_480P = "thumbnail_480p";
    private static final String THUMBNAIL_360P = "thumbnail_360p";
    private static final String THUMBNAIL_240P = "thumbnail_240p";
    private static final String THUMBNAIL_144P = "thumbnail_144p";

    private static final String SOCIAL_MEDIA_VIDEO = "social_media_video";
    private static final String VIDEO_LENGTH_SECONDS = "video_length_seconds";







    private static final String CONTENT_DESCRIPTION = "content_description";
    private static final String CONTENT_THUMBNAIL = "thumbnail_url";
    private static final String CONTENT_PRICE = "content_price";
    private static final String USER_ID_KEY = "user_id";
    private static final String PROCESSED_KEY = "processed";
    private static final String PARENT_KEY = "parent";


    /** user table keys **/
     private static final String USERNAME_KEY = "username";
    private static final String DISPLAYNAME_KEY = "displayname";
    private static final String EMAIL_KEY = "email";
    private static final String THUMBNAIL_KEY = "thumbnail";
    private static final String COVER_PHOTO_KEY = "cover_photo";
    private static final String SLOGAN_KEY = "slogan";


    private static final String GET_BUNDLE_CHILDREN =
            "SELECT * FROM bundle_match as bm" +
            " INNER JOIN `content` as ct ON ct.content_id=bm.child_content_id" +
            " INNER JOIN `user` as ut ON ct.`user_id` = ut.`user_id`" +
            " WHERE bm.parent_content_id = ?";

    // get the content ids of the user's likes
    private static final String GET_USER_LIKED_QUERY = "SELECT `content_id` from `content_likes` WHERE `user_id` = ?";

    // get the access ids from the users access
    private static final String GET_USER_ACCESS_QUERY = "SELECT `content_id` from `content_access` WHERE `user_id` = ?";

    // get the access ids from the users views
    private static final String GET_USER_VIEW_QUERY = "SELECT `content_id` from `content_views` WHERE `user_id` = ?";

    // get the access ids from the users views
    private static final String GET_USER_SAVED_QUERY = "SELECT `content_id` from `content_saved` WHERE `user_id` = ?";


    private RequestObject context;
    private int requesterUserId;
    private ArrayList<Integer> userLikes;
    private ArrayList<Integer> userAccess;
    private ArrayList<Integer> userViewed;
    private ArrayList<Integer> userSaved;

    public ContentWrapper(RequestObject context, int requesterUserId) {
        this.context = context;
        this.requesterUserId = requesterUserId;

        this.userLikes = this.getContentLiked(this.requesterUserId);
        this.userAccess = this.getContentAccess(this.requesterUserId);
        this.userViewed = this.getContentViewed(this.requesterUserId);
        this.userSaved = this.getContentSaved(this.requesterUserId);
    }

    /**
     * *
     * <p>
     * Rather than getting all the user's liked content each loop, we will index the content ids of liked content and compare against it in loops
     * ^ same for access
     * We're adding a +1 / -1 function to the content table for content_likes and content_views so we don't have to query again.
     * Make this stuff digestable
     * JOIN with user for every single content call
     */


    private ArrayList<Integer> getContentLiked(int userId) {

        final ArrayList<Integer> tempList = new ArrayList<>();
        /**
         * Get all the content this user has liked
         */
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_USER_LIKED_QUERY);
            executor1.execute(ps2 -> {
                ps2.setInt(1, userId);

                ResultSet results1 = ps2.executeQuery();

                //display results
                while (results1.next()) {
                    tempList.add(results1.getInt(CONTENT_ID_KEY));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            return null;
        }

        return tempList;

    }

    private ArrayList<Integer> getContentAccess(int userId) {

        final ArrayList<Integer> tempList = new ArrayList<>();
        /**
         * Get all the content this user has access to
         */
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_USER_ACCESS_QUERY);
            executor1.execute(ps2 -> {
                ps2.setInt(1, userId);

                ResultSet results1 = ps2.executeQuery();

                //display results
                while (results1.next()) {
                    tempList.add(results1.getInt(CONTENT_ID_KEY));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            return null;
        }

        return tempList;

    }

    private ArrayList<Integer> getContentViewed(int userId) {

        final ArrayList<Integer> tempList = new ArrayList<>();
        /**
         * Get all the content this user has viewed
         */
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_USER_VIEW_QUERY);
            executor1.execute(ps2 -> {
                ps2.setInt(1, userId);

                ResultSet results1 = ps2.executeQuery();

                //display results
                while (results1.next()) {
                    tempList.add(results1.getInt(CONTENT_ID_KEY));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            return null;
        }

        return tempList;

    }

    private ArrayList<Integer> getContentSaved(int userId) {

        final ArrayList<Integer> tempList = new ArrayList<>();
        /**
         * Get all the content this user has saved
         */
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_USER_SAVED_QUERY);
            executor1.execute(ps2 -> {
                ps2.setInt(1, userId);

                ResultSet results1 = ps2.executeQuery();

                //display results
                while (results1.next()) {
                    tempList.add(results1.getInt(CONTENT_ID_KEY));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            return null;
        }

        return tempList;

    }



    private ContentObject personalizeContent(ContentObject tempContent, UserObject tempPublisher, ResultSet currentObject) {

        int currentContentId = tempContent.getContentId();
        try {

            //todo(cmcan) REMOVE AFTER BETA TO ALLOW PURCHASES
            /**
            * BETA ACCESS AND FREE CONTENT UNTIL RELEASE (around dec, 15, 2015)
             * Allow publisher to see the price of their content
            */
            tempContent.setHasAccess(1);
            if(requesterUserId != tempContent.getUserId()) {
                tempContent.setContentPrice(0);
            }


            /**
             * AFTER BETA, UNCOMMENT BELOW:
             */
            /**
             * Set content access. If free or the user is the publisher, user has access
             */
            /*if (currentObject.getDouble(CONTENT_PRICE) == 0.00 || requesterUserId == tempContent.getUserId()) {
                tempContent.setHasAccess(1);
            } else if (userAccess.contains(currentContentId)) {
                tempContent.setHasAccess(1);
            } else {
                tempContent.setHasAccess(0);
                tempContent.setContentUrl(null);
            }*/

            /** Construct the poster **/

            tempContent.setPoster(tempPublisher);
            tempContent.setLikes(currentObject.getInt(CONTENT_LIKES_KEY));
            tempContent.setViews(currentObject.getInt(CONTENT_VIEWS_KEY));
            tempContent.setUserLiked(userLikes.contains(currentContentId) ? 1 : 0);
            tempContent.setUserSaved(userSaved.contains(currentContentId) ? 1 : 0);
            tempContent.setUserViewed(userViewed.contains(currentContentId) ? 1 : 0);

        } catch(SQLException e) {
            Logging.log("High", e);
        }

        return tempContent;
    }


    /**
     * wrapContent is a centralized way to construct content that can be returned in JSON responses throughout the entire API
     * Takes care of making it personalized and recurses bundles
     * @param currentObject an iteratable ResultSet of results from the database
     * @return
     */

    public ContentObject wrapContent(ResultSet currentObject) {

        ContentObject tempContent;
        UserObject tempPublisher;

        try {
            tempContent = new ContentObject(
                    currentObject.getInt(CONTENT_CATEGORY_ID),
                    currentObject.getInt(USER_ID_KEY),
                    currentObject.getInt(CONTENT_ID_KEY),
                    currentObject.getInt(CONTENT_TYPE_ID),
                    currentObject.getString(CONTENT_TITLE),
                    currentObject.getString(CONTENT_URL),

                    currentObject.getString(CONTENT_URL_1080P),
                    currentObject.getString(CONTENT_URL_720P),
                    currentObject.getString(CONTENT_URL_480P),
                    currentObject.getString(CONTENT_URL_360P),
                    currentObject.getString(CONTENT_URL_240P),
                    currentObject.getString(CONTENT_URL_144P),

                    currentObject.getString(CONTENT_PREVIEW_1080P),
                    currentObject.getString(CONTENT_PREVIEW_720P),
                    currentObject.getString(CONTENT_PREVIEW_480P),
                    currentObject.getString(CONTENT_PREVIEW_360P),
                    currentObject.getString(CONTENT_PREVIEW_240P),
                    currentObject.getString(CONTENT_PREVIEW_144P),

                    currentObject.getString(THUMBNAIL_1080P),
                    currentObject.getString(THUMBNAIL_720P),
                    currentObject.getString(THUMBNAIL_480P),
                    currentObject.getString(THUMBNAIL_360P),
                    currentObject.getString(THUMBNAIL_240P),
                    currentObject.getString(THUMBNAIL_144P),
                    currentObject.getInt(VIDEO_LENGTH_SECONDS),

                    currentObject.getString(SOCIAL_MEDIA_VIDEO),

                    currentObject.getString(CONTENT_DESCRIPTION),
                    currentObject.getString(CONTENT_THUMBNAIL),
                    currentObject.getDouble(CONTENT_PRICE),
                    currentObject.getInt(PROCESSED_KEY),
                    currentObject.getInt(PARENT_KEY)
            );

            /**
             * int categoryId,
             int userId,
             int contentId,
             int contentType,
             String contentTitle,
             String contentUrl,
             String contentUrl1080p,
             String contentUrl720p,
             String contentUrl480p,
             String contentUrl360p,
             String contentUrl240p,
             String contentUrl144p,
             String contentPreview1080p,
             String contentPreview720p,
             String contentPreview480p,
             String contentPreview360p,
             String contentPreview240p,
             String contentPreview144p,
             String thumbnail1080p,
             String thumbnail720p,
             String thumbnail480p,
             String thumbnail360p,
             String thumbnail240p,
             String thumbnail144p,
             String contentDescription,
             String thumbnailUrl,
             double contentPrice,
             int processed,
             int parent
             */

            tempPublisher = new UserObject(
                    currentObject.getInt(USER_ID_KEY),
                    currentObject.getString(USERNAME_KEY),
                    currentObject.getString(DISPLAYNAME_KEY),
                    currentObject.getString(EMAIL_KEY),
                    currentObject.getString(THUMBNAIL_KEY),
                    currentObject.getString(COVER_PHOTO_KEY),
                    currentObject.getString(SLOGAN_KEY),
                    1 // always a publisher
            );

            tempContent = this.personalizeContent(tempContent, tempPublisher, currentObject);

            if (tempContent.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                // we are entering a nested recursiveGetChildren loop
                tempContent.setChildren(this.recursiveGetChildren(tempContent, context));
            }

        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError("wrapContent", StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return null;
        }

        return tempContent;
    }

    public ArrayList<ContentObject> recursiveGetChildren(ContentObject parent, RequestObject context) {
        try {
            StatementExecutor executor1 = new StatementExecutor(GET_BUNDLE_CHILDREN);
            executor1.execute(ps -> {

                ps.setInt(1, parent.getContentId());

                ResultSet results = ps.executeQuery();
                while (results.next()) {

                    /** Construct the child **/

                    ContentObject child = new ContentObject(
                            results.getInt(CONTENT_CATEGORY_ID),
                            results.getInt(USER_ID_KEY),
                            results.getInt(CONTENT_ID_KEY),
                            results.getInt(CONTENT_TYPE_ID),
                            results.getString(CONTENT_TITLE),
                            results.getString(CONTENT_URL),

                            results.getString(CONTENT_URL_1080P),
                            results.getString(CONTENT_URL_720P),
                            results.getString(CONTENT_URL_480P),
                            results.getString(CONTENT_URL_360P),
                            results.getString(CONTENT_URL_240P),
                            results.getString(CONTENT_URL_144P),

                            results.getString(CONTENT_PREVIEW_1080P),
                            results.getString(CONTENT_PREVIEW_720P),
                            results.getString(CONTENT_PREVIEW_480P),
                            results.getString(CONTENT_PREVIEW_360P),
                            results.getString(CONTENT_PREVIEW_240P),
                            results.getString(CONTENT_PREVIEW_144P),

                            results.getString(THUMBNAIL_1080P),
                            results.getString(THUMBNAIL_720P),
                            results.getString(THUMBNAIL_480P),
                            results.getString(THUMBNAIL_360P),
                            results.getString(THUMBNAIL_240P),
                            results.getString(THUMBNAIL_144P),
                            results.getInt(VIDEO_LENGTH_SECONDS),

                            results.getString(SOCIAL_MEDIA_VIDEO),

                            results.getString(CONTENT_DESCRIPTION),
                            results.getString(CONTENT_THUMBNAIL),
                            results.getDouble(CONTENT_PRICE),
                            results.getInt(PROCESSED_KEY),
                            results.getInt(PARENT_KEY)
                    );

                    UserObject tempPublisher = new UserObject(
                            results.getInt(USER_ID_KEY),
                            results.getString(USERNAME_KEY),
                            results.getString(DISPLAYNAME_KEY),
                            results.getString(EMAIL_KEY),
                            results.getString(THUMBNAIL_KEY),
                            results.getString(COVER_PHOTO_KEY),
                            results.getString(SLOGAN_KEY),
                            1 // always a publisher
                    );

                    child = this.personalizeContent(child, tempPublisher, results);

                /** personalize the child **/

                    if (child.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
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
