package main.com.whitespell.peak.logic.config;


public final class Config {

    public static boolean TESTING = false;

    /*
    NOT OVERWRITTEN BY CONFIG.PROP BUT USED BY THE SERVER:
     */

    public static String CONFIGURATION_FILE = "config.prop";

    /*
    CONFIG.PROP
     */


    //General server information
    public static String SERVER_NAME = "API";
    public static int SERVER_VERSION = 1;
    public static String SERVER_TIMEZONE = "UTC";

    //General mysql information
    public static String DB = "peak";
    public static String DB_HOST = "127.0.0.1";
    public static String DB_USER = "root";
    public static String DB_PASS = "";
    public static int DB_PORT = 3306;

    //External Functionality Toggles


    //server.Server Ports
    public static int API_PORT = 8000;

    //security measures
    public static String ERROR_PATH = "/";
    public static int MAX_ERROR_FOLDER_SIZE_MB = 10;


}