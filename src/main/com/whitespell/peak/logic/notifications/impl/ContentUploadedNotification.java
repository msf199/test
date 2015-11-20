package main.com.whitespell.peak.logic.notifications.impl;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EmailSend;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.NotificationImplementation;
import main.com.whitespell.peak.logic.notifications.UserNotification;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class ContentUploadedNotification implements NotificationImplementation {

    private Gson g = new Gson();

    private int owner_user_id;

    private ContentObject contentObject;


    public ContentUploadedNotification(int owner_user_id, ContentObject contentObject) {
        this.owner_user_id = owner_user_id;
        this.contentObject = contentObject;
    }

    @Override
    public void send() {

        /**
         * Send all of my followers a notification that I have uploaded a new video
         */

        try {
            HttpResponse<String> stringResponse;
            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + owner_user_id + "?includeFollowers=1")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            UserObject me = g.fromJson(stringResponse.getBody(), UserObject.class);

            if(me != null) {
                ArrayList<Integer> followerIds = me.getUserFollowers();
                String publisherUsername = me.getUserName();
                boolean sendEmail = me.getEmailNotifications() == 1;
                boolean emailVerified = me.getEmailVerified() == 1;

                if (followerIds != null && followerIds.size() >= 1) {
                    for (int i : followerIds) {
                        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + i)
                                .header("accept", "application/json")
                                .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                                .asString();
                        UserObject follower = g.fromJson(stringResponse.getBody(), UserObject.class);

                        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + i + "/device")
                                .header("accept", "application/json")
                                .asString();
                        GetDeviceDetails.DeviceInfo followerDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                        boolean sent[] = {false};

                        /**
                         * Send email notification to follower when uploading new content
                         */


                        String message = publisherUsername + " uploaded a video!";


                        UserNotification n = new UserNotification(follower.getUserId(), message, "open-content:"+contentObject.getContentId(), me.getThumbnail());

                        insertNotification(n);

                        if(sendEmail && emailVerified) {
                            /**
                             * The content will be displayed on the peakapp post page, based on contentId
                             */
                            String contentUrl = Config.PLATFORM_VIEW_CONTENT_URL + contentObject.getContentId();

                            sent[0] = EmailSend.sendFollowerContentNotificationEmail(
                                    me.getThumbnail(), follower.getEmail(),
                                    publisherUsername, contentObject.getContentTitle(), contentUrl);
                        }

                        /**
                         * Handle device notifications
                         */
                        if(followerDevice != null) {
                            handleDeviceNotifications(followerDevice, n, message);
                        } else {
                            System.out.println("Couldn't find device information for user id: " + i);
                        }
                    }
                }
            }
        }catch(Exception e){
            Logging.log("High", e);
            return;
        }
    }
}
