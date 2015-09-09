package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         8/19/2015
 */
public class AddNewContentCuration extends EndpointHandler{

    private static final String INSERT_CONTENT_QUERY = "INSERT INTO `content_curation`(`user_id`, `category_id`, `content_type`, `content_url`, `content_title`, `content_description`, `thumbnail_url`, `timestamp`) VALUES (?,?,?,?,?,?,?,?)";
    private static final String UPDATE_USER_AS_PUBLISHER_QUERY = "UPDATE `user` SET `publisher` = ? WHERE `user_id` = ?";

    private static final String UPDATE_CURATION_ACCEPTED_QUERY = "UPDATE `content` SET `curation_accepted` = ? WHERE `content_url` = ?";

    private static final String PAYLOAD_CATEGORY_ID = "categoryId";
    private static final String PAYLOAD_CONTENT_TYPE_ID = "contentType";
    private static final String PAYLOAD_CONTENT_TITLE = "contentTitle";
    private static final String PAYLOAD_CONTENT_URL = "contentUrl";
    private static final String PAYLOAD_CONTENT_DESCRIPTION = "contentDescription";
    private static final String PAYLOAD_CONTENT_THUMBNAIL = "thumbnailUrl";
    private static final String PAYLOAD_CONTENT_ACCEPTED = "accepted";

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
        payloadInput.put(PAYLOAD_CONTENT_ACCEPTED, StaticRules.InputTypes.REG_INT_REQUIRED_ZERO);
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
        final int accepted = payload.get(PAYLOAD_CONTENT_ACCEPTED).getAsInt();


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
                if (rows <= 0) {
                    System.out.println("Failed to insert content");
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

        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_USER_AS_PUBLISHER_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, 1);
                ps.setInt(2, user_id);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                } else {
                    System.out.println("Failed to update user as publisher");
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        Gson g = new Gson();
        try {
            /**
             * authenticate as admin to post publishing
             */

            HttpResponse<String> stringResponse = null;
            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"userName\":\"coryqq\",\n" +
                            "\"password\" : \"qqqqqq\",\n" +
                            "\"deviceName\":\"coryadmin\",\n" +
                            "\"deviceUUID\":\"internal" + System.currentTimeMillis() + "\",\n" +
                            "\"deviceType\":-1\n" +
                            "}")
                    .asString();
            AuthenticationObject ao = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
            int ADMIN_UID = ao.getUserId();
            String ADMIN_KEY = ao.getKey();

            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + user_id + "/publishing")
                    .header("accept", "application/json")
                    .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                    .body("{\n" +
                            "\"categoryId\": \"" + category_id + "\",\n" +
                            "\"action\": \"publish\"\n" +
                            "}")
                    .asString();
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_CURATION_ACCEPTED_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, accepted);
                ps.setString(2, content_url);

                int rows = ps.executeUpdate();
                if (rows <= 0) {
                    System.out.println("Failed to update curation acceptance status");
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        context.getResponse().setStatus(HttpStatus.OK_200);
        AddContentObject object = new AddContentObject();
        object.setContentAdded(true);
        String json = g.toJson(object);
        context.getResponse().getWriter().write(json);

    }

    public class AddContentObject {

        private boolean content_added;

        public boolean isContentAdded() {
            return content_added;
        }

        public void setContentAdded(boolean content_added) {
            this.content_added = content_added;
        }

    }
}
