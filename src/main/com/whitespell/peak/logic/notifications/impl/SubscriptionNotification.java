package main.com.whitespell.peak.logic.notifications.impl;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.logic.UserHelper;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.NotificationImplementation;
import main.com.whitespell.peak.logic.notifications.UserNotification;
import main.com.whitespell.peak.logic.slack.SendSlackMessage;
import main.com.whitespell.peak.model.UserObject;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/7/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class SubscriptionNotification implements NotificationImplementation {

    private Gson g = new Gson();

    private int owner_user_id;

    public SubscriptionNotification(int owner_user_id) {
        this.owner_user_id = owner_user_id;
    }

    @Override
    public void send() {

        /**
         * Send a newly created user a thank you for your loyalty notification
         */

        try {
            HttpResponse<String> stringResponse;
            UserHelper h = new UserHelper();

            UserObject me = h.getUserById(owner_user_id, false, false, false, false);

            if (me != null) {

                try {
                    SendSlackMessage s = new SendSlackMessage("Ca-ching! New subscriber! Username: "+me.getUserName()+", Email: "+me.getEmail());
                    s.sendNotification();
                } catch(Exception e) {
                    e.printStackTrace();
                }


                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + me.getUserId() + "/device")
                        .header("accept", "application/json")
                        .asString();
                GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                String message = "Thank you for subscribing to  "+Config.PLATFORM_NAME+"!";

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
