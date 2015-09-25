package main.com.whitespell.peak.logic.notifications.impl;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.NotificationImplementation;
import main.com.whitespell.peak.logic.notifications.UserNotification;
import main.com.whitespell.peak.model.UserObject;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/24/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class NewFollowerNotification implements NotificationImplementation {

    private Gson g = new Gson();

    private int owner_user_id;

    private int follower_user_id;


    public NewFollowerNotification(int owner_user_id, int follower_user_id) {
        this.owner_user_id = owner_user_id;
        this.follower_user_id = follower_user_id;
    }

    @Override
    public void send() {

        /**
         * Send a notification that I have a new follower
         */
        System.out.println("try userfollower notification");

        try {
            HttpResponse<String> stringResponse;
            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + owner_user_id + "")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            UserObject me = g.fromJson(stringResponse.getBody(), UserObject.class);

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + follower_user_id + "")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            UserObject follower = g.fromJson(stringResponse.getBody(), UserObject.class);

            if(me != null) {

                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + me.getUserId() + "/device")
                        .header("accept", "application/json")
                        .asString();
                GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                /**
                 * Send user notification to user being followed
                 */

                String message = "You got a new follower!";
                UserNotification n = new UserNotification(me.getUserId(), message, "open-user:"+follower.getUserId());

                insertNotification(n);

                /**
                 * Handle device notifications
                 */

                if(myDevice != null) {
                    handleDeviceNotifications(myDevice, n, message);
                } else {
                    System.out.println("Couldn't find device information for user id: " + me.getUserId());
                }
            }
        }catch(Exception e){
            Logging.log("High", e);
            return;
        }
    }
}

