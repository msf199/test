package main.com.whitespell.peak;

import javapns.Push;
import javapns.notification.PushNotificationPayload;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.UserNotification;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         11/6/15
 *         main.com.whitespell.peak
 */
public class TestAPNS {

   static String uuid = "b7d2d81c233e12e3f2d0f85d35e6de0ef015bc4182c2db3451a0f7852890c3f1";



    public static void main(String[] args) throws Exception {
        /**
         * Use JavaPNS API to send push notification to iOS
         */

        BasicConfigurator.configure();

        PushNotificationPayload payload = PushNotificationPayload.complex();
        String message = "Wow just commented on your video!";
        UserNotification n = new UserNotification(134, message, "open-content:14057", "http://peakapp.me/img/app_assets/avatar.png");

        try {
            payload.addAlert(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            payload.addCustomDictionary("action", "open-content:14057");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            Push.payload(payload, Config.APNS_CERTIFICATE_LOCATION,
                    Config.APNS_PASSWORD_KEY, true, uuid);
        } catch (Exception e) {
                        e.printStackTrace();
        } finally {
            System.out.println("Notification sent");
        }
    }

}
