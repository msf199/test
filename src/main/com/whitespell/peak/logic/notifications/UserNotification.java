package main.com.whitespell.peak.logic.notifications;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         9/22/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class UserNotification {

    public int getUserId() {
        return userId;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public String getNotificationAction() {
        return notificationAction;
    }

    public int getNotificationBadge() {
        return notificationBadge;
    }

    public int getNotificationSound() {
        return notificationSound;
    }

    public int getNotificationStatus() {
        return notificationStatus;
    }

    private int userId;
    private String notificationText;
    private String notificationAction;
    private int notificationBadge = 0;
    private int notificationSound = 0;
    private int notificationStatus = 0;

    public UserNotification(int userId, String notificationText, String notificationAction) {
        this.userId = userId;
        this.notificationText = notificationText;
        this.notificationAction = notificationAction;
    }
}
