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
import main.com.whitespell.peak.model.CommentObject;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/24/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class ContentCommentNotification implements NotificationImplementation {

    private Gson g = new Gson();

    private int commenter_user_id;

    private int comment_content_id;


    public ContentCommentNotification(int commenter_user_id, int comment_content_id) {
        this.commenter_user_id = commenter_user_id;
        this.comment_content_id = comment_content_id;
    }

    @Override
    public void send() {

        /**
         * Send a notification that a comment was added to your video (publisher) and send a notification to other commenters on the video (users)
         */

        try {
            HttpResponse<String> stringResponse;

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + comment_content_id + "&limit=1")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            ContentObject[] content = g.fromJson(stringResponse.getBody(), ContentObject[].class);

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + content[0].getPoster().getUserId() + "")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            UserObject publisher = g.fromJson(stringResponse.getBody(), UserObject.class);

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/" + comment_content_id + "/comments")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            CommentObject[] comments = g.fromJson(stringResponse.getBody(), CommentObject[].class);

            /**
             * Notification that someone has commented on your published video (publisher)
             */
            if(publisher != null && publisher.getUserId() != commenter_user_id) {

                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + publisher.getUserId() + "/device")
                        .header("accept", "application/json")
                        .asString();
                GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                /**
                 * Send user notification to publisher of video that they have received a new comment.
                 */
                String message = "A user just commented on your video!";
                UserNotification n = new UserNotification(publisher.getUserId(), message, "open-content:"+comment_content_id);

                insertNotification(n);

                /**
                 * Handle device notifications
                 */

                if(myDevice != null) {
                    handleDeviceNotifications(myDevice, n, message);
                } else {
                    System.out.println("Couldn't find device information for user id: " + publisher.getUserId());
                }
            }

            /**
             * Send notification to all OTHER users that have commented on this video (users)
             */
            if(comments != null){
               for(int i = 0; i< comments.length; i++) {
                   if (comments[i].getPoster().getUserId() != commenter_user_id  && commenter_user_id != publisher.getUserId()) {
                       stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + comments[i].getPoster().getUserId() + "/device")
                               .header("accept", "application/json")
                               .asString();
                       GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                       /**
                        * Send user notification to other commenters that a new comment was posted.
                        */

                       String message = "New comment on a video you commented on!";
                       UserNotification n = new UserNotification(comments[i].getPoster().getUserId(), message, "open-content:" + comment_content_id);

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
               }
            }
        }catch(Exception e){
            Logging.log("High", e);
            return;
        }
    }
}