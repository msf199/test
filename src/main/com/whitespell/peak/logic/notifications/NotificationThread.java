package main.com.whitespell.peak.logic.notifications;

import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;

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
}
