package main.com.whitespell.peak.logic.notifications;

import javapns.Push;
import javapns.notification.PushNotificationPayload;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import org.apache.log4j.BasicConfigurator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Pim de Witte(wwadewitte) and Cory McAn(cmcan), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class NotificationThread extends Thread {

    private static BlockingQueue<NotificationImplementation> notifications = new LinkedBlockingQueue<>();

    private boolean running = false;

    public void run() {
        running = true;

        do {
            try {
                if (!notifications.isEmpty() && Config.NOTIFICATION_TOGGLE) {
                    NotificationImplementation notification = notifications.take();
                    notification.send();
                } else {
                    Thread.sleep(1000);
                }
            } catch(Exception e) {
                Logging.log("HIGH", e);
            }

        } while (running);

    }

    public void offerNotification(NotificationImplementation n) {
        if(Config.NOTIFICATION_TOGGLE){
            notifications.add(n);
        }
    }

    public static void main(String args[]){

        /**
         * Use JavaPNS API to send push notification to iOS
         */

        BasicConfigurator.configure();

        PushNotificationPayload payload = PushNotificationPayload.complex();
        try {
            payload.addAlert("test message for arielle");
            Push.payload(payload, Config.APNS_CERTIFICATE_LOCATION,
                    Config.APNS_PASSWORD_KEY, true, "6f32e69a586a5bb7df100209586fa45929f66ec62b6df1330dfb88cd7ee1bdf2");
        }catch(Exception e){
            Logging.log("High",e);
        }
    }
}
