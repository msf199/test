package whitespell.logic.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides query and update access to a SQL database.  All SQLExceptions
 * are treated as unrecoverable and converted to unchecked exceptions.
 */
public final class SqlDatabase {
    private final String url;
    private Connection connection = null;

    public SqlDatabase(String url) {
        this.url = url;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new SqlDatabaseException("Could not connect", e);
        }
    }

    public static SqlDatabase openSqliteFile(String filename) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        return new SqlDatabase("jdbc:sqlite:" + filename);
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new SqlDatabaseException("Could not close", e);
            }
        }
        connection = null;
    }

    public ResultSet query(String sql) {
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error executing SQL: " + sql, e);
        }
    }

    public ResultSet query(String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
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

    public int update(String sql) {
        try {
            Statement statement = connection.createStatement();
            try {
                return statement.executeUpdate(sql);
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error executing SQL: " + sql, e);
        }
    }

    public int update(String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
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
