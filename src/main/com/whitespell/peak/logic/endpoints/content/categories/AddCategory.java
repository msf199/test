package main.com.whitespell.peak.logic.endpoints.content.categories;

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
public class AddCategory implements EndpointInterface {

    private static final String INSERT_CATEGORY_QUERY = "INSERT INTO `category`(`category_name`, `category_thumbnail`) VALUES (?,?)";

    @Override
    public void call(RequestObject context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        System.out.println(payload.toString());
        /**
         * Check that the user id and content is valid.
         */
        if (payload.get("category_name") == null || payload.get("category_thumbnail") == null) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final String category_name = payload.get("category_name").getAsString();
        final String category_thumbnail = payload.get("category_thumbnail").getAsString();

        if (category_name.length() > StaticRules.MAX_CATEGORY_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CATEGORY_TOO_LONG);
            return;
        }
        if (category_thumbnail.length() > StaticRules.MAX_THUMBNAIL_URL_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.THUMBNAIL_URL_TOO_LONG);
            return;
        }

        final boolean[] success = {false};

        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CATEGORY_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, category_name);
                    ps.setString(2, category_thumbnail);

                    ps.executeUpdate();

                    success[0] = true;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            if (e.getMessage().contains("FK_user_content_content_type")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_SUCH_CATEGORY);
            } else if (e.getMessage().contains("Duplicate entry")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.DUPLICATE_CATEGORY);
            }
        }

        if (success[0]) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            AddCategoryObject object = new AddCategoryObject();
            object.setCategoryAdded(true);
            Gson g = new Gson();
            String json = g.toJson(object);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class AddCategoryObject {

        private boolean category_added;

        public boolean isCategoryAdded() {
            return this.category_added;
        }

        public void setCategoryAdded(boolean category_added) {
            this.category_added = category_added;
        }

    }


}
