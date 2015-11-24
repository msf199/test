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
        this.connection = Pool.getConnection();
        if(this.connection == null || this.connection.isClosed()) {
            throw new SQLException("Connection was "+this.connection == null? "null" : "closed");
        }
        this.statement = this.connection.prepareStatement(query);
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