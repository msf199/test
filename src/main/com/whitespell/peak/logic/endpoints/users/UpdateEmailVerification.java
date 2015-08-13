package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/12/2015
 */

public class UpdateEmailVerification extends EndpointHandler {

    private static final String CHECK_EMAIL_TOKEN = "SELECT `email_verified`, `email_token`, `email_expiration` FROM `user` WHERE `user_id` = ? LIMIT 1";
    private static final String UPDATE_EMAIL_VERIFICATION = "UPDATE `user` SET `email_verified` = ?, email_token = ?, email_expiration = ? WHERE `user_id` = ?";

    private static final String URL_USER_ID = "userId";

    private static final String PAYLOAD_EMAIL_TOKEN_KEY = "emailToken";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_EMAIL_TOKEN_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    private static final String ENSURE_VERIFIED_QUERY = "SELECT `username`, `email`, `email_verified` FROM `user` WHERE `user_id` = ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        int userId = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        int emailVerification = 1;
        String emailToken = payload.get(PAYLOAD_EMAIL_TOKEN_KEY).getAsString();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (userId == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Ensure the email token matches the one stored in the database.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_EMAIL_TOKEN);
            final int finalUserId = userId;
            executor.execute(ps -> {
                ps.setInt(1, finalUserId);

                ResultSet s = ps.executeQuery();
                if(s.next()){
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    if(s.getInt("email_verified") == 1){
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_ALREADY_VERIFIED);
                        return;
                    }
                    if(s.getString("email_token") == null || s.getTimestamp("email_expiration") == null){
                        /**
                         * Need to resend verification email.
                         */
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_VERIFICATION_NOT_SENT);
                        return;
                    }
                    if(s.getTimestamp("email_expiration").before(now)){
                        /**
                         * Need to resend verification email.
                         */
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_TOKEN_EXPIRED);
                        return;
                    }
                    if(!s.getString("email_token").equals(emailToken)){
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_TOKEN_INVALID);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Update the user's email verification status in the database, reset email expiration and token to null.
         */

        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_EMAIL_VERIFICATION);
            final int finalUserId = userId;
            final int finalEmailVerification = emailVerification;
            final String finalEmailToken = null;
            final String finalEmailExpiration = null;
            executor.execute(ps -> {
                ps.setInt(1, finalEmailVerification);
                ps.setString(2, finalEmailToken);
                ps.setString(3, finalEmailExpiration);
                ps.setInt(4, finalUserId);

                ps.executeUpdate();
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Ensure user has verified their email and return the response.
         */

        try {
            StatementExecutor executor = new StatementExecutor(ENSURE_VERIFIED_QUERY);
            final int finalUserId = userId;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setInt(1, finalUserId);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        context.getResponse().setStatus(HttpStatus.OK_200);
                        UserObject uo = new UserObject();
                        uo.setUserId(finalUserId);
                        uo.setUserName(s.getString("username"));
                        uo.setEmail(s.getString("email"));
                        uo.setEmailVerified(s.getInt("email_verified"));
                        Gson g = new Gson();
                        String json = g.toJson(uo);
                        try {
                            context.getResponse().getWriter().write(json);
                        } catch (IOException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                            return;
                        }
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

}
