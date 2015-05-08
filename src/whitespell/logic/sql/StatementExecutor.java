package whitespell.logic.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementExecutor {

    private PreparedStatement statement;
    private final Connection connection;

    public StatementExecutor(String query) throws SQLException {
        this.connection = Pool.getConnection();
        this.statement = this.connection.prepareStatement(query);
    }

    public void execute(ExecutionBlock block) throws SQLException {
        try {
            block.process(this.statement);
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }

}