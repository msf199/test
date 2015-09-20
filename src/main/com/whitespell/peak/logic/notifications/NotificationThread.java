package main.com.whitespell.peak.logic.notifications;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class NotificationThread extends Thread {

    private static BlockingQueue<NotificationImplementation> notifications = new LinkedBlockingQueue<NotificationImplementation>();

    @Override
    public void run() {
        while(!notifications.isEmpty()) {
            NotificationImplementation notification = notifications.remove();
            notification.send();
        }
    }

    public void offerNotification(NotificationImplementation n) {
        notifications.offer(n);
    }
}
