package whitespell.logic.config;


public final class Config {

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

    //External Functionality Toggles


    //server.Server Ports
    public static int API_PORT = 80;

    //hosts

    public static String MYSQL_HOST = "localhost";

    //security measures
    public static String ERROR_PATH = "/";
    public static int MAX_ERROR_FOLDER_SIZE_MB = 10;



}