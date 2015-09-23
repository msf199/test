package main.com.whitespell.peak.logic.notifications;

import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public interface NotificationImplementation {

    static final String INSERT_NOTIFCATION = "INSERT INTO `notification`(`user_id`, `notification_text`, `notification_action`, `notification_badge`, `notification_sound`)" +
                                            "VALUES (?,?,?,?,?)";
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

}
