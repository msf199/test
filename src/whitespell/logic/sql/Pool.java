package whitespell.logic.sql;

import org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Pool
{
    private static DataSource ds;

    static
    {
        DriverAdapterCPDS cpds = new DriverAdapterCPDS();
        try {
            cpds.setDriver("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        cpds.setUrl("jdbc:mysql://173.194.241.59:3306/peak");
        cpds.setUser("api");
        cpds.setPassword("LS6GP6CJ");

        SharedPoolDataSource tds = new SharedPoolDataSource();
        tds.setConnectionPoolDataSource(cpds);
        tds.setMaxTotal(10);
        tds.setDefaultMaxWaitMillis(50);

        ds = tds;
    }

    public static Connection getConnection()
    {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
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
}
