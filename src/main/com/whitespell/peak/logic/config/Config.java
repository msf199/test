package main.com.whitespell.peak.logic.config;


import facebook4j.conf.ConfigurationBuilder;

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

    //General mandrill mail information
    public static String MANDRILL_API_KEY = "_nuIwbGbVG1bvkxZo7LoiQ";
    public static String MANDRILL_API_VERSION = "1.0";
    public static String MANDRILL_API_URL = "https://mandrillapp.com/api";

    //Facebook API information
    public static String FB_APP_ID = "452573284915725";
    public static String FB_APP_SECRET = "f57760a7d12677fab8f8b99edc09bfba";
    public static String FB_API_VERSION = "v2.4";
    public static String FB_APP_ACCESS_TOKEN = FB_APP_ID + "|" + FB_APP_SECRET;


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