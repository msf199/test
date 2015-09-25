package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/2/15
 *         whitespell.logic
 */
public class Authentication {

    private static final String IS_AUTHENTICATED = "SELECT `expires` FROM `authentication` WHERE `user_id` = ? AND `key` = ? AND `expires` > ? LIMIT 1"; //todo add expiration on keys
    private static final String UPDATE_ACTIVITY = "UPDATE `authentication` SET `last_activity` = ? , `expires` = ? WHERE `user_id` = ? AND `key` = ? LIMIT 1";
    private static final String[] masterKeys = {
            "4ajerifjaierjf34ijfi34jij3a4ifj34ijf"
    };

    private final int userId;
    private final String key;

    /**
     * Authentication processes every authentication related function. The idea is that this object is reproducable over multiple threads.
     * The Authentication object is created every time authentication is called, so that it is executed on the thread the request is handled.
     *
     * @param authenticationHeader
     */
    public Authentication(String authenticationHeader) {

        if (authenticationHeader == null) {
            userId = -1;
            key = null;
        } else {

            String[] tempHeader = authenticationHeader.split(",");

            if (tempHeader.length != 2 || !Safety.isInteger(tempHeader[0]) || tempHeader[1].length() < StaticRules.MIN_AUTHENTICATION_HEADER_LENGTH || tempHeader[1].length() > StaticRules.MAX_AUTHENTICATION_HEADER_LENGTH) {
                userId = -1;
                key = null;
            } else {
                userId = Integer.parseInt(tempHeader[0]);
                key = tempHeader[1];
            }
        }

    }

    /**
     * Check whether the user doing an API safeCall is authenticated properly
     *
     * @return whether the user is authenticated or not.
     */

    public boolean isAuthenticated() {

        //todo in the future we want to store all the most active keys in a memcache layer so authentication is faster.

        if(userId == -1) {
            if(key != null && isMasterKey(key)) {
                return true;
            }
        }

        if (userId < 0 || key == null) {
            return false;
        }

        final boolean[] authenticated = new boolean[1];

        // by default, we are not authentication
        authenticated[0] = false;

        try {
            StatementExecutor executor = new StatementExecutor(IS_AUTHENTICATED);

            executor.execute(ps -> {
                ps.setInt(1, userId);
                ps.setString(2, key);
                ps.setTimestamp(3, new Timestamp(Server.getCalendar().getTimeInMillis()));
                final ResultSet s = ps.executeQuery();

                // if we do find a result and it hasn't expired, we have been authenticated
                if (s.next()) {
                        authenticated[0] = true;
                        this.updateActivity(userId, key);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }

        return authenticated[0];
    }

    private void updateActivity(int userId, String key) {
        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_ACTIVITY);

            executor.execute(ps -> {

                ps.setTimestamp(1, new Timestamp(Server.getCalendar().getTimeInMillis())); // set last activity to now
                ps.setTimestamp(2, new Timestamp(Server.getCalendar().getTimeInMillis() + 86400000 * 60)); // set expires to 2 months from last auth
                ps.setInt(3, userId);
                ps.setString(4, key);
                ps.executeUpdate();
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }

    public boolean isMasterKey(String key) {
        for(int i = 0; i < masterKeys.length; i++) {
            if(masterKeys[i].equals(key)) {
                return true;
            }
        }
        return false;
    }

    public int getUserId() {
        return userId;
    }
    public String getKey() {
        return key;
    }
}
