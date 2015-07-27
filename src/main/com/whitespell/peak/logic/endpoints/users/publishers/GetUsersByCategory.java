package main.com.whitespell.peak.logic.endpoints.users.publishers;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.GenericAPIActions;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.Safety;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetUsersByCategory extends EndpointHandler {


    private static final String PARAMETER_CATEGORY_ID_KEY = "categories";
    private static final String PARAMETER_LIMIT_KEY = "limit";
    private static final String PARAMETER_OFFSET_KEY = "offset";

    @Override
    protected void setUserInputs() {
        queryStringInput.put(PARAMETER_LIMIT_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(PARAMETER_OFFSET_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(PARAMETER_CATEGORY_ID_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        try {


            /**
             * Construct the WHERE string based on the categories.
             */

            String[] categories_str = context.getQueryString().get("categories")[0].split(","); //todo(pim) make more safe with same system we used for JSON payloads.

            StringBuilder whereString = new StringBuilder();
            for (int i = 0; i < categories_str.length; i++) {

                if (!Safety.isInteger(categories_str[i])) {
                    continue;
                }


                if (i == 0) {
                    whereString.append("WHERE `category_id` = " + categories_str[i] + " ");
                } else {
                    whereString.append("OR `category_id` = " + categories_str[i] + " ");
                }
            }

            StatementExecutor executor = new StatementExecutor("" +
                    "SELECT DISTINCT user.user_id, category_id, user.username, user.thumbnail FROM `category_publishing` INNER JOIN user ON user.user_id=category_publishing.user_id " +
                    whereString.toString() +
                    "ORDER BY `category_id` LIMIT " + GenericAPIActions.getLimit(context.getQueryString()));
            executor.execute(ps -> {

                final ResultSet results = ps.executeQuery();
                ArrayList<CategorizedUserObject> users = new ArrayList<>();
                while (results.next()) {

                    CategorizedUserObject d = new CategorizedUserObject(results.getInt("user_id"), results.getString("username"), results.getString("thumbnail"), results.getInt("category_id"));
                    users.add(d);
                }

                // put the array list into a JSON array and write it as a response

                Gson g = new Gson();
                String response = g.toJson(users);
                context.getResponse().setStatus(200);
                try {
                    context.getResponse().getWriter().write(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }

    class CategorizedUserObject {
        int userId;
        String userName;
        String thumbnail;
        int categoryId;

        public CategorizedUserObject(int userId, String userName, String thumbnail, int categoryId) {
            this.userId = userId;
            this.userName = userName;
            this.thumbnail = thumbnail;
            this.categoryId = categoryId;
        }
    }

}
