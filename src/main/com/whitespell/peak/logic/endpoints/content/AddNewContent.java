package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Pim de Witte, Whitespell Inc.
 *         5/4/2015
 */
public class AddNewContent extends EndpointHandler {

    private static final String INSERT_CONTENT_QUERY = "INSERT INTO `content`(`user_id`, `content_type`, `content_url`, `content_title`, `content_description`, `timestamp`) VALUES (?,?,?,?,?,?)";


    private static final String PAYLOAD_CONTENT_TYPE_ID = "content_type";
    private static final String PAYLOAD_CONTENT_TITLE = "content_title";
    private static final String PAYLOAD_CONTENT_URL = "content_url";
    private static final String PAYLOAD_CONTENT_DESCRIPTION = "content_description";

    private static final String URL_USER_ID_KEY = "user_id";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_TYPE_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_TITLE, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_URL, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_CONTENT_DESCRIPTION, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }


    @Override
    public void safeCall(RequestObject context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID_KEY));
        final String content_type = payload.get(PAYLOAD_CONTENT_TYPE_ID).getAsString();
        final String content_url = payload.get(PAYLOAD_CONTENT_URL).getAsString();
        final String content_title = payload.get(PAYLOAD_CONTENT_TITLE).getAsString();
        final String content_description = payload.get(PAYLOAD_CONTENT_DESCRIPTION).getAsString();
        final Timestamp now = new Timestamp(new Date().getTime());
        //todo(pim) thumbnail
        //todo(pim) content_likes
        //todo(pim) last_comment
        //todo(pim) content_category


        if (content_type.length() > StaticRules.MAX_CONTENT_TYPE_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_TYPE_TOO_LONG);
            return;
        }
        if (content_description.length() > StaticRules.MAX_CONTENT_DESCRIPTION_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_DESCRIPTION_TOO_LONG);
            return;
        }

        final boolean[] success = {false};

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CONTENT_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, String.valueOf(user_id));
                    ps.setInt(2, Integer.parseInt(content_type));
                    ps.setString(3, content_url);
                    ps.setString(4, content_title);
                    ps.setString(5, content_description);
                    ps.setString(6, now.toString());

                    ps.executeUpdate();

                    success[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            if (e.getMessage().contains("FK_user_content_content_type")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_SUCH_CATEGORY);
            }
            return;
        }

        if (success[0]) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            AddContentObject object = new AddContentObject();
            object.setContentAdded(true);
            Gson g = new Gson();
            String json = g.toJson(object);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
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
