package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         9/30/2015
 */
public class GrantContentAccess extends EndpointHandler {

    private static final String URL_USER_ID = "userId";

    private static final String PAYLOAD_CONTENT_ID = "contentId";

    private static final String ADD_CONTENT_ACCESS_QUERY = "INSERT INTO `content_access`(`content_id`, `user_id`, `timestamp`) VALUES (?,?,?)";

    ArrayList<Integer> contentIdsToGrantAccessTo = new ArrayList<>();

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        ContentHelper h = new ContentHelper();

        JsonObject j = context.getPayload().getAsJsonObject();

        final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        final int content_id = j.get(PAYLOAD_CONTENT_ID).getAsInt();
        final Timestamp now = new Timestamp(Server.getCalendar().getTimeInMillis());

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = user_id == a.getUserId();

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Grant access to this content
         */

        ContentObject c = null;

        try {
            c = h.getContentById(content_id);
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        /** If the type is a bundle, we need to grant all the children of the bundle access as well **/

        if(c != null && c.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
            recursiveGrantChildrenAccess(c);
        } else {
            contentIdsToGrantAccessTo.add(c.getContentId());
        }

        for(int to_insert_content_id : contentIdsToGrantAccessTo) {

            try {
                StatementExecutor executor = new StatementExecutor(ADD_CONTENT_ACCESS_QUERY);

                executor.execute(ps -> {
                    ps.setInt(1, to_insert_content_id);
                    ps.setInt(2, user_id);
                    ps.setTimestamp(3, now);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        System.out.println("content_access successfully granted for contentId " + to_insert_content_id + " and userId " + user_id);
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_GRANT_CONTENT_ACCESS);
                        return;
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }

        ContentAccessResponse car = new ContentAccessResponse();
        car.setSuccess(true);

        Gson g = new Gson();
        String response = g.toJson(car);
        context.getResponse().setStatus(200);
        try {
            context.getResponse().getWriter().write(response);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public void recursiveGrantChildrenAccess(ContentObject c) {
        contentIdsToGrantAccessTo.add(c.getContentId());
        if(c.getChildren() != null) {
            c.getChildren().forEach(this::recursiveGrantChildrenAccess);
        }
    }

    public class ContentAccessResponse {

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        private boolean success;

        public ContentAccessResponse(){
            this.success = false;
        }
    }
}
