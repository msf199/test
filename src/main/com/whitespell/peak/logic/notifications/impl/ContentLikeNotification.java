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
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/24/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class ContentLikeNotification implements NotificationImplementation {

    private Gson g = new Gson();

    private int like_user_id;

    private int like_content_id;


    public ContentLikeNotification(int like_user_id, int like_content_id) {
        this.like_user_id = like_user_id;
        this.like_content_id = like_content_id;
    }

    @Override
    public void send() {

        /**
         * Send a notification that a user liked your video (publisher)
         */
        System.out.println("try contentlikenotification");
        try {
            HttpResponse<String> stringResponse;

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + like_content_id + "&limit=1")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            ContentObject[] content = g.fromJson(stringResponse.getBody(), ContentObject[].class);

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + content[0].getPoster().getUserId() + "")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            UserObject publisher = g.fromJson(stringResponse.getBody(), UserObject.class);

            /**
             * Notification that someone has commented on your published video (publisher)
             */
            if (publisher != null && like_user_id != publisher.getUserId()) {

                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + publisher.getUserId() + "/device")
                        .header("accept", "application/json")
                        .asString();
                GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                /**
                 * Send user notification to publisher of video that they have received a new comment.
                 */
                String message = "A user just liked your video!";
                UserNotification n = new UserNotification(publisher.getUserId(), message, "open-content:" + like_content_id);

                insertNotification(n);

                /**
                 * Handle device notifications
                 */

                if (myDevice != null) {
                    handleDeviceNotifications(myDevice, n, message);
                } else {
                    System.out.println("Couldn't find device information for user id: " + publisher.getUserId());
                }
            }

        }catch(Exception e){
            Logging.log("High", e);
            return;
        }
    }
}