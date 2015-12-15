package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.UserHelper;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetUser extends EndpointHandler {

    private static final String QS_FOLLOWERS_KEY = "includeFollowers";
    private static final String QS_FOLLOWING_KEY = "includeFollowing";
    private static final String QS_CATEGORIES_KEY = "includeCategories";
    private static final String QS_PUBLISHING_KEY = "includePublishing";

    private static final String FIND_FOLLOWERS_QUERY = "SELECT `user_id` FROM `user_following` WHERE `following_id` = ?";
    private static final String FIND_FOLLOWING_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";
    private static final String FIND_CATEGORIES_QUERY = "SELECT `category_id` FROM `category_following` WHERE `user_id` = ?";
    private static final String FIND_PUBLISHING_QUERY = "SELECT `category_id` FROM `category_publishing` WHERE `user_id` = ?";
    private static final String GET_USER = "SELECT `user_id`, `username`, `displayname`, `email`, `thumbnail`, `cover_photo`, `slogan`, `publisher`, `email_verified`, `email_notifications`, `subscriber` FROM `user` WHERE `user_id` = ?";
    private static final String URL_USER_ID = "userId";
    private static final String USERNAME_KEY = "username";
    private static final String DISPLAYNAME_KEY = "displayname";
    private static final String EMAIL_KEY = "email";
    private static final String THUMBNAIL_KEY = "thumbnail";
    private static final String COVER_PHOTO_KEY = "cover_photo";
    private static final String SLOGAN_KEY = "slogan";
    private static final String PUBLISHER_KEY = "publisher";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        queryStringInput.put(QS_FOLLOWING_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        queryStringInput.put(QS_CATEGORIES_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        queryStringInput.put(QS_PUBLISHING_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        boolean getFollowers = false;
        boolean getFollowing = false;
        boolean getCategories = false;
        boolean getPublishing = false;

        /**
         * Check if we want to see the users we are following
         */
        if(context.getQueryString().get(QS_FOLLOWERS_KEY) != null){
            if(context.getQueryString().get(QS_FOLLOWERS_KEY)[0].equals("1")){
                getFollowers = true;
            }
        }

        /**
         * Check if we want to see the users we are following
         */
        if(context.getQueryString().get(QS_FOLLOWING_KEY) != null){
            if(context.getQueryString().get(QS_FOLLOWING_KEY)[0].equals("1")){
                getFollowing = true;
            }
        }

        /**
         * Check if we want to see the categories we are following
         */
        if(context.getQueryString().get(QS_CATEGORIES_KEY) != null){
            if(context.getQueryString().get(QS_CATEGORIES_KEY)[0].equals("1")){
                getCategories = true;
            }
        }

        /**
         * Check if we want to see the categories we are publishing in
         */
        if(context.getQueryString().get(QS_PUBLISHING_KEY) != null){
            if(context.getQueryString().get(QS_PUBLISHING_KEY)[0].equals("1")){
                getPublishing = true;
            }
        }

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (user_id == a.getUserId());

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        UserHelper uh = new UserHelper();
        UserObject u = uh.getUserById(user_id,  getFollowers,  getFollowing,  getCategories,  getPublishing);

        if(u == null) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USER_NOT_FOUND);
            return;
        } else {
            Gson g = new Gson();
            String response = g.toJson(u);
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
}
