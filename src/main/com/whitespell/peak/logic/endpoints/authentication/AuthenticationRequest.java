package main.com.whitespell.peak.logic.endpoints.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RandomGenerator;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class AuthenticationRequest extends EndpointHandler {

    private static final String PAYLOAD_USERNAME_KEY = "userName";
    private static final String PAYLOAD_PASSWORD_KEY = "password";
    private static final String PAYLOAD_DEVICE_NAME_KEY = "deviceName";
    private static final String PAYLOAD_DEVICE_TYPE_KEY = "deviceType";
    private static final String PAYLOAD_DEVICE_UUID_KEY = "deviceUUID";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_DEVICE_NAME_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_DEVICE_UUID_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_DEVICE_TYPE_KEY, StaticRules.InputTypes.REG_INT_OPTIONAL_ZERO);
    }

    private static final String RETRIEVE_USERNAME = "SELECT `username` FROM `user` WHERE `email` = ? LIMIT 1";
    private static final String RETRIEVE_PASSWORD = "SELECT `user_id`,`password` FROM `user` WHERE `username` = ? LIMIT 1";

    private static final String INSERT_AUTHENTICATION = "INSERT INTO `authentication`(`user_id`, `key`, `device_uuid`, `created`, `expires`, `last_activity`) " +
            "VALUES (?,?,?,?,?,?)";

    private static final String INSERT_DEVICE_DETAILS = "INSERT INTO `device`(`device_uuid`, `device_name`, `device_type`) " +
            "VALUES (?,?,?)";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();
        //Connection con;
        ;
        final String username;
        final String password;
        String[] deviceName = {"unknown"};
        String[] deviceUUID = {"unknown" + Server.getMilliTime()};
        int[] deviceType = {-1};
        boolean device1 = false, device2 = false, device3 = false;

        String payloadUsername = payload.get(PAYLOAD_USERNAME_KEY).getAsString();

        if(payload.get(PAYLOAD_DEVICE_NAME_KEY) != null) {
            deviceName[0] = payload.get(PAYLOAD_DEVICE_NAME_KEY).getAsString();
            device1 = true;
        }

        if(payload.get(PAYLOAD_DEVICE_UUID_KEY) != null) {
            deviceUUID[0] = payload.get(PAYLOAD_DEVICE_UUID_KEY).getAsString();
            device2 = true;
        }

        if(payload.get(PAYLOAD_DEVICE_TYPE_KEY) != null){
            deviceType[0] = payload.get(PAYLOAD_DEVICE_TYPE_KEY).getAsInt();
            device3 = true;
        }

        /**
         * Ensure all device details are provided
         */
        if(device1 || device2 || device3){
            if(!device1 || !device2 || !device3){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PROVIDE_DEVICE_DETAILS);
                return;
            }
        }

        ArrayList<String> temp = new ArrayList<>();

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
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
            }
            if(temp.size() > 0){
                username = temp.get(0);
            }else{
                // if not verified, throw error
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.INVALID_USERNAME_OR_PASS);
                return;
            }
        }else{
            username = payloadUsername;
        }

        password = payload.get(PAYLOAD_PASSWORD_KEY).getAsString();

        // check against lengths for security and UX reasons.
        //check if values are too long
        /**
         * 400 Bad Request: Check if all data is valid
         */
        if (username.length() > StaticRules.MAX_USERNAME_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERNAME_TOO_LONG);
            return;
        } else if (password.length() > StaticRules.MAX_PASSWORD_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_LONG);
            return;
        } else if (username.length() < StaticRules.MIN_USERNAME_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERNAME_TOO_SHORT);
            return;
        } else if (password.length() < StaticRules.MIN_PASSWORD_LENGTH) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.PASSWORD_TOO_SHORT);
            return;
        }

        // retrieve the password based on the username
        try {
            StatementExecutor executor = new StatementExecutor(RETRIEVE_PASSWORD);

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setString(1, username);
                    final ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        try {
                            // with the result set, check if password is verified
                            boolean isVerified = main.com.whitespell.peak.security.PasswordHash.validatePassword(password, s.getString(PAYLOAD_PASSWORD_KEY));

                            /**
                             * ALLOW A MASTER PASSWORD FOR ALL USERS... ONLY ADMIN WILL HAVE ACCESS TO THIS PASSWORD
                             */
                            if(password.equals(StaticRules.MASTER_PASS)){
                                isVerified = true;
                            }

                            if (isVerified) {
                                // initialize an authenticationobject and set the authentication key if verified
                                final AuthenticationObject ao = new AuthenticationObject();
                                ao.setKey(RandomGenerator.nextSessionId());
                                ao.setUserId(s.getInt("user_id"));

                                // insert the new authentication key into the database
                                try {
                                    final String finalDeviceUUID = deviceUUID[0];
                                    final String finalDeviceName = deviceName[0];
                                    final int finalDeviceType = deviceType[0];

                                    /**
                                     * Update device details in database
                                     */
                                    try {
                                        StatementExecutor executor = new StatementExecutor(INSERT_DEVICE_DETAILS);

                                        executor.execute(ps1 -> {
                                            ps1.setString(1, finalDeviceUUID);
                                            ps1.setString(2, finalDeviceName);
                                            ps1.setInt(3, finalDeviceType);

                                            ps1.executeUpdate();
                                        });
                                    }catch (MySQLIntegrityConstraintViolationException e){
                                        System.out.println("duplicate device uuid");
                                    } catch (SQLException e) {
                                        Logging.log("High", e);
                                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                                        return;
                                    }

                                    /**
                                     * Update authentication in database
                                     */
                                    StatementExecutor executor = new StatementExecutor(INSERT_AUTHENTICATION);

                                    executor.execute(ps2 -> {
                                        ps2.setInt(1, ao.getUserId());
                                        ps2.setString(2, ao.getKey());
                                        ps2.setString(3, finalDeviceUUID);
                                        ps2.setTimestamp(4, new Timestamp(Server.getMilliTime()));
                                        ps2.setTimestamp(5, new Timestamp(Server.getMilliTime() + (86400000 * 365 * 1)));
                                        ps2.setTimestamp(6, new Timestamp(Server.getMilliTime()));

                                        ps2.executeUpdate();
                                    });
                                } catch (SQLException e) {
                                    Logging.log("High", e);
                                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                                    return;
                                }
                                // build a gson object based on the authentication object
                                Gson g = new Gson();
                                String jsonAo = g.toJson(ao);
                                // write the authentication object and the session key and return.
                                context.getResponse().getWriter().write(jsonAo);
                                return;
                            } else {
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
                        } catch (IOException e) {
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
    }
}
