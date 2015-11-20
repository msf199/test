package main.com.whitespell.peak.logic.notifications.impl;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.NotificationImplementation;
import main.com.whitespell.peak.logic.notifications.UserNotification;
import main.com.whitespell.peak.model.UserObject;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/7/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class WelcomeNotification implements NotificationImplementation {

    private Gson g = new Gson();

    private int owner_user_id;

    public WelcomeNotification(int owner_user_id) {
        this.owner_user_id = owner_user_id;
    }

    @Override
    public void send() {

        /**
         * Send a newly created user a welcome notification with the Intro to Peak video
         */

        try {
            HttpResponse<String> stringResponse;
            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + owner_user_id)
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            UserObject me = g.fromJson(stringResponse.getBody(), UserObject.class);

            if (me != null) {
                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + me.getUserId() + "/device")
                        .header("accept", "application/json")
                        .asString();
                GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                String message = "Welcome to "+Config.PLATFORM_NAME+"!";

                UserNotification n = new UserNotification(me.getUserId(), message, "open-content:" + Config.INTRO_CONTENT_ID, Config.PLATFORM_THUMBNAIL_URL);
                insertNotification(n);

                /**
                 * Handle device notifications
                 */
                if (myDevice != null) {
                    handleDeviceNotifications(myDevice, n, message);
                } else {
                    System.out.println("Couldn't find device information for user id: " + me.getUserId());
                }
            }
        }catch(UnirestException e){
            Logging.log("High", e);
            //do not return error on client side
        }
    }
}
