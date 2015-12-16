package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.security.PasswordHash;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/18/2015
 */

public class ResetPassword extends EndpointHandler {

    private static final String CHECK_RESET_TOKEN = "SELECT `reset_token` FROM `user` WHERE `username` = ? LIMIT 1";

    private static final String UPDATE_PASSWORD = "UPDATE `user` SET `password` = ?, `reset_token` = NULL WHERE `username` = ? LIMIT 1";

    private static final String PAYLOAD_USERNAME_KEY = "userName";
    private static final String PAYLOAD_NEW_PASSWORD_KEY = "newPassword";
    private static final String PAYLOAD_RESET_TOKEN_KEY = "resetToken";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_NEW_PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_RESET_TOKEN_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    private static final String ENSURE_RESET_QUERY = "SELECT `username`, `password`, `email`, `reset_token` FROM `user` WHERE `username` = ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        String username = payload.get(PAYLOAD_USERNAME_KEY).getAsString();
        String newPass = payload.get(PAYLOAD_NEW_PASSWORD_KEY).getAsString();
        String resetToken = payload.get(PAYLOAD_RESET_TOKEN_KEY).getAsString();
        String passHash;

        /**
         * Check if newPass restrictions are violated
         */

        if (newPass.length() > StaticRules.MAX_PASSWORD_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_LONG);
            return;
        } else if (newPass.length() < StaticRules.MIN_PASSWORD_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_SHORT);
            return;
        }

        /**
         * Ensure the reset token matches the one stored in the database.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_RESET_TOKEN);
            final String finalUsername = username;
            executor.execute(ps -> {
                ps.setString(1, finalUsername);

                ResultSet s = ps.executeQuery();
                if(s.next()){
                    if(s.getString("reset_token") != null) {
                        if (!resetToken.equals(s.getString("reset_token"))) {
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_TOKEN_INVALID);
                            return;
                        }
                    }else{
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
         * Replace the current password and set reset token to null
         */

        try {
            passHash = PasswordHash.createHash(newPass);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }


        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_PASSWORD);

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, passHash);
                    ps.setString(2, username);
                    int rows = ps.executeUpdate();
                    if (rows <= 0) {
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

        /**
         * Ensure the reset token was nullified so that user can now log in with new pass.
         */

        try {
            StatementExecutor executor = new StatementExecutor(ENSURE_RESET_QUERY);
            final String finalUsername = username;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, finalUsername);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        if(s.getString("password").equals(passHash)){
                            context.getResponse().setStatus(HttpStatus.OK_200);
                            resetSuccessObject rs = new resetSuccessObject();
                            rs.setSuccess(true);

                            /**
                             * parse the email and return a secure email with asterisks (e.g. r****k@gmail.com)
                             */
                            String email = s.getString("email");
                            String newString = "";
                            if(email.contains("@")) {
                                String[] split = email.split("@");
                                newString = split[0].substring(0,1) + "******" +
                                        split[0].substring(split[0].length() - 1) + "@" + split[1];
                                System.out.println("safeEmail: " +newString);
                            }

                            rs.setEmail(newString);
                            Gson g = new Gson();
                            String json = g.toJson(rs);
                            try {
                                context.getResponse().getWriter().write(json);
                            } catch (IOException e) {
                                Logging.log("High", e);
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                                return;
                            }
                        }else{
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.RESET_FAILED);
                            return;
                        }
                    }else{
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
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
        String email;

        resetSuccessObject(){
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

    }
}

