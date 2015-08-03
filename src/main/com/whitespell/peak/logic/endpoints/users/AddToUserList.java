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
 *         8/03/15
 */
public class AddToUserList extends EndpointHandler {

    private static final String INSERT_USER_SAVED_CONTENT = "INSERT INTO `lists_saved` (`content_id`, `user_id`, `list_id`) VALUES(?,?,?)";
    private static final String CHECK_DUPLICATE_CONTENT_IN_LIST = "SELECT * FROM `lists_saved` WHERE `content_id` = ? AND `user_id` = ? AND `list_id` = ? LIMIT 1";
    private static final String URL_USER_ID = "userId";
    private static final String CONTENT_ID = "contentId";
    private static final String LIST_ID = "listId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(LIST_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        int content_id = Integer.parseInt(context.getPayload().getAsJsonObject().get(CONTENT_ID).getAsString());
        int list_id = Integer.parseInt(context.getPayload().getAsJsonObject().get(LIST_ID).getAsString());

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
         * Check that the content is not already in the user's list with that listId
         */
        try{
            StatementExecutor executor = new StatementExecutor(CHECK_DUPLICATE_CONTENT_IN_LIST);
            final int finalUser_id = user_id;
            final int finalContent_id = content_id;
            final int finalList_id = list_id;
            executor.execute(ps -> {
                ps.setInt(1, finalContent_id);
                ps.setInt(2, finalUser_id);
                ps.setInt(3, finalList_id);

                ResultSet result = ps.executeQuery();

                if (result.next()) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_ALREADY_IN_LIST);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }


        /**
         * Since the content has yet to be added to the user's list, add the content.
         */
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_USER_SAVED_CONTENT);
            final int finalUser_id = user_id;
            final int finalContent_id = content_id;
            final int finalList_id = list_id;
            final AddToSavedListResponse addToSavedListResponse = new AddToSavedListResponse();
            executor.execute(ps -> {
                ps.setInt(1, finalContent_id);
                ps.setInt(2, finalUser_id);
                ps.setInt(3, finalList_id);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    addToSavedListResponse.setAddedContentId(finalContent_id);
                    addToSavedListResponse.setAddedToListId(finalList_id);
                    Gson g = new Gson();
                    String response = g.toJson(addToSavedListResponse);
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
            if (e.getMessage().contains("fk_lists_saved_content_id")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            }else if(e.getMessage().contains("fk_lists_saved_user_id")){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
            }else{
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_ALREADY_IN_LIST);
            }
            return;
        }
    }


    public class AddToSavedListResponse {

        public AddToSavedListResponse(){
            this.addedContentId = -1;
        }

        public AddToSavedListResponse(int addedContentId, int addedToListId) {
            this.addedContentId = addedContentId;
        }

        public int getAddedContentId() {
            return addedContentId;
        }

        public void setAddedContentId(int addedContentId) {
            this.addedContentId = addedContentId;
        }

        public int getAddedToListId() {
            return addedToListId;
        }

        public void setAddedToListId(int addedToListId) {
            this.addedToListId = addedToListId;
        }

        public int addedContentId;
        public int addedToListId;
    }
}

