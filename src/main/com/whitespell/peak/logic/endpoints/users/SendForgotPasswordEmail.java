package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.util.ArrayList;

import static main.com.whitespell.peak.logic.EmailSend.updateDBandSendResetEmail;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/18/2015
 */
public class SendForgotPasswordEmail extends EndpointHandler {

    private static final String PAYLOAD_USERNAME_KEY = "userName";

    private static final String RETRIEVE_USERNAME = "SELECT `username`, `email` FROM `user` WHERE `email` = ? LIMIT 1";
    private static final String RETRIEVE_EMAIL = "SELECT `email` FROM `user` WHERE `username` = ? LIMIT 1";

    private static final String ENSURE_CORRECT_USER = "SELECT `user_id` FROM `user` WHERE `username` = ? LIMIT 1";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    private static final String ENSURE_VALID_TOKEN_QUERY = "SELECT `reset_token` FROM `user` WHERE `username` = ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        String payloadUsername = payload.get(PAYLOAD_USERNAME_KEY).getAsString();
        String username;
        String email;
        ArrayList<String> temp = new ArrayList<>();
        ArrayList<String> temp1 = new ArrayList<>();

        /**
         * Handle username is the user's email
         */
        if(payloadUsername.contains("@") && payloadUsername.contains(".")){
            try {
                StatementExecutor executor = new StatementExecutor(RETRIEVE_USERNAME);

                executor.execute(ps -> {
                    ps.setString(1, payloadUsername);
                    final ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        temp.add(s.getString(PAYLOAD_USERNAME_KEY));
                        temp1.add(s.getString("email"));
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
            if(temp.size() > 0){
                username = temp.get(0);
                email = payloadUsername;
            }else{
                // if not verified, throw error
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INVALID_USERNAME_OR_PASS);
                return;
            }
        }else{
            /**
             * Get the user's email if they only provided the username
             */
            username = payloadUsername;
            try {
                StatementExecutor executor = new StatementExecutor(RETRIEVE_EMAIL);

                executor.execute(ps -> {
                    ps.setString(1, username);
                    final ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        temp1.add(s.getString("email"));
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
            email = temp1.get(0);
        }

        /**
         * Send Forgot Password? email and update DB with reset_token.
         */
        try {
            StatementExecutor executor = new StatementExecutor(ENSURE_CORRECT_USER);
            final String finalUsername = username;
            executor.execute(ps -> {
                ps.setString(1, finalUsername);

                ResultSet s = ps.executeQuery();
                if(s.next()){
                    updateDBandSendResetEmail(username, email);
                }else{
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Ensure the token was updated in the database and return the response.
         */

        try {
            StatementExecutor executor = new StatementExecutor(ENSURE_VALID_TOKEN_QUERY);
            final String finalUsername = username;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalUsername);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        context.getResponse().setStatus(HttpStatus.OK_200);

                        resetSuccessObject rs = new resetSuccessObject();
                        rs.setSuccess(true);

                        Gson g = new Gson();
                        String json = g.toJson(rs);
                        try {
                            context.getResponse().getWriter().write(json);
                        } catch (IOException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                            return;
                        }
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.RESET_FAILED);
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

    public static class resetSuccessObject {

        boolean success;

        resetSuccessObject(){
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

    }
}
