package main.com.whitespell.peak.logic.notifications;

import main.com.whitespell.peak.logic.logging.Logging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class NotificationThread extends Thread {

    private static BlockingQueue<NotificationImplementation> notifications = new LinkedBlockingQueue<NotificationImplementation>();

    private boolean running = false;

    public void run() {
        running = true;

        do {
            try {
                if (notifications.isEmpty()) {
                    NotificationImplementation notification = notifications.take();
                    notification.send();
                }
            } catch(Exception e) {
                Logging.log("HIGH", e);
            }

        } while (running);

    }

    public void offerNotification(NotificationImplementation n) {
        notifications.add(n);
    }
}
