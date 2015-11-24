package main.com.whitespell.peak.logic.sql;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import main.com.whitespell.peak.logic.logging.Logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementExecutor {

    private Connection connection;
    private PreparedStatement statement;

    public StatementExecutor(String query) throws SQLException {
        int retryCount = 5;
        this.connection = Pool.getConnection();
        for(int i = 0; i < retryCount; i++) {
            if(prepareStatement(query)) {
             break;
            }
            if(i == 4) {
                Logging.log("HIGH", "unable to establish database connection");
            }
        }

    }

    public boolean prepareStatement(String query) throws SQLException {

        try {
            if (connection == null || connection.isClosed()) {
                connection = Pool.getConnection();
            }
            this.statement = this.connection.prepareStatement(query);
        } catch(Exception e) {
            return false;
        }
        return true;
    }
    public void execute(ExecutionBlock block) throws SQLException {
        try {
            block.process(this.statement);
        } catch (CommunicationsException e) {
            Pool.initializePool();
            Logging.log("HIGH", e);
            throw new SQLException(e);
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }

}