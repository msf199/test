package main.com.whitespell.peak.logic.endpoints.users.publishers;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointInterface;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.Safety;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetUsersByCategory implements EndpointInterface {


    private static final String GET_PUBLISHERS_BY_CATEGORY = "" +
            "SELECT DISTINCT user.user_id, user.username, user.thumbnail, category_id FROM `category_publishing` INNER JOIN user ON user.user_id=category_publishing.user_id WHERE `category_id` = ? ORDER BY `category_id` GROUP BY `user_id`,`category_id` LIMIT 30";

    @Override
    public void call(final RequestObject context) throws IOException {
        try {

            if (context.getParameterMap().get("categories") == null) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
                return;
            }

            /**
             * Object limits
             */

            int limit = StaticRules.MAX_PUBLISHING_USER_SELECT;

            if (context.getParameterMap().get("limit") != null) {
                String limitString = context.getParameterMap().get("limit").toString();
                if (Safety.isNumeric(limitString)) {
                    int limitProposed = Integer.parseInt(limitString);
                    if (limitProposed > StaticRules.MAX_PUBLISHING_USER_SELECT) {
                        limit = StaticRules.MAX_PUBLISHING_USER_SELECT;
                    } else {
                        limit = limitProposed;
                    }
                }
            }


            /**
             * Construct the WHERE string based on the categories.
             */

            String[] categories_str = context.getParameterMap().get("categories")[0].split(",");

            StringBuilder whereString = new StringBuilder();
            for (int i = 0; i < categories_str.length; i++) {

                if (!Safety.isNumeric(categories_str[i])) {
                    continue;
                }


                if (i == 0) {
                    whereString.append("WHERE `category_id` = " + categories_str[i] + " ");
                } else {
                    whereString.append("OR `category_id` = " + categories_str[i] + " ");
                }
            }

            StatementExecutor executor = new StatementExecutor("" +
                    "SELECT user.user_id, user.username, user.thumbnail, category_id FROM `category_publishing` INNER JOIN user ON user.user_id=category_publishing.user_id " +
                    whereString.toString() +
                    "ORDER BY `category_id` LIMIT " + limit + "");
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

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
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }

    class CategorizedUserObject {
        int user_id;
        String username;
        String thumbnail;
        int category_id;

        public CategorizedUserObject(int user_id, String username, String thumbnail, int category_id) {
            this.user_id = user_id;
            this.username = username;
            this.thumbnail = thumbnail;
            this.category_id = category_id;
        }
    }

}
