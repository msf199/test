package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         7/28/15
 */
public class AddToUserWorkout extends EndpointHandler {

    private static final String INSERT_USER_WORKOUT = "INSERT INTO `lists_workout` (`content_id`, `user_id`) VALUES(?,?)";
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

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_USER_WORKOUT);
            final int finalUser_id = user_id;
            final int finalContent_id = content_id;
            final AddToWorkoutResponse addToWorkoutResponse = new AddToWorkoutResponse();
            executor.execute(ps -> {
                ps.setInt(1, finalContent_id);
                ps.setInt(2, finalUser_id);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    addToWorkoutResponse.setAddedContentId(finalContent_id);
                    Gson g = new Gson();
                    String response = g.toJson(addToWorkoutResponse);
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
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND_OR_IN_LIST);
            return;
        }
    }


    public class AddToWorkoutResponse {

        public AddToWorkoutResponse(){
            this.addedContentId = -1;
        }

        public AddToWorkoutResponse(int addedContentId) {
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

