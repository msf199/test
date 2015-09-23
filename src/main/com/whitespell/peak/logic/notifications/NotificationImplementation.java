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
    static final String UPDATE_NOTIFICATION = "";

    public abstract void send();
    public default void insertNotification(UserNotification n) {

            /**
             * Update device details in database
             */
            try {
                StatementExecutor executor = new StatementExecutor(INSERT_NOTIFCATION);

                executor.execute(ps1 -> {
                    ps1.setInt(1, n.getUserId());
                    ps1.setString(2, n.getNotificationText());
                    ps1.setString(3, n.getNotificationAction());
                    ps1.setInt(4, n.getNotificationBadge());
                    ps1.setInt(5, n.getNotificationBadge());

                    ps1.executeUpdate();
                });
            }catch (SQLException e) {
                Logging.log("High", e);
            }
    }

}
