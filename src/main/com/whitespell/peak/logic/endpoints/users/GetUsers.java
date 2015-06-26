package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;

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
public class GetUsers extends EndpointHandler {


    private static final String GET_USERS = "SELECT `user_id`, `username`, `thumbnail` FROM `user`";

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        try {
            StatementExecutor executor = new StatementExecutor(GET_USERS);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    final ResultSet results = ps.executeQuery();
                    ArrayList<UserObject> users = new ArrayList<>();
                    while (results.next()) {

                        UserObject d = new UserObject(results.getInt("user_id"), results.getString("username"), "hidden", results.getString("thumbnail"));

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

    @Override
    protected void setUserInputs() {

    }

}
