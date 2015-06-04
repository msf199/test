package whitespell.logic;

import whitespell.StaticRules;
import whitespell.logic.logging.Logging;
import whitespell.logic.sql.ExecutionBlock;
import whitespell.logic.sql.StatementExecutor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/2/15
 *         whitespell.logic
 */
public class Authentication {

    private static final String IS_AUTHENTICATED = "SELECT 1 FROM `authentication` WHERE `user_id` = ? AND `key` = ? LIMIT 1"; //todo add expiration on keys

    private final int userId;
    private final String key;
    public Authentication(String authenticationHeader) {

        if(authenticationHeader == null) {
            userId = -1;
            key = null;
        } else {

            String[] tempHeader = authenticationHeader.split(",");

            if (tempHeader.length != 2 || !Safety.isNumeric(tempHeader[0]) || tempHeader[1].length() < StaticRules.MIN_AUTHENTICATION_HEADER_LENGTH || tempHeader[1].length() > StaticRules.MAX_AUTHENTICATION_HEADER_LENGTH) {
                userId = -1;
                key = null;
            } else {
                userId = Integer.parseInt(tempHeader[0]);
                key = tempHeader[1];
            }
        }

    }

    /**
     * Check whether the user doing an API call is authenticated properly

     * @return
     */
    public boolean isAuthenticated() {

        //todo in the future we want to store all the most active keys in a memcache layer so authentication is faster.

        if(userId < 0 || key == null) {
            return false;
        }

        System.out.println(" user id " + " -> " + " key ->" + key);

        final boolean[] authenticated = new boolean[1];

        // by default, we are not authentication
        authenticated[0] = false;

        try {
            StatementExecutor executor = new StatementExecutor(IS_AUTHENTICATED);

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setInt(1, userId);
                    ps.setString(2, key);
                    final ResultSet s = ps.executeQuery();

                    // if we do find a result, we have been authenticated
                    if (s.next()) {
                        authenticated[0] = true;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }

        return authenticated[0];
    }

    public int getUserId() {
        return userId;
    }
}
