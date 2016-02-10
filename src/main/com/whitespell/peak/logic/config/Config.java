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

    private static boolean dev = true;

    //General server information
    public static String SERVER_NAME = "API";
    public static int SERVER_VERSION = 1;

    //General mandrill mail information
    public static String MANDRILL_API_KEY = "";
    public static String MANDRILL_API_VERSION = "1.0";
    public static String MANDRILL_API_URL = "";

    //Facebook API information
    public static String FB_APP_ID = "";
    public static String FB_APP_SECRET = "";
    public static String FB_API_VERSION = "v2.4";
    public static String FB_APP_ACCESS_TOKEN = FB_APP_ID + "|" + FB_APP_SECRET;

    //Google Cloud Messaging API information
    public static String GOOGLE_MESSAGING_API_KEY = "";

    //APNS Push API information
    public static String APNS_CERTIFICATE_LOCATION = "";
    public static String APNS_PASSWORD_KEY = "";

    //AWS S3 API information
    public static String AWS_API_KEY_ID = "";
    public static String AWS_API_SECRET = "";
    public static String AWS_API_BUCKET = "";
    public static String AWS_API_VID_BUCKET = "";
    public static String AWS_API_IMG_BUCKET = "";
    public static String AWS_API_HOSTNAME = "";

    //Cloudinary API information
    public static String CL_API_KEY_ID = "";
    public static String CL_API_SECRET = "";
    public static String CL_API_FOLDER = "";
    public static String CL_API_HOSTNAME = "";

    //General mysql information
    public static String DB = "peak";
    public static String DB_HOST = "";
    public static String DB_USER = "root";
    public static String DB_PASS = "";
    public static int DB_PORT = 3306;

    //External Functionality Toggles
    public static boolean NOTIFICATION_TOGGLE = true;
    public static boolean VIDEOS_IN_NEWSFEED = false;

    //Static ids and urls
    public static int INTRO_CONTENT_ID = 14546;

    //Static device details
    public static int IOS_DEVICE_TYPE_ID = 0;
    public static int ANDROID_DEVICE_TYPE_ID = 1;

    //Static order details
    public static int ORDER_ORIGIN_APPLE = 1;
    public static int ORDER_ORIGIN_GOOGLE = 2;
    public static int ORDER_ORIGIN_WEB = 3;
    public static int ORDER_TYPE_BUNDLE = 1;
    public static int ORDER_TYPE_SUBSCRIPTION = 2;
    public static String ORDER_CURRENCY_USD_SYMBOL = "$";
    public static String ORDER_CURRENCY_USD_NAME = "USD";
    public static double ORDER_SUBSCRIPTION_PRICE = 3.99;

    //Google Order details
    public static String GOOGLE_CLIENT_ID = "";
    public static String GOOGLE_PRIVATE_KEY_PATH = "";
    public static String GOOGLE_PACKAGE_NAME = "";
    public static String GOOGLE_PURCHASE_99 = "";
    public static String GOOGLE_PURCHASE_199 = "";
    public static String GOOGLE_PURCHASE_299 = "";
    public static String GOOGLE_PURCHASE_399 = "";
    public static String GOOGLE_PURCHASE_499 = "";
    public static String GOOGLE_PURCHASE_599 = "";
    public static String GOOGLE_PURCHASE_699 = "";
    public static String GOOGLE_SUBSCRIPTION_MO = "";

    //Platform Details
    public static String PLATFORM_NAME = "Upfit";
    public static String PLATFORM_THUMBNAIL_URL = "https://s3.amazonaws.com/peak-users/img/upfit-og.png";
    public static String PLATFORM_VIEW_CONTENT_URL = "https://app.upfit.co/#/post/";
    public static String PLATFORM_HOME_PAGE_URL = "https://www.upfit.co";
    public static String PLATFORM_EMAIL_SEND_ADDRESS = "upfit@whitespell.com";
    public static String PLATFORM_EMAIL_SEND_NAME = "Upfit";
    public static String PLATFORM_DEFAULT_THUMBNAIL = "http://peakapp.me/img/app_assets/avatar.png";

    //server.Server Ports
    public static int API_PORT = 80;

    //security measures
    public static String ERROR_PATH = "/";
    public static int MAX_ERROR_FOLDER_SIZE_MB = 10;


    public static void setDev(boolean devStatus) {
        dev = devStatus;
    }


    public static boolean isDev() {
        return dev;
    }
}