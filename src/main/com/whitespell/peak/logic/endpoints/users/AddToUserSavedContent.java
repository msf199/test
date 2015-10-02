package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         7/28/15
 */
public class AddToUserSavedContent extends EndpointHandler {

    private static final String INSERT_USER_content_saved = "INSERT INTO `content_saved` (`content_id`, `user_id`) VALUES(?,?)";
    private static final String CHECK_DUPLICATE_CONTENT_IN_LIST = "SELECT `content_id` FROM `content_saved` WHERE `user_id` = ?";

    private static final String URL_USER_ID = "userId";
    private static final String CONTENT_ID = "contentId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        int content_id = Integer.parseInt(context.getPayload().getAsJsonObject().get(CONTENT_ID).getAsString());

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (user_id == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Check that the content is not already in the user's workouts with that contentId
         */
        try{
            StatementExecutor executor = new StatementExecutor(CHECK_DUPLICATE_CONTENT_IN_LIST);
            final int finalUser_id = user_id;
            executor.execute(ps -> {
                ps.setInt(1, finalUser_id);

                ResultSet results = ps.executeQuery();

                while (results.next()) {
                    if(results.getInt("content_id") == content_id) {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_ALREADY_IN_BUNDLE);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Insert the new workout into the user's myWorkouts list
         */
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_USER_content_saved);
            final int finalUser_id = user_id;
            final int finalContent_id = content_id;
            final AddToSavedContentResponse addToSavedContentResponse = new AddToSavedContentResponse();
            executor.execute(ps -> {
                ps.setInt(1, finalContent_id);
                ps.setInt(2, finalUser_id);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    addToSavedContentResponse.setAddedContentId(finalContent_id);
                    Gson g = new Gson();
                    String response = g.toJson(addToSavedContentResponse);
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
            if (e.getMessage().contains("fk_lists_workout_content_id")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            }else if(e.getMessage().contains("fk_lists_workout_user_id")){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
            }else{
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_ALREADY_IN_BUNDLE);
            }
            return;
        }
    }


    public class AddToSavedContentResponse {

        public AddToSavedContentResponse(){
            this.addedContentId = -1;
        }

        public AddToSavedContentResponse(int addedContentId) {
            this.addedContentId = addedContentId;
        }

        public int getAddedContentId() {
            return addedContentId;
        }

        public void setAddedContentId(int addedContentId) {
            this.addedContentId = addedContentId;
        }

        public int addedContentId;
    }
}

