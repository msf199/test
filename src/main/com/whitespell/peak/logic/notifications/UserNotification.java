package main.com.whitespell.peak.logic.notifications;

import java.sql.Timestamp;

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

    public void setNotificationBadge(int notificationBadge) {
        this.notificationBadge = notificationBadge;
    }

    public void setNotificationSound(int notificationSound) {
        this.notificationSound = notificationSound;
    }

    public void setNotificationStatus(int notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public Timestamp getNotificationTimestamp() {
        return notificationTimestamp;
    }

    public void setNotificationTimestamp(Timestamp notificationTimestamp) {
        this.notificationTimestamp = notificationTimestamp;
    }

    private int userId;
    private String notificationText;
    private String notificationAction;
    private int notificationBadge = 0;
    private int notificationSound = 0;
    private int notificationStatus = 0;
    private Timestamp notificationTimestamp;

    public UserNotification(int userId, String notificationText, String notificationAction) {
        this.userId = userId;
        this.notificationText = notificationText;
        this.notificationAction = notificationAction;
    }
}
