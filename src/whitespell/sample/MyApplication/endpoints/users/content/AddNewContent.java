package whitespell.sample.MyApplication.endpoints.users.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.Safety;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.StatementExecutor;
import whitespell.model.AddContentObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Josh Lipson(mrgalkon)
 * 5/4/2015
 */
public class AddNewContent implements ApiInterface {

    private static final String INSERT_CONTENT_QUERY = "INSERT INTO `user_content`(`user_id`, `content_type`, `content_description`, `timestamp`) VALUES (?,?,?,?)";

    public void call(RequestContext context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        String context_user_id = context.getUrlVariables().get("user_id");

        /**
         * Check that the user id and content is valid.
         */
        if (!Safety.isNumeric(context_user_id) || payload.get("content_type") == null || payload.get("content_description") == null) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final int user_id = Integer.parseInt(context_user_id);
        final String content_type = payload.get("content_type").getAsString();
        final String content_description = payload.get("content_description").getAsString();
        final Timestamp now = new Timestamp(new Date().getTime());

        /**
         * Check that the user id is valid (>= 0 && <= Integer.MAX_VALUE), and content type and description are of-length.
         */
        if (!Safety.isValidUserId(user_id)) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }
        if (content_type.length() > StaticRules.MAX_CONTENT_TYPE_LENGTH) {
            context.throwHttpError(StaticRules.ErrorCodes.CONTENT_TYPE_TOO_LONG);
            return;
        }
        if (content_description.length() > StaticRules.MAX_CONTENT_DESCRIPTION_LENGTH) {
            context.throwHttpError(StaticRules.ErrorCodes.CONTENT_DESCRIPTION_TOO_LONG);
            return;
        }

        final boolean[] success = {false};

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CONTENT_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, String.valueOf(user_id));
                    statement.setString(2, content_type);
                    statement.setString(3, content_description);
                    statement.setString(4, now.toString());

                    statement.executeUpdate();

                    success[0] = true;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (success[0]) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            AddContentObject object = new AddContentObject();
            object.setContentAdded(true);
            Gson g = new Gson();
            String json = g.toJson(object);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

}
