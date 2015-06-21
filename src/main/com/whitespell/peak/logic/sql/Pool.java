package main.com.whitespell.peak.logic.sql;

import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Pool {
    private static DataSource ds;

    static {
        DriverAdapterCPDS cpds = new DriverAdapterCPDS();
        try {
            cpds.setDriver("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        /*
        Caused by: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: The last packet successfully received from the server was 50,847,327 milliseconds ago.
         The last packet sent successfully to the server was 50,847,328 milliseconds ago. is longer than the server configured value of 'wait_timeout'.
          You should consider either expiring and/or testing connection validity before use in your application, increasing the server configured values for client timeouts,
          or using the Connector/J connection property 'autoReconnect=true' to avoid this problem.
         */
        cpds.setUrl("jdbc:mysql://"+Config.DB_HOST+":"+Config.DB_PORT+"/"+Config.DB+"?autoreconnect=true");
        cpds.setUser(Config.DB_USER);
        cpds.setPassword(Config.DB_PASS);
        SharedPoolDataSource tds = new SharedPoolDataSource();
        tds.setConnectionPoolDataSource(cpds);
        tds.setMaxTotal(20);
        tds.setDefaultMaxWaitMillis(-1);
        tds.setValidationQuery("SELECT 1");
        tds.setDefaultMaxIdle(10);
        tds.setDefaultTestWhileIdle(true);
        tds.setDefaultMinIdle(0);
        ds = tds;
    }

    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            Logging.log("High", e);
        }
        return null;
    }

    public static void initializePool() {
        try {
            ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet query(String sql, Object... params) {
        try {
            PreparedStatement statement = ds.getConnection().prepareStatement(sql);
            int i = 0;
            for (Object param : params) {
                i++;
                statement.setObject(i, param);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error executing SQL: " + sql, e);
        }
    }

    public int update(String sql, Object... params) {
        try {
            PreparedStatement statement = ds.getConnection().prepareStatement(sql);
            try {
                int i = 0;
                for (Object param : params) {
                    i++;
                    statement.setObject(i, param);
                }
                return statement.executeUpdate();
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error executing SQL: " + sql, e);
        }
    }
}
