package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.ContentHelper;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         7/28/15
 */
public class SavedContentAction extends EndpointHandler {

    private static final String INSERT_USER_CONTENT_SAVED = "INSERT INTO `content_saved` (`content_id`, `user_id`, `timestamp`) VALUES(?,?,?)";
    private static final String CHECK_DUPLICATE_CONTENT_IN_LIST = "SELECT `content_id` FROM `content_saved` WHERE `user_id` = ?";
    private static final String DELETE_CONTENT_SAVED_QUERY = "DELETE FROM `content_saved` WHERE `user_id` = ? AND `content_id` = ?";


    private static final String URL_USER_ID = "userId";
    private static final String CONTENT_ID = "contentId";
    private static final String ACTION_ID = "action";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(ACTION_ID, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        int content_id = j.get(CONTENT_ID).getAsInt();
        String action = j.get(ACTION_ID).getAsString();
        final Timestamp now = new Timestamp(Server.getMilliTime());


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
         * Ensure valid action
         */
        if(!action.equalsIgnoreCase("save") && !action.equalsIgnoreCase("unsave")){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INVALID_ACTION);
            return;
        }


        ContentHelper h = new ContentHelper();
        ContentObject currentContent = null;
        try{
            currentContent = h.getContentById(context, content_id, a.getUserId());
        }catch(Exception e){
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return;
        }

        /**
         * Do not allow individual videos to be saved
         */
        if(currentContent != null && currentContent.getContentType() != StaticRules.BUNDLE_CONTENT_TYPE){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * If payload action is save, check for duplicate and insert if not a duplicate
         */
        if(action.equalsIgnoreCase("save")) {
            /**
             * Check that the content is not already in the user's workouts with that contentId
             */
            try {
                StatementExecutor executor = new StatementExecutor(CHECK_DUPLICATE_CONTENT_IN_LIST);
                final int finalUser_id = user_id;
                executor.execute(ps -> {
                    ps.setInt(1, finalUser_id);

                    ResultSet results = ps.executeQuery();

                    while (results.next()) {
                        if (results.getInt("content_id") == content_id) {
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
                StatementExecutor executor = new StatementExecutor(INSERT_USER_CONTENT_SAVED);
                final int finalUser_id = user_id;
                final int finalContent_id = content_id;
                final SavedContentActionResponse object = new SavedContentActionResponse();
                executor.execute(ps -> {
                    ps.setInt(1, finalContent_id);
                    ps.setInt(2, finalUser_id);
                    ps.setTimestamp(3, now);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        object.setAddedContentId(finalContent_id);
                        object.setSuccess(true);
                        Gson g = new Gson();
                        String response = g.toJson(object);
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
                if (e.getMessage().contains("fk_content_saved_content_id")) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                }else if(e.getMessage().contains("fk_saved_content_user_id")){
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                }
                return;
            }
        }

        /**
         * Payload action is unsave, delete the saved content from the user's list
         */
        else{
            try {
                final SavedContentActionResponse object = new SavedContentActionResponse();

                StatementExecutor executor = new StatementExecutor(DELETE_CONTENT_SAVED_QUERY);
                executor.execute(ps -> {
                    ps.setInt(1, user_id);
                    ps.setInt(2, content_id);

                    int rows = ps.executeUpdate();
                    if(rows > 0){
                        System.out.println("contentId " + content_id + " was removed from the user's saved content successfully");
                        object.setRemovedContentId(content_id);
                        object.setSuccess(true);
                        Gson g = new Gson();
                        String response = g.toJson(object);
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
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }
    }


    public class SavedContentActionResponse {

        public SavedContentActionResponse(){
            this.addedContentId = -1;
            this.removedContentId = -1;
            this.success = false;
        }

        public int getAddedContentId() {
            return addedContentId;
        }

        public void setAddedContentId(int addedContentId) {
            this.addedContentId = addedContentId;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getRemovedContentId() {
            return removedContentId;
        }

        public void setRemovedContentId(int removedContentId) {
            this.removedContentId = removedContentId;
        }

        public int removedContentId;
        public int addedContentId;
        public boolean success;
    }
}

