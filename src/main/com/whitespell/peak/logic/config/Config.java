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
    public static String APNS_CERTIFICATE_LOCATION = "certificates/final-prod.p12";
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
    public static boolean VIDEOS_IN_NEWSFEED = false;

    //Static ids and urls
    public static int INTRO_CONTENT_ID = 14131;

    //Static order details
    public static int ORDER_ORIGIN_APPLE = 1;
    public static int ORDER_ORIGIN_GOOGLE = 2;
    public static int ORDER_ORIGIN_WEB = 3;
    public static int ORDER_TYPE_BUNDLE = 1;
    public static int ORDER_TYPE_SUBSCRIPTION = 2;
    public static int ORDER_CURRENCY_USD = 1;
    public static double ORDER_SUBSCRIPTION_PRICE = 3.99;

    //Google Order details
    public static String GOOGLE_CLIENT_ID = "954032824901-8oifacg1pmnpn8fr3tmumoqjvovfa6ka@developer.gserviceaccount.com";
    public static String GOOGLE_PRIVATE_KEY_PATH = "certificates/Google Play Android Developer-715af11a1027.p12";
    public static String GOOGLE_PACKAGE_NAME = "com.whitespell.upfit";
    public static String GOOGLE_PURCHASE_99 = "com.whitespell.upfit.inapp1";
    public static String GOOGLE_PURCHASE_199 = "com.whitespell.upfit.inapp2";
    public static String GOOGLE_PURCHASE_299 = "com.whitespell.upfit.inapp3";
    public static String GOOGLE_PURCHASE_399 = "com.whitespell.upfit.inapp4";
    public static String GOOGLE_PURCHASE_499 = "com.whitespell.upfit.inapp5";
    public static String GOOGLE_PURCHASE_599 = "com.whitespell.upfit.inapp6";
    public static String GOOGLE_PURCHASE_699 = "com.whitespell.upfit.inapp7";
    public static String GOOGLE_SUBSCRIPTION_MO = "com.whitespell.upfit.substest1mo";

    //Platform Details
    public static String PLATFORM_NAME = "Upfit";
    public static String PLATFORM_THUMBNAIL_URL = "https://s3.amazonaws.com/peak-users/img/upfit-og.png";
    public static String PLATFORM_VIEW_CONTENT_URL = "https://app.upfit.co/#/post/";
    public static String PLATFORM_HOME_PAGE_URL = "https://www.upfit.co";
    public static String PLATFORM_EMAIL_SEND_ADDRESS = "upfit@whitespell.com";
    public static String PLATFORM_EMAIL_SEND_NAME = "Upfit by Whitespell";

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