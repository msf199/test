package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/12/2015
 */

public class UpdateEmailVerification extends EndpointHandler {

    private static final String CHECK_EMAIL_TOKEN = "SELECT `email_verified`, `email_token`, `email_expiration` FROM `user` WHERE `username` = ? LIMIT 1";
    private static final String UPDATE_EMAIL_VERIFICATION = "UPDATE `user` SET `email_verified` = ?, `email_token` = ?, `email_expiration` = ? WHERE `username` = ?";

    private static final String PAYLOAD_EMAIL_TOKEN_KEY = "emailToken";

    private static final String PAYLOAD_USERNAME_KEY = "userName";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_EMAIL_TOKEN_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    private static final String ENSURE_VERIFIED_QUERY = "SELECT `username`, `email`, `email_verified` FROM `user` WHERE `username` = ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        int emailVerification = 1;
        String username = payload.get(PAYLOAD_USERNAME_KEY).getAsString();
        String emailToken = payload.get(PAYLOAD_EMAIL_TOKEN_KEY).getAsString();

        /**
         * Ensure the email token matches the one stored in the database.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_EMAIL_TOKEN);
            final String finalUsername = username;
            executor.execute(ps -> {
                ps.setString(1, finalUsername);

                ResultSet s = ps.executeQuery();
                if(s.next()){
                    Timestamp now = new Timestamp(Server.getCalendar().getTimeInMillis());
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
            final String finalUsername = username;
            final int finalEmailVerification = emailVerification;
            final String finalEmailToken = null;
            final String finalEmailExpiration = null;
            executor.execute(ps -> {
                ps.setInt(1, finalEmailVerification);
                ps.setString(2, finalEmailToken);
                ps.setString(3, finalEmailExpiration);
                ps.setString(4, finalUsername);

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
            final String finalUsername = username;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalUsername);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        context.getResponse().setStatus(HttpStatus.OK_200);
                        UserObject uo = new UserObject();
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
