package tests.com.whitespell.peak;

import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/21/15
 *         tests.com.whitespell.peak
 */
public class Tests extends Server {



    static String API = null;

    @Test
    public void startTests() throws Exception {

        // load the system with test properties
        Config.CONFIGURATION_FILE = "tests.prop";

        // start the server
        Server.start();

        // initialize the new database on the test server
        newDatabase();

        API = "http://localhost:"+Config.API_PORT+"/";

        // wait for the server to get online

        int attempts = 20;

        boolean isOnline = false;

        while(attempts > 0 && !isOnline) {
            try {
                isOnline = HttpHandler.isOnline(API+"ping");
            }catch (Exception e) {

            }
            System.out.println("Waiting for API to come online..... " + attempts + " attempts left..");
            attempts--;
            Thread.sleep(1000);
        }
        System.out.println("Server is online, lets test.");

        String str = "Junit is working fine";
        assertEquals("Junit is working fine", str);
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public void newDatabase() throws IOException {
        String databaseName = "test_" + (System.currentTimeMillis() / 1000);

        if (Config.DB_USER.equals("testpeak")) { // ensure we are on the test server
            // truncate peak_ci_test_ddl


            /**
             * CREATING THE TEST DATABASE
             */
            try {
                StatementExecutor executor = new StatementExecutor("CREATE DATABASE " + databaseName + ";");
                executor.execute(new ExecutionBlock() {

                    @Override
                    public void process(PreparedStatement ps) throws SQLException {

                        ps.executeUpdate();
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }

            /**
             * USING THE TEST DATABASE
             */

            try {
                StatementExecutor executor = new StatementExecutor("use " + databaseName + ";");
                executor.execute(new ExecutionBlock() {

                    @Override
                    public void process(PreparedStatement ps) throws SQLException {

                        ps.executeUpdate();
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }

            /**
             * EXECUTING DDL ON TEST DATABASE
             */

            String[] queries = readFile("ddl/peak.sql", StandardCharsets.UTF_8).split(";");

            for (int i = 0; i < queries.length; i++) {
                if(queries[i] == null || queries[i].length() < 2 || queries[i].isEmpty()) {
                    continue;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(queries[i]);
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
    }

}
