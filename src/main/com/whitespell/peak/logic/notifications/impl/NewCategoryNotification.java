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
import main.com.whitespell.peak.model.CategoryObject;
import main.com.whitespell.peak.model.UserObject;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/24/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class NewCategoryNotification implements NotificationImplementation {

    private Gson g = new Gson();

    private String newCategoryName;


    public NewCategoryNotification(String newCategoryName) {
        this.newCategoryName = newCategoryName;
    }

    @Override
    public void send() {

        /**
         * Send a notification that a new category has been added
         */

        try {
            HttpResponse<String> stringResponse;
            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            UserObject[] users = g.fromJson(stringResponse.getBody(), UserObject[].class);

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/categories/")
                    .header("accept", "application/json")
                    .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                    .asString();
            CategoryObject[] categories = g.fromJson(stringResponse.getBody(), CategoryObject[].class);

            CategoryObject newCategory = null;
            for(int i = 0; i < categories.length; i++){
                if(categories[i].getCategoryName().equalsIgnoreCase(newCategoryName)){
                    newCategory = categories[i];
                }
            }

            if(users != null && newCategory != null) {

                for (int i = 0; i < users.length; i++) {
                    stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + users[i].getUserId() + "/device")
                            .header("accept", "application/json")
                            .asString();
                    GetDeviceDetails.DeviceInfo myDevice = g.fromJson(stringResponse.getBody(), GetDeviceDetails.DeviceInfo.class);

                    /**
                     * Send email notification to follower when uploading new content
                     */
                    String message = "A new category has been added to Peak!";
                    UserNotification n = new UserNotification(users[i].getUserId(), message, "open-category:" + newCategory.getCategoryId());

                    insertNotification(n);

                    /**
                     * Handle device notifications
                     */

                    if (myDevice != null) {
                        handleDeviceNotifications(myDevice, n, message);
                    } else {
                        System.out.println("Couldn't find device information for user id: " + i);
                    }
                }
            }
            else{
                System.out.println("category not found");
            }
        }catch(Exception e){
            Logging.log("High", e);
            return;
        }
    }
}