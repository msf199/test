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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         9/30/2015
 */
public class GrantContentAccess extends EndpointHandler {

    private static final String URL_USER_ID = "userId";

    private static final String PAYLOAD_CONTENT_ID = "contentId";

    private static final String ADD_CONTENT_ACCESS_UPDATE = "INSERT INTO `content_access`(`content_id`, `user_id`, `timestamp`) VALUES (?,?,?)";
    private static final String GET_CONTENT_ACCESS_QUERY = "SELECT `content_id` FROM `content_access` WHERE `user_id` = ?";

    /**
     * ContentIds the user already has access to
     */
    Set<Integer> accessibleContentIds = null;

    /**
     * ContentIds user will gain access to
     */
    Set<Integer> contentIdsToGrantAccessTo = null;

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        accessibleContentIds = new HashSet<>();
        contentIdsToGrantAccessTo = new HashSet<>();
        ContentAccessResponse car = new ContentAccessResponse();
        Gson g = new Gson();

        ContentHelper h = new ContentHelper();

        JsonObject j = context.getPayload().getAsJsonObject();

        final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        final int content_id = j.get(PAYLOAD_CONTENT_ID).getAsInt();
        final Timestamp now = new Timestamp(Server.getMilliTime());

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
         * Get all the contentAccess details for this user, construct list of contentIds to prevent multiple grant access attempts
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_ACCESS_QUERY);

            executor.execute(ps -> {
                ps.setInt(1, user_id);

                ResultSet results = ps.executeQuery();

                while (results.next()) {
                    accessibleContentIds.add(results.getInt("content_id"));
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Grant access to this content
         */

        ContentObject c = null;

        c = h.getContentById(context, content_id, a.getUserId());
        if (c == null) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return;
        }

        /**
         * If the user already has access to this content (and it's not a bundle), simply return true.
         */
        if (c.getContentType() != StaticRules.BUNDLE_CONTENT_TYPE &&
                accessibleContentIds.contains(c.getContentId())) {
            car.setSuccess(true);
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

        /** If the type is a bundle, we need to grant all the children of the bundle access as well **/

        if (c != null && c.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
            recursiveGrantChildrenAccess(c);
        } else if (c != null && accessibleContentIds != null && !accessibleContentIds.contains(c.getContentId())) {
            contentIdsToGrantAccessTo.add(c.getContentId());
        }


        /**
         * If no access can be granted, return
         */
        if (contentIdsToGrantAccessTo.size() == 0){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_GRANT_CONTENT_ACCESS);
            return;
        }

        /**
         * Attempt to grant access to the relevant contentIds
         */
        for (int to_insert_content_id: contentIdsToGrantAccessTo){

            try {
                StatementExecutor executor = new StatementExecutor(ADD_CONTENT_ACCESS_UPDATE);

                executor.execute(ps -> {
                    ps.setInt(1, to_insert_content_id);
                    ps.setInt(2, user_id);
                    ps.setTimestamp(3, now);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        System.out.println("content_access successfully granted for contentId " + to_insert_content_id + " and userId " + user_id);
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }

        car.setSuccess(true);

        String response = g.toJson(car);
        context.getResponse().setStatus(200);

        try{
            context.getResponse().getWriter().write(response);
        } catch (Exception e){
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public void recursiveGrantChildrenAccess(ContentObject c) {
        if (accessibleContentIds != null && !accessibleContentIds.contains(c.getContentId())) {
                contentIdsToGrantAccessTo.add(c.getContentId());
        }
        if (c.getChildren() != null) {
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

        public ContentAccessResponse() {
            this.success = false;
        }
    }
}
