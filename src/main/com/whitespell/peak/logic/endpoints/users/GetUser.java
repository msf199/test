package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointInterface;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.Safety;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetUser extends EndpointInterface {


    private static final String GET_USER = "SELECT `user_id`, `username`, `thumbnail` FROM `user` WHERE `user_id` = ?";

    @Override
    public void call(final RequestObject context) throws IOException {

        String user_id_str = context.getUrlVariables().get("user_id");
        int user_id = -1;

        if(user_id_str == null) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        if(Safety.isInteger(user_id_str)) {
            user_id = Integer.parseInt(user_id_str);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERID_NOT_NUMERIC);
        }

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(GET_USER);
            final int finalUser_id = user_id;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    UserObject user = null;

                    ps.setInt(1, finalUser_id);

                    final ResultSet results = ps.executeQuery();

                    if (results.next()) {

                        user = new UserObject(results.getInt("user_id"), results.getString("username"), "hidden", results.getString("thumbnail"));
                    } else {
                        context.throwHttpError("GetUser", StaticRules.ErrorCodes.USER_NOT_FOUND);
                        return;
                    }

                    Gson g = new Gson();
                    String response = g.toJson(user);
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

}
