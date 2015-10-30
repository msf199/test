package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;
import main.com.whitespell.peak.security.PasswordHash;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         7/07/15
 *         whitespell.model
 */
public class UpdateSettings extends EndpointHandler {
    private static final String URL_USER_ID = "userId";

    private static final String PAYLOAD_EMAIL_KEY = "email";

    private static final String PAYLOAD_CURRENT_PASSWORD_KEY = "password";
    private static final String PAYLOAD_NEW_PASSWORD_KEY = "newPassword";
    private static final String PAYLOAD_NEW_PUBLISHER_VALUE = "publisher";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_EMAIL_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_CURRENT_PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_NEW_PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_NEW_PUBLISHER_VALUE, StaticRules.InputTypes.REG_INT_OPTIONAL_ZERO);
    }

    private static final String CHECK_EMAIL_TAKEN_QUERY = "SELECT `user_id`, `email` FROM `user` WHERE `email` = ? AND `user_id` != ? LIMIT 1";
    private static final String RETRIEVE_PASSWORD = "SELECT `user_id`,`password` FROM `user` WHERE `user_id` = ? LIMIT 1";

    private static final String UPDATE_AUTHENTICATION = "INSERT INTO `authentication`(`user_id`, `key`) " +
            "VALUES (?,?)";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();
        String temp = "", temp1 = "";
        int tempPub = -1;
        final ArrayList<String> updateKeys = new ArrayList<>();
        final ArrayList<String> updateValues = new ArrayList<>();

        final String current_pass = j.get(PAYLOAD_CURRENT_PASSWORD_KEY).getAsString();
        if (j.get(PAYLOAD_EMAIL_KEY) != null) {
            temp = j.get(PAYLOAD_EMAIL_KEY).getAsString();
            updateKeys.add(PAYLOAD_EMAIL_KEY);
            updateValues.add(temp);
        }
        if (j.get(PAYLOAD_NEW_PASSWORD_KEY) != null) {
            temp1 = j.get(PAYLOAD_NEW_PASSWORD_KEY).getAsString();
            updateKeys.add(PAYLOAD_CURRENT_PASSWORD_KEY);
            updateValues.add(temp1);
        }
        if(j.get(PAYLOAD_NEW_PUBLISHER_VALUE) != null){
            tempPub = j.get(PAYLOAD_NEW_PUBLISHER_VALUE).getAsInt();
        }
        final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        final String email = temp;
        final String new_pass = temp1;
        final int publisher = tempPub;
        String passHash = "";

        /**
         * Ensure that the user is authenticated properly
         */
        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated() || user_id != a.getUserId()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * 401 Unauthorized: Check if email exists already
         */
        if (updateKeys.contains(PAYLOAD_EMAIL_KEY)) {
            try {
                StatementExecutor executor = new StatementExecutor(CHECK_EMAIL_TAKEN_QUERY);
                final int finalUser_id = user_id;
                final String finalEmail = email;
                final boolean[] returnCall = {false};
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {
                        ps.setString(1, finalEmail);
                        ps.setInt(2, finalUser_id);
                        ResultSet s = ps.executeQuery();
                        if (s.next()) {
                            if (s.getString(PAYLOAD_EMAIL_KEY).equalsIgnoreCase(finalEmail)) {
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_TAKEN);
                            }
                            returnCall[0] = true;
                            return;
                        }
                    }
                });
                /**
                 * This check is put in place if the request should be closed and no more logic should be executed after the query result is received. Because it is a nested function, we need to check with a finalized array
                 * whether we should return, and do so if necessary.
                 */
                if (returnCall[0]) {
                    return;
                }
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }

        /**
         * Check if new_pass restrictions are violated
         */
        if(updateKeys.contains(PAYLOAD_NEW_PASSWORD_KEY)) {
            if (new_pass.length() > StaticRules.MAX_PASSWORD_LENGTH) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_LONG);
                return;
            } else if (new_pass.length() < StaticRules.MIN_PASSWORD_LENGTH) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_SHORT);
                return;
            }
            if(new_pass.equals(current_pass)){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INVALID_USERNAME_OR_PASS);
                return;
            }
        }

        /**
         * Validate the current password and create a new session key for this user_id
         */
        try {
            StatementExecutor executor = new StatementExecutor(RETRIEVE_PASSWORD);

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setInt(1, user_id);
                    final ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        try {
                            // with the result set, check if current_pass is verified
                            boolean isVerified = main.com.whitespell.peak.security.PasswordHash.validatePassword(current_pass, s.getString(PAYLOAD_CURRENT_PASSWORD_KEY));

                            if (!isVerified) {
                                // if not verified, throw error
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INVALID_USERNAME_OR_PASS);
                                return;
                            }
                        } catch (NoSuchAlgorithmException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                            return;
                        } catch (InvalidKeySpecException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                            return;
                        }
                    } else {
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

        /**
         * Create a new passhash and prepare for database query
         */
        if(updateKeys.contains(PAYLOAD_CURRENT_PASSWORD_KEY)) {
            try {
                passHash = PasswordHash.createHash(new_pass);
            } catch (Exception e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
        }

        /**
         * Construct the SET string based on the fields the user wants to update.
         */
        StringBuilder setString = new StringBuilder();
        int count = 1;
        int size = updateKeys.size();
        if(updateKeys.size() > 0) {
            for (String s : updateKeys) {
                if (count == 1) {
                    setString.append("UPDATE `user` SET ");
                    if (tempPub > -1) {
                        setString.append("`publisher` = ?, ");
                    }
                }
                if (count == size) {
                    setString.append("`" + s + "` = ? ");
                } else {
                    setString.append("`" + s + "` = ?, ");
                }
                count++;
            }
        }else{
            setString.append("UPDATE `user` SET ");
            if (tempPub > -1) {
                setString.append("`publisher` = ? ");
            }
        }
        setString.append("WHERE `user_id` = ?");
        final String UPDATE_USER = setString.toString();

        /**
         * Try to update settings in database
         */
        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_USER);
            final int finalUser_id = user_id;
            final int finalPublisher = publisher;
            final String finalEmail = email;
            final String finalPassword = passHash;

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    UserObject user = null;
                    int count = 1;
                    boolean resendEmailVerification = false;

                    if(publisher > -1){
                        ps.setInt(count, finalPublisher);
                        count++;
                    }
                    if (updateValues.contains(finalEmail)) {
                        resendEmailVerification = true;
                        ps.setString(count, finalEmail);
                        count++;
                    }
                    if (updateKeys.contains(PAYLOAD_CURRENT_PASSWORD_KEY)) {
                        ps.setString(count, finalPassword);
                        count++;
                    }

                    ps.setInt(count, finalUser_id);

                    final int update = ps.executeUpdate();

                    if (update > 0) {
                        //only output the user_id and email
                        UserObject updatedUser = new UserObject(user_id,null,null,finalEmail,null,null,null,finalPublisher);

                        /**
                         * Resend email verification if email was updated
                         */
                        if(resendEmailVerification){
                            try {
                                Unirest.post("http://localhost:" + Config.API_PORT + "/users/resendemail")
                                        .header("accept", "application/json")
                                        .body("{\n" +
                                                "\"email\": \"" + finalEmail + "\"\n" + "}")
                                        .asString();
                            }catch(Exception e){
                                Logging.log("Low", e);
                                //don't throw an error for client side
                            }
                        }

                        Gson g = new Gson();
                        String response = g.toJson(updatedUser);
                        context.getResponse().setStatus(200);
                        try{
                            context.getResponse().getWriter().write(response);
                        }catch(IOException e){
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                            return;
                        }
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USER_NOT_EDITED);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }//end update settings
    }
}
