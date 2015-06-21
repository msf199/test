package main.com.whitespell.peak.logic.sql;

public class SqlDatabaseException extends RuntimeException {
    public SqlDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
