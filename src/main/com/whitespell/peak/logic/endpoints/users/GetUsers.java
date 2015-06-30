package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
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
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetUsers extends EndpointHandler {

    private static final String GET_USERS = "SELECT `user_id`, `username`, `thumbnail` FROM `user`";


	private static final String URL_USER_ID = "user_id";

	private static final String USERNAME_KEY = "username";
	private static final String EMAIL_KEY = "email";
	private static final String THUMBNAIL_KEY = "thumbnail";
	private static final String COVER_PHOTO_KEY = "cover_photo";
	private static final String SLOGAN_KEY = "slogan";

	@Override
	protected void setUserInputs() {
		urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
	}

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

                        UserObject d = new UserObject(results.getInt(URL_USER_ID), results.getString(USERNAME_KEY),
								results.getString(EMAIL_KEY), results.getString(THUMBNAIL_KEY), results.getString(SLOGAN_KEY),
								results.getString(COVER_PHOTO_KEY));

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
}
