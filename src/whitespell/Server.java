package whitespell;

import whitespell.logic.config.Config;
import whitespell.logic.config.ServerProperties;
import whitespell.logic.logging.Logging;
import whitespell.logic.sql.Pool;
import whitespell.model.baseapi.WhitespellWebServer;
import whitespell.peakapi.MyEndpoints;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Server {

    private static ServerProperties systemProperties;
    public static ServerProperties getServerProperties() {
        return systemProperties;
    }
    private static Calendar calendar = new GregorianCalendar();

    public static Calendar getCalendar() {
        return calendar;
    }

    public static void main(String[] args) throws Exception {

        /*
        Initialize logging and system properties
         */

        systemProperties = new ServerProperties(Config.CONFIGURATION_FILE);
        ServerProperties.read();
        try {
            System.setErr(new PrintStream(new Logging.ErrorFile(), true));
        }catch (Exception e) {
            System.out.println("Error was thrown with config.prop config. Switching to this dir for errors");
            Logging.log("SEVERE", e);
            try {
                System.setErr(new PrintStream(new Logging.ErrorFile("."), true));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                System.out.println("Closing error logging stream");
                // close the stream so it is no longer logged anywhere
                System.err.close();
            }
        }
        calendar.setTimeZone(TimeZone.getTimeZone(Config.SERVER_TIMEZONE));
        Logging.log("RESTART","--------------------------------------------------------------------");




        // initialize MySQL Connection Pool
      Pool.initializePool();

        /* start the API */
        WhitespellWebServer testApi = new MyEndpoints();
        System.out.println("Starting "+Config.SERVER_NAME+" on main thread.");
        testApi.startAPI(Config.API_PORT);

    }

}
