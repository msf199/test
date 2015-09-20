package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import javapns.Push;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EmailSend;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.impl.ContentUploadedNotification;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Pim de Witte & Cory McAn(cmcan), Whitespell Inc.
 *         5/4/2015
 */
public class AddNewContent extends EndpointHandler {

    private static final String INSERT_CONTENT_QUERY = "INSERT INTO `content`(`user_id`, `category_id`, `content_type`, `content_url`, `content_title`, `content_description`, `thumbnail_url`, `timestamp`) VALUES (?,?,?,?,?,?,?,?)";
    private static final String UPDATE_USER_AS_PUBLISHER_QUERY = "UPDATE `user` SET `publisher` = ? WHERE `user_id` = ?";
    private static final String GET_CONTENT_ID_QUERY = "SELECT `content_id` FROM `content` WHERE `content_url` = ? AND `timestamp` = ?";

    private static final String DELETE_FROM_CURATION = "DELETE FROM `content_curation` WHERE `content_url` = ?";

    private static final String PAYLOAD_CATEGORY_ID = "categoryId";
    private static final String PAYLOAD_CONTENT_TYPE_ID = "contentType";
    private static final String PAYLOAD_CONTENT_TITLE = "contentTitle";
    private static final String PAYLOAD_CONTENT_URL = "contentUrl";
    private static final String PAYLOAD_CONTENT_DESCRIPTION = "contentDescription";
    private static final String PAYLOAD_CONTENT_THUMBNAIL = "thumbnailUrl";

    private static final String URL_USER_ID_KEY = "userId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CATEGORY_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_TYPE_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_TITLE, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_URL, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_DESCRIPTION, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_THUMBNAIL, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID_KEY));
        final int category_id = payload.get(PAYLOAD_CATEGORY_ID).getAsInt();
        final String content_type = payload.get(PAYLOAD_CONTENT_TYPE_ID).getAsString();
        final String content_url = payload.get(PAYLOAD_CONTENT_URL).getAsString();
        final String content_title = payload.get(PAYLOAD_CONTENT_TITLE).getAsString();
        final String content_description = payload.get(PAYLOAD_CONTENT_DESCRIPTION).getAsString();
        final String thumbnail_url = payload.get(PAYLOAD_CONTENT_THUMBNAIL).getAsString();
        final Timestamp now = new Timestamp(new Date().getTime());

        int[] contentId = {0};
        int ADMIN_UID = -1;
        String ADMIN_KEY = StaticRules.MASTER_KEY;
        //todo(pim) last_comment

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (a.getUserId() == user_id);

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Insert the content into the database
         */

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CONTENT_QUERY);
            executor.execute(ps -> {
                ps.setString(1, String.valueOf(user_id));
                ps.setInt(2, category_id);
                ps.setInt(3, Integer.parseInt(content_type));
                ps.setString(4, content_url);
                ps.setString(5, content_title);
                ps.setString(6, content_description);
                ps.setString(7, thumbnail_url);
                ps.setString(8, now.toString());

                int rows = ps.executeUpdate();
                if (rows <= 0){
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            if (e.getMessage().contains("FK_user_content_content_type")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_SUCH_CATEGORY);
            }
            return;
        }

        /**
         * Get the contentId so that the content can be added to a bundle
         */

        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_ID_QUERY);
            executor.execute(ps -> {
                ps.setString(1, content_url);
                ps.setString(2, now.toString());

                ResultSet r = ps.executeQuery();
                if (r.next()){
                    contentId[0] = r.getInt("content_id");
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Update user as publisher in database
         */

        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_USER_AS_PUBLISHER_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, 1);
                ps.setInt(2, user_id);

                int rows = ps.executeUpdate();
                if (rows <= 0) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_PUBLISHING_CATEGORY);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        Gson g = new Gson();
        try{
            /**
             * authenticate as admin to get user followers
             */

            HttpResponse<String> stringResponse = null;

            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + user_id + "/publishing")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"categoryId\": \"" + category_id + "\",\n" +
                            "\"action\": \"publish\"\n" +
                            "}")
                    .asString();

            if(!(stringResponse.getBody().contains("published") || stringResponse.getBody().contains("already publishing"))){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_PUBLISHING_CATEGORY);
                return;
            }
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }



        Server.NotificationService.offerNotification(new ContentUploadedNotification(user_id, new ContentObject(
                category_id,
                user_id,
                contentId[0],
                Integer.parseInt(content_type),
                content_title,
                content_url,
                content_description,
                thumbnail_url


        )));


        context.getResponse().setStatus(HttpStatus.OK_200);
        AddContentObject object = new AddContentObject();
        object.setContentId(contentId[0]);
        object.setContentAdded(true);
        String json = g.toJson(object);
        context.getResponse().getWriter().write(json);
    }

    public class AddContentObject {

        private boolean contentAdded;
        private int contentId;

        public int getContentId() {
            return contentId;
        }

        public void setContentId(int contentId) {
            this.contentId = contentId;
        }

        public boolean isContentAdded() {
            return contentAdded;
        }

        public void setContentAdded(boolean contentAdded) {
            this.contentAdded = contentAdded;
        }

    }
}
