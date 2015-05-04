package whitespell.logic.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ExecutionBlock {

    public void prepare(PreparedStatement ps) throws SQLException;

}