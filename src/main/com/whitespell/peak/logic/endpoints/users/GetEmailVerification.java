package main.com.whitespell.peak.logic.endpoints.users;


import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/13/2015
 */
public class GetEmailVerification extends EndpointHandler {

    private static final String CHECK_EMAIL_VERIFICATION = "SELECT `email_verified`, `email_expiration` FROM `user` WHERE `user_id` = ? LIMIT 1";

    private static final String URL_USER_ID = "userId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int userId = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));

        /**
         * Check user's email verification status and return the response.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_EMAIL_VERIFICATION);
            final int finalUserId = userId;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setInt(1, finalUserId);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        context.getResponse().setStatus(HttpStatus.OK_200);
                        GetEmailVerificationResponse vr = new GetEmailVerificationResponse();
                        vr.setUserId(finalUserId);
                        vr.setEmailVerified(s.getInt("email_verified"));
                        vr.setEmailExpiration(s.getTimestamp("email_expiration"));
                        Gson g = new Gson();
                        String json = g.toJson(vr);
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

    public class GetEmailVerificationResponse {

        private int userId;
        private int emailVerified;
        private Timestamp emailExpiration;

        public GetEmailVerificationResponse(){
            this.userId = -1;
            this.emailVerified = -1;
            this.emailExpiration = null;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getEmailVerified() {
            return emailVerified;
        }

        public void setEmailVerified(int emailVerified) {
            this.emailVerified = emailVerified;
        }

        public Timestamp getEmailExpiration() {
            return emailExpiration;
        }

        public void setEmailExpiration(Timestamp emailExpiration) {
            this.emailExpiration = emailExpiration;
        }
    }
}

