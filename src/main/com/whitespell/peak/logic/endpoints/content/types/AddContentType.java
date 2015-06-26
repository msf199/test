package main.com.whitespell.peak.logic.endpoints.content.types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointInterface;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Pim de Witte, Whitespell Inc.
 *         5/4/2015
 */
public class AddContentType extends EndpointInterface {

    private static final String INSERT_CONTENT_QUERY = "INSERT INTO `content_type`(`content_type_name`) VALUES (?)";

    @Override
    public void call(RequestObject context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        /**
         * Check that the user id and content is valid.
         */
        if (payload.get("content_type_name") == null) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final String content_type_name = payload.get("content_type_name").getAsString();

        if (content_type_name.length() > StaticRules.MAX_CONTENT_TYPE_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_TYPE_TOO_LONG);
            return;
        }

        final boolean[] success = {false};

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CONTENT_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, content_type_name);

                    ps.executeUpdate();

                    success[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            if (e.getMessage().contains("FK_user_content_content_type")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_SUCH_CATEGORY); //todo check if this should be category or content ype
            } else if (e.getMessage().contains("Duplicate entry")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.DUPLICATE_CONTENT_TYPE);
            }
        }

        if (success[0]) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            AddContentTypeObject object = new AddContentTypeObject();
            object.setContentTypeAdded(true);
            Gson g = new Gson();
            String json = g.toJson(object);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class AddContentTypeObject {

        private boolean content_type_added;

        public boolean isContentTypeAdded() {
            return this.content_type_added;
        }

        public void setContentTypeAdded(boolean content_type_added) {
            this.content_type_added = content_type_added;
        }

    }

}
