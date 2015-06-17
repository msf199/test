package whitespell.peakapi.endpoints.users;

import com.google.gson.Gson;
import whitespell.logic.EndpointInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.logging.Logging;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.StatementExecutor;
import whitespell.model.UserObject;

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
public class GetUsers implements EndpointInterface {


    private static final String GET_USERS = "SELECT `user_id`, `username`, `thumbnail` FROM `users`";

    @Override
    public void call(final RequestContext context) throws IOException {
        /**
        * Get the signups by day
        */
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
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }

}
