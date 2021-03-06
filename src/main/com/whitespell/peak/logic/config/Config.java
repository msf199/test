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

    //General mandrill mail information
    public static String MANDRILL_API_KEY = "_nuIwbGbVG1bvkxZo7LoiQ";
    public static String MANDRILL_API_VERSION = "1.0";
    public static String MANDRILL_API_URL = "https://mandrillapp.com/api";

    //Facebook API information
    public static String FB_APP_ID = "452573284915725";
    public static String FB_APP_SECRET = "f57760a7d12677fab8f8b99edc09bfba";
    public static String FB_API_VERSION = "v2.4";
    public static String FB_APP_ACCESS_TOKEN = FB_APP_ID + "|" + FB_APP_SECRET;

    //Google Cloud Messaging API information
    public static String GOOGLE_MESSAGING_API_KEY = "AIzaSyDTv7nfVlKXo73ykx-PYpecPWMMR9iIBXA";

    //APNS Push API information
    public static String APNS_CERTIFICATE_LOCATION = "certificates/iosPushCertificate.p12";
    public static String APNS_PASSWORD_KEY = "Halo2";

    //AWS S3 API information
    public static String AWS_API_KEY_ID = "AKIAIKEHG3TEUSIZLVVQ";
    public static String AWS_API_SECRET = "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";
    public static String AWS_API_BUCKET = "peak-users";
    public static String AWS_API_VID_BUCKET = "peak-users/vid";
    public static String AWS_API_IMG_BUCKET = "peak-users/img";
    public static String AWS_API_HOSTNAME = "s3.amazonaws.com";

    //Cloudinary API information
    public static String CL_API_KEY_ID = "417454796385697";
    public static String CL_API_SECRET = "_i8gmSm5ZSVcURutBeAOFI93UZ4";
    public static String CL_API_FOLDER = "whitespell-inc";
    public static String CL_API_HOSTNAME = "res.cloudinary.com";

    //General mysql information
    public static String DB = "peak";
    public static String DB_HOST = "127.0.0.1";
    public static String DB_USER = "root";
    public static String DB_PASS = "";
    public static int DB_PORT = 3306;

    //External Functionality Toggles
    public static boolean NOTIFICATION_TOGGLE = true;

    //Static ids and urls
    public static int INTRO_CONTENT_ID = 14131;
    public static String PEAK_THUMBNAIL_URL = "https://s3.amazonaws.com/peak-users/img/peak_thumbnail.png";
    public static String PEAK_VIEW_CONTENT_URL = "http://app.peakapp.me/#/post/";

    //server.Server Ports
    public static int API_PORT = 8000;

    //security measures
    public static String ERROR_PATH = "/";
    public static int MAX_ERROR_FOLDER_SIZE_MB = 10;


}