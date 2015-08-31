package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EmailSend;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/31/2015
 */

public class ResendEmailVerification extends EndpointHandler {

    private static final String CHECK_USERNAME_QUERY = "SELECT `user_id`,`username` FROM `user` WHERE `email` = ? LIMIT 1";

    private static final String PAYLOAD_EMAIL_KEY = "email";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_EMAIL_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        String email = payload.get(PAYLOAD_EMAIL_KEY).getAsString();
        String[] username = {"new user"};
        int[] userId = {0};

        /**
         * Get the username for the welcome email
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_QUERY);
            final String finalEmail = email;
            executor.execute(ps -> {
                ps.setString(1, finalEmail);

                ResultSet s = ps.executeQuery();
                if(s.next()){
                    username[0] = s.getString("username");
                    userId[0] = s.getInt("user_id");
                }
                else{
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Send the email verification
         */

        EmailSend.tokenResponseObject sent = EmailSend.updateDBandSendWelcomeEmail(username[0], email);
        EmailVerificationSentStatus status = new EmailVerificationSentStatus();
        if(sent.getEmailToken() != null){
            status.setSent(true);
        }else{
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_VERIFICATION_NOT_SENT);
        }

        /**
         * Output the response
         */

        Gson g = new Gson();
        String json = g.toJson(status);

        try {
            context.getResponse().getWriter().write(json);
        } catch (IOException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class EmailVerificationSentStatus {

        public void setSent(boolean sent) {
            this.success = sent;
        }

        public boolean isSent() {
            return success;
        }

        boolean success;

        public EmailVerificationSentStatus(){
            this.success = false;
        }
    }
}
