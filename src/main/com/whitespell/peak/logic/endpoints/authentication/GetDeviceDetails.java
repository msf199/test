package main.com.whitespell.peak.logic.endpoints.authentication;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/4/2015
 */
public class GetDeviceDetails extends EndpointHandler {

    public static String URL_USER_ID_KEY = "userId";

    @Override
    protected void setUserInputs() {
        payloadInput.put(URL_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    private static final String RETRIEVE_DEVICE_DETAILS = "SELECT `device_uuid`, `device_name`, `device_type` FROM `authentication` WHERE `user_id` = ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        final int userId = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID_KEY));

        /**
         * Retrieve the device details for this user
         */
        try {
            StatementExecutor executor = new StatementExecutor(RETRIEVE_DEVICE_DETAILS);
            executor.execute(ps -> {
                ps.setInt(1, userId);

                ResultSet r = ps.executeQuery();
                DeviceInfo d = new DeviceInfo();
                if (r.next()){
                    d.setDeviceUUID(r.getString("device_uuid"));
                    d.setDeviceName(r.getString("device_name"));
                    d.setDeviceType(r.getInt("device_type"));
                }else{
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_RETRIEVE_DEVICE_DETAILS);
                    return;
                }
                try {
                    Gson g = new Gson();
                    String jsonDevice = g.toJson(d);
                    context.getResponse().getWriter().write(jsonDevice);
                    return;
                } catch (Exception e){
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class DeviceInfo {
        public String getDeviceUUID() {
            return deviceUUID;
        }

        public void setDeviceUUID(String deviceUUID) {
            this.deviceUUID = deviceUUID;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public int getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(int deviceType) {
            this.deviceType = deviceType;
        }

        String deviceUUID = null;
        String deviceName = null;
        int deviceType = -1;
    }
}
