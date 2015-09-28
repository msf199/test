package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         7/28/15
 */
public class GetUserSavedContent extends EndpointHandler {

    private static final String GET_USER_SAVED_CONTENT_ID_QUERY = "SELECT `content_id` FROM `saved_content` WHERE `user_id` = ?";
    private static final String GET_CONTENT_OBJECT_QUERY = "SELECT * FROM `content` WHERE `content_id` = ?";

    private static final String URL_USER_ID = "userId";

    private static final String CONTENT_CATEGORY_ID = "category_id";
    private static final String CONTENT_ID_KEY = "content_id";
    private static final String CONTENT_TYPE_ID = "content_type";
    private static final String CONTENT_TITLE = "content_title";
    private static final String CONTENT_URL = "content_url";
    private static final String CONTENT_DESCRIPTION = "content_description";
    private static final String CONTENT_THUMBNAIL = "thumbnail_url";
    private static final String CONTENT_PRICE = "content_price";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (user_id == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        final getSavedContent getSavedContent = new getSavedContent();
        try {
            StatementExecutor executor = new StatementExecutor(GET_USER_SAVED_CONTENT_ID_QUERY);
            final int finalUser_id = user_id;

            executor.execute(ps -> {
                ps.setInt(1, finalUser_id);

                ResultSet results = ps.executeQuery();

                while (results.next()) {
                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_CONTENT_OBJECT_QUERY);
                        final int finalContent_id = results.getInt(CONTENT_ID_KEY);
                        executor2.execute(ps2 -> {
                            ps2.setInt(1, finalContent_id);

                            ResultSet results2 = ps2.executeQuery();

                            if (results2.next()) {
                                ContentObject c = new ContentObject(results2.getInt(CONTENT_CATEGORY_ID), results2.getInt("user_id"), results2.getInt(CONTENT_ID_KEY),
                                        results2.getInt(CONTENT_TYPE_ID), results2.getString(CONTENT_TITLE), results2.getString(CONTENT_URL), results2.getString(CONTENT_DESCRIPTION),
                                        results2.getString(CONTENT_THUMBNAIL));
                                c.setPrice(results2.getDouble(CONTENT_PRICE));
                                getSavedContent.addToSavedContent(c);
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        if (e.getMessage().contains("fk_lists_workout_content_id")) {
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                        }else if(e.getMessage().contains("fk_lists_workout_user_id")){
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                        }
                        return;
                    }
                }
                Gson g = new Gson();
                String response = g.toJson(getSavedContent);
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
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class getSavedContent {

        public getSavedContent() {
            this.savedContent = new ArrayList<>();
        }

        public ArrayList<ContentObject> getSavedContent() {
            return savedContent;
        }

        public void addToSavedContent(ContentObject content) {
            savedContent.add(content);
        }

        public ArrayList<ContentObject> savedContent;
    }
}


