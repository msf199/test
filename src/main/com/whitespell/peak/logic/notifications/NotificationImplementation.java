package main.com.whitespell.peak.logic.notifications;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import javapns.Push;
import javapns.notification.PushNotificationPayload;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.apache.log4j.BasicConfigurator;

import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public interface NotificationImplementation {

    static final String INSERT_NOTIFCATION = "INSERT INTO `notification`(`user_id`, `notification_text`, `notification_action`, `notification_badge`, `notification_sound`, `notification_image`)" +
                                            "VALUES (?,?,?,?,?,?)";
    static final String UPDATE_NOTIFICATION = "UPDATE `notification` SET `notification_status` = 1 WHERE `user_id` = ? AND `notification_text` = ? " +
            "AND `notification_action` = ?";

    public abstract void send();
    public default void insertNotification(UserNotification n) {

            /**
             * Insert the notification details
             */
            try {
                StatementExecutor executor = new StatementExecutor(INSERT_NOTIFCATION);

                executor.execute(ps1 -> {
                    ps1.setInt(1, n.getUserId());
                    ps1.setString(2, n.getNotificationText());
                    ps1.setString(3, n.getNotificationAction());
                    ps1.setInt(4, n.getNotificationBadge());
                    ps1.setInt(5, n.getNotificationSound());
                    ps1.setString(6, n.getNotificationImage());

                    ps1.executeUpdate();
                });
            }catch (SQLException e) {
                Logging.log("High", e);
            }
    }

    public default void successfulNotification(UserNotification n) {

        /**
         * Update notification status to 1 for success
         */
        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_NOTIFICATION);

            executor.execute(ps1 -> {
                ps1.setInt(1, n.getUserId());
                ps1.setString(2, n.getNotificationText());
                ps1.setString(3, n.getNotificationAction());

                ps1.executeUpdate();
            });
        }catch (SQLException e) {
            Logging.log("High", e);
        }
    }

    public default void handleDeviceNotifications(GetDeviceDetails.DeviceInfo deviceInfo, UserNotification notification, String message){
        boolean iOSDevice = deviceInfo.getDeviceType() == 0;
        boolean androidDevice = deviceInfo.getDeviceType() == 1;
        try {
            if (androidDevice) {

                /**
                 * Use Google Cloud to send push notification to Android
                 */

                HttpResponse<String> stringResponse = Unirest.post("https://gcm-http.googleapis.com/gcm/send")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "key=" + Config.GOOGLE_MESSAGING_API_KEY)
                        .body("{\"data\":{\n" +
                                "\"title\": \"" + message + "\"" +
                                "\"message\": \"" + message + "\"," +
                                "\"action\": \"" + notification.getNotificationAction() + "\"" +
                                "\n},\n" +
                                "\"to\": \"" + deviceInfo.getDeviceUUID() + "\"}")
                        .asString();

                if (!stringResponse.getBody().contains("INVALID")) {
                    successfulNotification(notification);
                }else{
                    System.out.println("notificationFailed: " + stringResponse.getBody() + " deviceUUID: " + deviceInfo.getDeviceUUID());
                }
            } else if (iOSDevice) {

                /**
                 * Use JavaPNS API to send push notification to iOS
                 */

                BasicConfigurator.configure();

                PushNotificationPayload payload = PushNotificationPayload.complex();

                payload.addAlert(message);
                payload.addCustomDictionary("action", notification.getNotificationAction());


                try {
                    Push.payload(payload, Config.APNS_CERTIFICATE_LOCATION,
                            Config.APNS_PASSWORD_KEY, true, deviceInfo.getDeviceUUID());
                   Logging.log("High", "deviceUUID: "+deviceInfo.getDeviceUUID());
                } catch (Exception e) {
                    Logging.log("High", e);
                    e.printStackTrace();
                } finally {
                    //success
                    successfulNotification(notification);
                }
            }
        } catch (Exception e) {
            Logging.log("High", e);
        }
    }

}
