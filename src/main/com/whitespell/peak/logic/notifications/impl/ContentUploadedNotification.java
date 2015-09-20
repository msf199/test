package main.com.whitespell.peak.logic.notifications.impl;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import javapns.Push;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EmailSend;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.NotificationImplementation;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;
import org.apache.log4j.BasicConfigurator;

import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
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

                        sent[0] = EmailSend.sendFollowerContentNotificationEmail(
                                follower.getUserName(), me.getThumbnail(), follower.getEmail(), publisherUsername, contentObject.getContentTitle(), contentObject.getContentUrl());

                        /**
                         * Handle device notifications
                         */

                        if(followerDevice != null) {
                            boolean iOSDevice = followerDevice.getDeviceType() == 0;
                            boolean androidDevice = followerDevice.getDeviceType() == 1;
                            try {
                                if (androidDevice) {

                                    /**
                                     * Use Google Cloud to send push notification to Android
                                     */

                                    String googleMessagingApiKey = Config.GOOGLE_MESSAGING_API_KEY;
                                    Unirest.post("https://gcm-http.googleapis.com/gcm/send")
                                            .header("accept", "application/json")
                                            .header("Content-Type", "application/json")
                                            .header("Authorization", "key=" + googleMessagingApiKey)
                                            .body("\"data\":{\n" +
                                                    "\"title\": \"" + publisherUsername + " uploaded a new video!\"" +
                                                    "\"message\": \"" + publisherUsername + " uploaded " + contentObject.getContentTitle() + "!\"" +
                                                    "\n},\n" +
                                                    "\"to\": \"" + followerDevice.getDeviceUUID() + "\"")
                                            .asString();

                                } else if (iOSDevice) {

                                    /**
                                     * Use JavaPNS API to send push notification to iOS
                                     */

                                    BasicConfigurator.configure();
                                    Push.alert(publisherUsername + "uploaded a new video!", Config.APNS_CERTIFICATE_LOCATION,
                                            Config.APNS_PASSWORD_KEY, false, followerDevice.getDeviceUUID());
                                }
                            }
                            catch(Exception e){
                                Logging.log("High", e);
                            }
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
