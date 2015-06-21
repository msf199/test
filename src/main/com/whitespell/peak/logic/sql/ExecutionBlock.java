package main.com.whitespell.peak.logic.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ExecutionBlock {

    public void process(PreparedStatement ps) throws SQLException;

}