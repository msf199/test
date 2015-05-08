package whitespell.peakapi.endpoints.users.content;

import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.Safety;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Josh Lipson(mrgalkon)
 * 5/4/2015
 */
public class RequestContent implements ApiInterface {

    private static final String SELECT_FOLLOWING_IDS_QUERY = "SELECT `followed_id` FROM `following` WHERE `user_id` = ?";
    private static final String SELECT_CONTENT_FOR_ID_QUERY = "SELECT * FROM `user_content` WHERE `user_id` = ?";

    @Override
    public void call(RequestContext context) throws IOException {
        String context_user_id = context.getUrlVariables().get("user_id");

        /**
         * Check that the user id is valid.
         */
        if (!Safety.isNumeric(context_user_id)) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final int user_id = Integer.parseInt(context_user_id);
        final List<Integer> followedIds = new ArrayList<>();

        /**
         * Request the list of followed ids.
         */
        try {
            StatementExecutor executor = new StatementExecutor(SELECT_FOLLOWING_IDS_QUERY);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, String.valueOf(user_id));

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        followedIds.add(results.getInt("followed_id"));
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /**
         * Request the content of the followed ids.
         */
        for (final int followedId : followedIds) {
            try {
                StatementExecutor executor = new StatementExecutor(SELECT_CONTENT_FOR_ID_QUERY);
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {
                        ps.setString(1, String.valueOf(followedId));

                        ResultSet results = ps.executeQuery();
                        while (results.next()) {
                            //TODO display results
                        }
                    }
                });
            } catch (SQLException e) {
                System.err.println("FOLLOWED ID: " + followedId);
                e.printStackTrace();
            }
        }

        context.getResponse().setStatus(HttpStatus.OK_200);
        context.getResponse().getWriter().write("{}");
    }

}
