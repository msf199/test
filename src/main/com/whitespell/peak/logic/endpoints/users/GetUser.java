package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
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

    private static final String QS_FOLLOWERS_KEY = "includeFollowing";

    private static final String FIND_FOLLOWING_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";
    private static final String GET_USER = "SELECT `user_id`, `username`, `displayname`, `email`, `thumbnail`, `cover_photo`, `slogan` FROM `user` WHERE `user_id` = ?";
    private static final String URL_USER_ID = "user_id";
    private static final String USERNAME_KEY = "username";
    private static final String DISPLAYNAME_KEY = "displayname";
    private static final String EMAIL_KEY = "email";
    private static final String THUMBNAIL_KEY = "thumbnail";
    private static final String COVER_PHOTO_KEY = "cover_photo";
    private static final String SLOGAN_KEY = "slogan";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        queryStringInput.put(QS_FOLLOWERS_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        boolean getFollowers = false;

        /**
         * Check if we want to see the users we are following
         */
        if(context.getQueryString().get(QS_FOLLOWERS_KEY) != null){
            if(context.getQueryString().get(QS_FOLLOWERS_KEY)[0].equals("1")){
                getFollowers = true;
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

        final ArrayList<Integer> initialFollowing = new ArrayList<>();
        if(getFollowers) {
            try {
                StatementExecutor executor = new StatementExecutor(FIND_FOLLOWING_QUERY);
                executor.execute(ps -> {
                    ps.setString(1, String.valueOf(user_id));
                    UserObject user = null;

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        initialFollowing.add(results.getInt("following_id"));
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }
        }

        try {
            StatementExecutor executor = new StatementExecutor(GET_USER);
            final int finalUser_id = user_id;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    UserObject user = null;

                    ps.setInt(1, finalUser_id);

                    final ResultSet results = ps.executeQuery();

                    if (results.next()) {
                        if(initialFollowing != null){
                            user = new UserObject(initialFollowing, results.getInt(URL_USER_ID), results.getString(USERNAME_KEY), results.getString(DISPLAYNAME_KEY),
                                    results.getString(EMAIL_KEY), results.getString(THUMBNAIL_KEY), results.getString(COVER_PHOTO_KEY), results.getString(SLOGAN_KEY));
                        }else{
                            user = new UserObject(results.getInt(URL_USER_ID), results.getString(USERNAME_KEY), results.getString(DISPLAYNAME_KEY),
                                    results.getString(EMAIL_KEY), results.getString(THUMBNAIL_KEY), results.getString(COVER_PHOTO_KEY), results.getString(SLOGAN_KEY));
                        }
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USER_NOT_FOUND);
                        return;
                    }

                    Gson g = new Gson();
                    String response = g.toJson(user);
                    context.getResponse().setStatus(200);
                    try {
                        context.getResponse().getWriter().write(response);
                    } catch (Exception e) {
                        Logging.log("High", e);
						return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
			return;
        }

    }
}
