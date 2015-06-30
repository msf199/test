package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
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

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class GetUser extends EndpointHandler {


    private static final String GET_USER = "SELECT `user_id`, `username`, `email`, `thumbnail`, `cover_photo`, `slogan` FROM `user` WHERE `user_id` = ?";

    private static final String URL_USER_ID = "user_id";

    private static final String PAYLOAD_USERNAME_KEY = "username";
    private static final String PAYLOAD_EMAIL_KEY = "email";
    private static final String PAYLOAD_THUMBNAIL_KEY = "thumbnail";
    private static final String PAYLOAD_COVER_PHOTO_KEY = "cover_photo";
    private static final String PAYLOAD_SLOGAN_KEY = "slogan";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
		payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
		payloadInput.put(PAYLOAD_EMAIL_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
		payloadInput.put(PAYLOAD_THUMBNAIL_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
		payloadInput.put(PAYLOAD_COVER_PHOTO_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
		payloadInput.put(PAYLOAD_SLOGAN_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));

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

                        user = new UserObject(results.getInt(URL_USER_ID), results.getString(PAYLOAD_USERNAME_KEY),
                                results.getString(PAYLOAD_EMAIL_KEY), results.getString(PAYLOAD_THUMBNAIL_KEY), results.getString(PAYLOAD_SLOGAN_KEY),
                                results.getString(PAYLOAD_COVER_PHOTO_KEY));
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
                        Logging.log("High", e);
						return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
			return;
        }

    }
}
