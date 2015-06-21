package main.com.whitespell.peak;

import main.com.whitespell.peak.logic.ApiThread;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.config.ServerProperties;
import main.com.whitespell.peak.logic.logging.Logging;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Server {

    private static ServerProperties systemProperties;
    private static Calendar calendar = new GregorianCalendar();

    public static ServerProperties getServerProperties() {
        return systemProperties;
    }

    public static Calendar getCalendar() {
        return calendar;
    }

    public static final ApiThread apiThread = new ApiThread();

    public static void main(String[] args) throws Exception {
       start();
    }

    public static void start() {
        readConfigs();
        startApi();
    }

    public static void startApi() {
        apiThread.start();
    }

    public static void readConfigs() {
        systemProperties = new ServerProperties(Config.CONFIGURATION_FILE);
        ServerProperties.read();
        try {
            System.setErr(new PrintStream(new Logging.ErrorFile(), true));
        } catch (Exception e) {
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
        Logging.log("RESTART", "--------------------------------------------------------------------");

    }

}
