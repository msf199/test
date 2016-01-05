package main.com.whitespell.peak.logic.notifications.impl;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.UpdateStatus;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.NotificationImplementation;
import main.com.whitespell.peak.logic.notifications.UserNotification;
import main.com.whitespell.peak.model.UserObject;

import java.util.HashSet;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         1/4/16
 *         main.com.whitespell.peak.logic.notifications
 */
public class MassNotification implements NotificationImplementation {

    private Gson g = new Gson();
    private String text;
    private int deviceType;

    public MassNotification(String text, int deviceType){
        this.text = text;
        this.deviceType = deviceType;
    }

    /**
     * LastUserId used to obtain user list 50 users at a time.
     */
    private int lastUserId = 0;
    private boolean allNotificationsSent = false;
    private HashSet notifiedDevices = new HashSet();

    @Override
    public void send() {

        /**
         * Send a newly created user a mass notification with given text (Used for announcements, updates etc)
         */

        try {
            while(!allNotificationsSent) {
                HttpResponse<String> stringResponse;
                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users?offset=" + lastUserId)
                        .header("accept", "application/json")
                        .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                        .asString();

                /**
                 * Notifications sent successfully, empty response list from getUsers
                 */
                if (stringResponse.getBody().equals("[]")) {
                    allNotificationsSent = true;
                    break;
                }

                UserObject[] allUsers = g.fromJson(stringResponse.getBody(), UserObject[].class);

                for (int i = 0; i < allUsers.length; i++) {
                    if (allUsers[i] != null) {
                        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + allUsers[i].getUserId() + "/device")
                                .header("accept", "application/json")
                                .asString();
                        GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                        /**
                         * Prevent resending notifications to the same device
                         */
                        lastUserId = allUsers[i].getUserId();
                        if(myDevice != null && stringResponse.getBody().contains("errorId") ||
                                ((myDevice.getDeviceUUID().contains("unknown")
                                        || myDevice.getDeviceUUID().contains("simulator") ||
                                            myDevice.getDeviceType() == -1))){
                            System.out.println("bad device for userId: " + allUsers[i].getUserId());
                            System.out.println("deviceInfo: "+stringResponse.getBody());
                            continue;
                        } else if(myDevice != null && notifiedDevices.contains(myDevice.getDeviceUUID())){
                            continue;
                        }

                        String message = text;

                        UserNotification n = new UserNotification(allUsers[i].getUserId(), message, "open-content:" + Config.INTRO_CONTENT_ID, Config.PLATFORM_THUMBNAIL_URL);
                        insertNotification(n);

                        /**
                         * Handle device notifications. Only send to devices that match the device type.
                         */
                        if (myDevice != null && deviceType == myDevice.getDeviceType()) {
                            handleDeviceNotifications(myDevice, n, message);
                            notifiedDevices.add(myDevice.getDeviceUUID());
                        }
                    }
                }
            }
        }catch(UnirestException e){
            Logging.log("High", e);
            //do not return error on client side
        }
    }
}
