package whitespell.peakapi.endpoints.content.categories;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.EndpointInterface;
import whitespell.logic.RequestContext;
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
public class AddCategory implements EndpointInterface {

    private static final String INSERT_CATEGORY_QUERY = "INSERT INTO `categories`(`category_name`, `category_thumbnail`, `category_followers`, `category_publishers`) VALUES (?,?,?,?)";

    @Override
    public void call(RequestContext context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        /**
         * Check that the user id and content is valid.
         */
        if (payload.get("category_name") == null ) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final String content_type_name = payload.get("category_name").getAsString();

        if (content_type_name.length() > StaticRules.MAX_CONTENT_TYPE_LENGTH) {
            context.throwHttpError(StaticRules.ErrorCodes.CONTENT_TYPE_TOO_LONG);
            return;
        }

        final boolean[] success = {false};

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CATEGORY_QUERY);
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
                context.throwHttpError(StaticRules.ErrorCodes.NO_SUCH_CATEGORY);
            } else if(e.getMessage().contains("Duplicate entry")) {
                context.throwHttpError(StaticRules.ErrorCodes.DUPLICATE_CATEGORY);
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
