package whitespell.peakapi.endpoints.content.types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.EndpointInterface;
import whitespell.logic.RequestObject;
import whitespell.logic.logging.Logging;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.StatementExecutor;
import whitespell.model.AddContentTypeObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Josh Lipson(mrgalkon)
 * 5/4/2015
 */
public class AddContentType implements EndpointInterface {

    private static final String INSERT_CONTENT_QUERY = "INSERT INTO `content_type`(`content_type_name`) VALUES (?)";

    @Override
    public void call(RequestObject context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        /**
         * Check that the user id and content is valid.
         */
        if (payload.get("content_type_name") == null ) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final String content_type_name = payload.get("content_type_name").getAsString();

        if (content_type_name.length() > StaticRules.MAX_CONTENT_TYPE_LENGTH) {
            context.throwHttpError(StaticRules.ErrorCodes.CONTENT_TYPE_TOO_LONG);
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
                context.throwHttpError(StaticRules.ErrorCodes.NO_SUCH_CATEGORY); //todo check if this should be category or content ype
            } else if(e.getMessage().contains("Duplicate entry")) {
                context.throwHttpError(StaticRules.ErrorCodes.DUPLICATE_CONTENT_TYPE);
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
            context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

}
