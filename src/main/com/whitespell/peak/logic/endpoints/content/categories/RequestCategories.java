package main.com.whitespell.peak.logic.endpoints.content.categories;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.CategoryObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class RequestCategories extends EndpointHandler {


    private static final String GET_CATEGORIES = "SELECT * FROM `category`";

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        try {
            StatementExecutor executor = new StatementExecutor(GET_CATEGORIES);
            executor.execute(ps -> {

                final ResultSet results = ps.executeQuery();
                ArrayList<CategoryObject> categoryObjects = new ArrayList<>();
                while (results.next()) {

                    CategoryObject d = new CategoryObject(results.getInt("category_id"), results.getString("category_name"), results.getString("category_thumbnail"), results.getInt("category_followers"), results.getInt("category_publishers"));
                    categoryObjects.add(d);
                }

                // put the array list into a JSON array and write it as a response
                Gson g = new Gson();
                String response = g.toJson(categoryObjects);
                context.getResponse().setStatus(200);
                try {
                    context.getResponse().getWriter().write(response);
                } catch (Exception e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    @Override
    protected void setUserInputs() {

    }
}
