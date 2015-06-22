package tests.com.whitespell.peak;

import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.Pool;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/21/15
 *         tests.com.whitespell.peak
 */
public class TestFunctions {


    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }



    public void dropDatabase() {
        /**
         * CREATING THE TEST DATABASE
         */
        try {
            StatementExecutor executor = new StatementExecutor("DROP DATABASE " + Tests.TEST_DB_NAME + ";");
            executor.execute(new ExecutionBlock() {

                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    ps.executeUpdate();
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }
}
