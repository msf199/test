package com.whitespell.peak;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.TestUser;
import facebook4j.conf.ConfigurationBuilder;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EmailSend;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.authentication.ExpireAuthentication;
import main.com.whitespell.peak.logic.endpoints.content.*;
import main.com.whitespell.peak.logic.endpoints.content.types.AddReportingType;
import main.com.whitespell.peak.logic.endpoints.users.*;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.GetUserNotifications;
import main.com.whitespell.peak.logic.notifications.UserNotification;
import main.com.whitespell.peak.logic.sql.Pool;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.*;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Collections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         6/21/15
 *         tests.com.whitespell.peak
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTests extends Server {

    static String TEST_DB_NAME = "test_" + (System.currentTimeMillis() / 1000);

    static String TEST_USERNAME = "pimdewitte";
    static String TEST_PASSWORD = "3#$$$$$494949($(%*__''";
    static String TEST_EMAIL = "testytest@testy.com";
    static int TEST_UID = -1;
    static String TEST_KEY;
    static int TEST2_UID = -1;
    static String TEST2_KEY;
    static int TEST3_UID = -1;
    static String TEST3_KEY;
    static int ADMIN_UID = -1;
    static String ADMIN_KEY;
    static int COMMENTER_UID;
    static String COMMENTER_KEY;

    static int COMMENT_CONTENT_ID;
    static int COMMENT_TEST_ID;

    static CategoryObject[] categories;
    static ContentTypeObject[] contentTypes;
    static ContentObject[] content;

    static String ADMIN_USERNAME = "cory";
    static String ADMIN_PASSWORD = "3#$$$$$494949($(%*__''";
    static String ADMIN_EMAIL = "cory@whitespell.com";
    static String ADMIN_DEVICE_UUID = "cl2CJhwLrbE:APA91bEyu6SrBtC6GTCm8NM0Lq" +
            "K36og1PsAfGI3NolVxSOVFKsHKkwBtWXpbdS3l_Iqy8K" +
            "WKXUcVaX-eCHXj1dJA3xeveSTA_6J1l_IB65mdBGV6-Bma" +
            "up1ArvOWZo3QcOFM43TL2H0H";
    static String ADMIN_DEVICE_NAME = "cory's phone";
    static int ADMIN_DEVICE_TYPE = 1;

    static String SKYDIVER_USERNAME = "skydiver10";
    static String SKYDIVER_PASSWORD = "3#$$$$$494949($(%*__''";
    static String SKYDIVER_EMAIL = "skydiver10@testy.com";
    static int SKYDIVER_UID;
    static String SKYDIVER_KEY;

    static String ROLLERSKATER_USERNAME = "rollerskater10";
    static String ROLLERSKATER_PASSWORD = "3#$$$$$494949($(%*__''";
    static String ROLLERSKATER_EMAIL = "rollerskater10@testy.com";
    static int ROLLERSKATER_UID;

    static String API = null;

    Gson g = new Gson();
    HttpResponse<String> stringResponse = null;
    HttpResponse<JsonNode> jsonResponse = null;


    @Test
    public void test0001_StartTests() throws Exception {

        // load the system with test properties
        Config.TESTING = true;
        Config.CONFIGURATION_FILE = "tests.prop";

        // start the server
        Server.start();

        API = "http://localhost:" + Config.API_PORT;

        //configure log4j debug output
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for (Logger logger : loggers) {
            logger.setLevel(Level.OFF);
        }

        Config.NOTIFICATION_TOGGLE = false;
    }

    @Test
    public void test0002_NewDatabase() throws IOException {


        if (Config.DB_USER.equals("testpeak")) { // ensure we are on the test server
            // truncate peak_ci_test_ddl

            /**
             * CREATING THE TEST DATABASE
             */
            try {
                StatementExecutor executor = new StatementExecutor("CREATE DATABASE " + TEST_DB_NAME + ";");
                executor.execute(ps -> ps.executeUpdate());
            } catch (SQLException e) {
                Logging.log("High", e);
            }

            /**
             * USING THE TEST DATABASE
             */

            try {
                StatementExecutor executor = new StatementExecutor("use " + TEST_DB_NAME + ";");
                executor.execute(ps -> ps.executeUpdate());
            } catch (SQLException e) {
                Logging.log("High", e);
            }

            /**
             * EXECUTING DDL ON TEST DATABASE
             */

            String[] queries = readFile("ddl/peakdev.sql", StandardCharsets.UTF_8).split(";");

            for (int i = 0; i < queries.length - 1; i++) {
                if (queries[i] == null || queries[i].length() < 2 || queries[i].isEmpty()) {
                    continue;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(queries[i]);
                    executor.execute(ps -> ps.executeUpdate());
                } catch (SQLException e) {
                    Logging.log("High", e);
                }
            }

            // set the current database to the new database and re-initialize the Pool
            Config.DB = TEST_DB_NAME;
            Pool.initializePool();

            /**
             * Add the order type and origin values to the test DB
             */

            String INSERT_ORDER_TYPE = "INSERT INTO "+TEST_DB_NAME+".`order_type`(`order_type_id`, `order_type_name`) VALUES(?,?)";
            String INSERT_ORDER_ORIGIN = "INSERT INTO "+TEST_DB_NAME+".`order_origin`(`order_origin_id`, `order_origin_name`) VALUES(?,?)";
            ArrayList<String> types = new ArrayList<>();
            types.add("bundle");
            types.add("subscription");
            ArrayList<String> origins = new ArrayList<>();
            origins.add("apple");
            origins.add("google");
            origins.add("web");
            final int[] count = {1};

            for (String s : types) {
                try {
                    StatementExecutor executor = new StatementExecutor(INSERT_ORDER_TYPE);
                    executor.execute(ps -> {
                        ps.setInt(1, count[0]);
                        ps.setString(2, s);

                        ps.executeUpdate();
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                    return;
                }
                count[0]++;
            }

            count[0] = 1;
            for (String s : origins) {
                try {
                    StatementExecutor executor = new StatementExecutor(INSERT_ORDER_ORIGIN);
                    executor.execute(ps -> {
                        ps.setInt(1, count[0]);
                        ps.setString(2, s);

                        ps.executeUpdate();
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                    return;
                }
                count[0]++;
            }
        }
    }


    @Test
    public void test0003_WaitForOnlineTest() {
        int attempts = 45;

        boolean isOnline = false;

        while (attempts > 0 && !isOnline) {
            try {
                isOnline = isOnline(API + "/monitoring/ping");
            } catch (Exception e) {
                Logging.log("Low", e);
            }
            System.out.println("Waiting for API to come online..... " + attempts + " attempts left..");
            attempts--;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test0004_ForceErrorTest() throws UnirestException {
        /**
         * Force an error
         */


        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/nosuchendpoint")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"testaccount1\",\n" +
                        "\"password\" : \"1234567\",\n" +
                        "\"email\" : \"test123\"\n" +
                        "}")
                .asString();


        ErrorObject e = g.fromJson(stringResponse.getBody(), ErrorObject.class);
        assertEquals("There was no endpoint found on this path. Make sure you're using the right method (GET,POST,etc.)", e.getErrorMessage());
        assertEquals("EndpointDispatcher", e.getClassName());
        assertEquals(404, e.getHttpStatusCode());
        assertEquals(126, e.getErrorId());
    }

    @Test
    public void test0005_CreateAccountTest() throws UnirestException {

        /**
         * Create the account we are testing with
         */
        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + TEST_USERNAME + "\",\n" +
                        "\"password\" : \"" + TEST_PASSWORD + "\",\n" +
                        "\"email\" : \"" + TEST_EMAIL + "\"\n" +
                        "}")
                .asString();

        /**
         * Create a second account to test following and content
         */
        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\",\n" +
                        "\"email\" : \"" + ROLLERSKATER_EMAIL + "\"\n" +
                        "}")
                .asString();

        /**
         * Create a third "admin" account to test newsfeed
         */
        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"coryqq\",\n" +
                        "\"password\" : \"qqqqqq\",\n" +
                        "\"email\" : \"coryqq@qq.qq\"\n" +
                        "}")
                .asString();

        /**
         * Authenticate first user we just created
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + TEST_USERNAME + "\",\n" +
                        "\"password\" : \"" + TEST_PASSWORD + "\"\n" +

                        "}")
                .asString();


        AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST_UID = a.getUserId();
        TEST_KEY = a.getKey();

        assertEquals(a.getUserId() > -1, true);

        /**
         * Authenticate first user we just created with email instead of username
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + TEST_EMAIL + "\",\n" +
                        "\"password\" : \"" + TEST_PASSWORD + "\",\n" +
                        "\"deviceUUID\" : \"unknown\",\n" +
                        "\"deviceName\" : \"test device\",\n" +
                        "\"deviceType\" : " + 1 + "\n" +
                        "}")
                .asString();


        AuthenticationObject b = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST_UID = b.getUserId();
        TEST_KEY = b.getKey();

        assertEquals(b.getUserId() > -1, true);

        /**
         * Authenticate the 2nd User with username
         * */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\"\n" +
                        "}")
                .asString();


        AuthenticationObject c = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST2_UID = c.getUserId();
        TEST2_KEY = c.getKey();

        assertEquals(c.getUserId() > -1, true);

        /**
         * Authenticate the 2nd User with email
         * */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + ROLLERSKATER_EMAIL + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\"\n" +
                        "}")
                .asString();


        AuthenticationObject d = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST2_UID = d.getUserId();
        TEST2_KEY = d.getKey();

        assertEquals(d.getUserId() > -1, true);

        /**
         * Authenticate third user we just created
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"coryqq\",\n" +
                        "\"password\" : \"qqqqqq\"\n" +
                        "}")
                .asString();


        AuthenticationObject e = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST3_UID = e.getUserId();
        TEST3_KEY = e.getKey();

        assertEquals(e.getUserId() > -1, true);

        /**
         * Get the UserObject from the users/userid endpoint
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject user = g.fromJson(stringResponse.getBody(), UserObject.class);

        assertEquals(user.getUserId(), TEST_UID);
        assertEquals(user.getUserName(), TEST_USERNAME);

        /**
         * Get the 2nd UserObject from the users/userid endpoint
         * */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        UserObject user2 = g.fromJson(stringResponse.getBody(), UserObject.class);

        assertEquals(user2.getUserId(), TEST2_UID);
        assertEquals(user2.getUserName(), ROLLERSKATER_USERNAME);
    }


    @Test
    public void test0006_CategoriesTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/categories")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"categoryName\": \"skydiving\",\n" +
                        "\"categoryThumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n" +
                        "}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/categories")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"categoryName\": \"roller-skating\",\n" +
                        "\"categoryThumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n" +
                        "}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/categories")
                .header("accept", "application/json")
                .asString();
        categories = g.fromJson(stringResponse.getBody(), CategoryObject[].class);
        assertEquals(categories.length, 2);
        assertEquals(categories[0].getCategoryName(), "skydiving");
        assertEquals(categories[1].getCategoryName(), "roller-skating");
    }

    @Test
    public void test0007_FollowCategoriesTest() throws UnirestException {

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/categories")
                .header("accept", "application/json")
                .asString();

        categories = g.fromJson(stringResponse.getBody(), CategoryObject[].class);
        assertEquals(categories[0].getCategoryFollowers(), 0);
        assertEquals(categories[1].getCategoryFollowers(), 0);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/categories")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();
        CategoryFollowAction.FollowCategoryActionObject h = g.fromJson(stringResponse.getBody(), CategoryFollowAction.FollowCategoryActionObject.class);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/categories")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();
        CategoryFollowAction.FollowCategoryActionObject f = g.fromJson(stringResponse.getBody(), CategoryFollowAction.FollowCategoryActionObject.class);

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/categories")
                .header("accept", "application/json")
                .asString();

        categories = g.fromJson(stringResponse.getBody(), CategoryObject[].class);
        assertEquals(categories[0].getCategoryFollowers(), 1);
        assertEquals(categories[1].getCategoryFollowers(), 1);

        //todo (pim) get categories_following from user object and test whether they are skydivign and rollerskating
    }

    @Test
    public void test0008_CreatePublishers() throws UnirestException {
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + SKYDIVER_USERNAME + "\",\n" +
                        "\"password\" : \"" + SKYDIVER_PASSWORD + "\",\n" +
                        "\"email\" : \"" + SKYDIVER_EMAIL + "\"\n" +
                        "}")
                .asString();

        UserObject skydiver = g.fromJson(stringResponse.getBody(), UserObject.class);
        SKYDIVER_UID = skydiver.getUserId();

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + SKYDIVER_USERNAME + "\",\n" +
                        "\"password\" : \"" + SKYDIVER_PASSWORD + "\",\n" +
                        "\"deviceUUID\" : \"unknown\",\n" +
                        "\"deviceName\" : \"test device\",\n" +
                        "\"deviceType\" : " + 1 + "\n" +
                        "}")
                .asString();
        AuthenticationObject ao = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        SKYDIVER_KEY = ao.getKey();

        //todo(pim) authenticate as user
        //todo(pim) safeCall to publish in this category
    }

    @Test
    public void test0009_ContentTypesTest() throws UnirestException {

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"contentTypeName\": \"youtube\"\n"
                        + "}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"contentTypeName\": \"bundle\"\n"
                        + "}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .asString();

        contentTypes = g.fromJson(stringResponse.getBody(), ContentTypeObject[].class);
        assertEquals(contentTypes.length, 2);
        assertEquals(contentTypes[1].getContentTypeName(), "youtube");
        assertEquals(contentTypes[0].getContentTypeName(), "bundle");

        StaticRules.BUNDLE_CONTENT_TYPE = contentTypes[0].getContentTypeId();
        StaticRules.PLATFORM_UPLOAD_CONTENT_TYPE = contentTypes[1].getContentTypeId(); // todo(do a video with peak content type and set this to the actual one)
    }

    @Test
    public void test0010_FollowTest() throws UnirestException {
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"followingId\": \"" + TEST2_UID + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();

        UserFollowAction.FollowActionObject b = g.fromJson(stringResponse.getBody(), UserFollowAction.FollowActionObject.class);
        assertEquals(b.getActionTaken(), "followed");

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"followingId\": \"" + TEST_UID + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();

        UserFollowAction.FollowActionObject c = g.fromJson(stringResponse.getBody(), UserFollowAction.FollowActionObject.class);
        assertEquals(c.getActionTaken(), "followed");
    }

    @Test
    public void test0011_ContentTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"We have excuse-proofed your fitness routine with our latest Class FitSugar.\",\n" +
                        "\"contentTitle\": \"10-Minute No-Equipment Home Workout\",\n" +
                        "\"contentUrl\": \"doesnt matter\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumbguy.com\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"This one's hot!\",\n" +
                        "\"contentTitle\": \"Another Video!\",\n" +
                        "\"contentUrl\": \"doesnt matter\"," +
                        "\"contentPrice\": 0," +
                        "\"thumbnailUrl\": \"thumbguy.com\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"content2\",\n" +
                        "\"contentTitle\": \"content2\",\n" +
                        "\"contentUrl\": \"doesnt matter\"," +
                        "\"contentPrice\": 0," +
                        "\"thumbnailUrl\": \"thumbguy.com\"" +
                        "\n}")
                .asString();



        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?userId=" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl() != null, true);
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumbguy.com");
        assertEquals(content[0].getContentPrice(), 1.99, 0.0);
        assertEquals(content[0].getUserId(), TEST_UID);

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/add_child")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"childId\": \"" + content[1].getContentId() + "\"" +
                        "\n}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content[0].getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentId(), content[0].getContentId());
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl() != null, true);
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumbguy.com");
        assertEquals(content[0].getContentPrice(), 1.99, 0.0);
        assertEquals(content[0].getUserId(), TEST_UID);

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentType=" + contentTypes[0].getContentTypeId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl() != null, true);
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumbguy.com");
        assertEquals(content[0].getContentPrice(), 1.99, 0.0);
        assertEquals(content[0].getUserId(), TEST_UID);

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?categoryId=" + categories[0].getCategoryId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl() != null, true);
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumbguy.com");
        assertEquals(content[0].getContentPrice(), 1.99, 0.0);
        assertEquals(content[0].getUserId(), TEST_UID);

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl() != null, true);
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getContentPrice(), 1.99, 0.0);
        assertEquals(content[0].getThumbnailUrl(), "thumbguy.com");

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl(), null);
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumbguy.com");
        assertEquals(content[0].getContentPrice(), 1.99, 0.0);
        assertEquals(content[0].getUserId(), TEST_UID);
    }

    @Test
    public void test0012_getPublishersByCategory() {
        //todo(pim) do a search on /users where publishing list contains numbers category[0] and category[1] and output as json
        // then follow these users
        // then post content as the publishers
        // then generate newsfeed based on their content
    }

    @Test
    public void test0013_incurCreateAccountErrors() {
        //todo(pim) create accounts with usernames and too long strings that are already taken and should give us errors
    }

    @Test
    public void test0014_EditUser() throws UnirestException {

        /**
         * Currently the response for this object is only the values the user updated. This is to avoid an additional
         * get of the user's current fields.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        /**
         * Change only thumbnail
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"thumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n}")
                .asString();
        UserObject userEdit = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userEdit.getUserId(), TEST_UID);
        assertEquals(userEdit.getThumbnail(), "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO");

        /**
         * Change only cover_photo
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"coverPhoto\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n}")
                .asString();
        UserObject userEdit2 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userEdit2.getCoverPhoto(), "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO");


        /**
         * Change multiple fields
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"userName\": \"p1mw1n\",\n" +
                        "\"displayName\": \"new\",\n" +
                        "\"coverPhoto\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\",\n" +
                        "\"slogan\": \"slogan\"\n" +
                        "}")
                .asString();
        UserObject userEdit3 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userEdit3.getUserName(), "p1mw1n");
        assertEquals(userEdit3.getDisplayName(), "new");
        assertEquals(userEdit3.getCoverPhoto(), "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO");
        assertEquals(userEdit3.getSlogan(), "slogan");

        /**
         * Change only username
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"userName\": \"evenneweruser\"\n}")
                .asString();
        UserObject userEdit4 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userEdit4.getUserId(), TEST_UID);
        assertEquals(userEdit4.getUserName(), "evenneweruser");

        /**
         * Change username and slogan
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"userName\": \"evenneweruser2\",\n" +
                        "\"slogan\": \"slogan\"\n" +
                        "}")
                .asString();
        TEST_USERNAME = "evenneweruser2";
        UserObject userEdit5 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userEdit5.getUserId(), TEST_UID);
        assertEquals(userEdit5.getUserName(), "evenneweruser2");
        assertEquals(userEdit5.getSlogan(), "slogan");
    }

    @Test
    public void test0015_EditSettings() throws UnirestException {

        /**
         * Currently the response for this object is only the values the user updated. This is to avoid an additional
         * get of the user's current fields.
         */
        Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        /**
         * Change only email
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"" + TEST_PASSWORD + "\",\n" +
                        "\"email\": \"newtestemail@lol.com\"\n" +
                        "}")
                .asString();
        UserObject user = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user.getEmail(), "newtestemail@lol.com");

        /**
         * Change only password
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"" + TEST_PASSWORD + "\",\n" +
                        "\"newPassword\": \"!@#$%^&*()~\",\n" +
                        "\"email\": \"newtestemail2@lol.com\"\n" +
                        "}")
                .asString();
        UserObject user2 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user2.getEmail(), "newtestemail2@lol.com");

        /**
         * Change both email & password back to the previous values to ensure old values will work
         **/
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"!@#$%^&*()~\",\n" +
                        "\"newPassword\": \"" + TEST_PASSWORD + "\",\n" +
                        "\"email\": \"newtestemail@lol.com\"\n" +
                        "}")
                .asString();
        UserObject user3 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user3.getEmail(), "newtestemail@lol.com");

        /**
         * Test changing publisher value along with other fields.
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"" + TEST_PASSWORD + "\",\n" +
                        "\"newPassword\": \"!@#$%^&*()~\",\n" +
                        "\"email\": \"newtestemail2@lol.com\",\n" +
                        "\"publisher\": " + 0 + "\n" +
                        "}")
                .asString();

        UserObject user4 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user4.getEmail(), "newtestemail2@lol.com");
        assertEquals(user4.getPublisher(), 0);

        /**
         * Change publisher value back to ensure value can alternate.
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"!@#$%^&*()~\",\n" +
                        "\"newPassword\": \"newestpass\",\n" +
                        "\"email\": \"newestemail@email.com\",\n" +
                        "\"publisher\": " + 1 + "\n" +
                        "}")
                .asString();
        TEST_PASSWORD = "newestpass";
        UserObject user5 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user5.getEmail(), "newestemail@email.com");
        assertEquals(user5.getPublisher(), 1);
    }


    @Test
    public void test0016_Search() throws UnirestException {

        //assertEquals(true,false); //purposely fail test to test jenkins

        /**
         * Test search for the content we've added.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/search?q=excuse")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        assertEquals(stringResponse.getBody().contains("10-Minute No-Equipment Home Workout"), true);

        /**
         * Test search for the user we've added.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/search?q=even")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        assertEquals(stringResponse.getBody().contains("evenneweruser2"), true);

        /**
         * Test search for the category we've added.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/search?q=roller")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        assertEquals(stringResponse.getBody().contains("[" + categories[1].getCategoryId() + "]"), true);
    }

    @Test
    public void test0017_GetUserFollowingAndFollowers() throws UnirestException {

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"followingId\": \"" + SKYDIVER_UID + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();

        UserFollowAction.FollowActionObject a = g.fromJson(stringResponse.getBody(), UserFollowAction.FollowActionObject.class);
        assertEquals(a.getActionTaken(), "followed");

        /**
         * List following for user that followed other users previously
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/?includeFollowing=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject userThatFollows = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userThatFollows.getUserFollowing().get(0).intValue(), TEST2_UID);
        assertEquals(userThatFollows.getUserFollowing().get(1).intValue(), SKYDIVER_UID);

        /**
         * List followers for user that has been followed by other users
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/?includeFollowers=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject userThatIsFollowed = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userThatIsFollowed.getUserFollowers().get(0).intValue(), TEST2_UID);
    }

    @Test
    public void test0018_GetCategoryFollowing() throws UnirestException {

        /**
         * List categories user is following
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/?includeCategories=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject userThatFollows = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userThatFollows.getCategoryFollowing().get(0).intValue(), categories[0].getCategoryId());
        assertEquals(userThatFollows.getCategoryFollowing().get(1).intValue(), categories[1].getCategoryId());
    }

    @Test
    public void test0019_GetUsers() throws UnirestException {

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/?offset=1&limit=50")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject[] a = g.fromJson(stringResponse.getBody(), UserObject[].class);
        assertEquals(a[0].getUserId(), SKYDIVER_UID);
        assertEquals(a[1].getUserId(), TEST3_UID);
    }

    @Test
    public void test0020_GetNewsfeed() throws UnirestException {

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/newsfeed/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        System.out.println(stringResponse.getBody());

        NewsfeedObject[] n = g.fromJson(stringResponse.getBody(), NewsfeedObject[].class);

        for (int i = 0; i < n.length; i++) {
            if (i == 0) {
                System.out.println(content[0].getContentId());
                System.out.println(content[1].getContentId());
                System.out.println(content[2].getContentId());

                assertEquals(n[i].getNewsfeedContent().getContentId(), content[0].getContentId());
                assertEquals(n[i].getNewsfeedContent().getPoster().getUserId(), TEST_UID);
                assertEquals(n[i].getNewsfeedContent().getContentTitle(), "10-Minute No-Equipment Home Workout");
            }
        }

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"new content for bundle update test\",\n" +
                        "\"contentTitle\": \"testerino\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=82377fhU\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumbguy.com\"" +
                        "\n}")
                .asString();
        System.out.println(stringResponse.getBody());
        ContentObject child = g.fromJson(stringResponse.getBody(), ContentObject.class);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/add_child")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"childId\": \"" + child.getContentId() + "\"" +
                        "\n}")
                .asString();

        System.out.println(stringResponse.getBody());

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/newsfeed/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        /**
         * Check that first item of newsfeed is now the updated bundle. Also ensure bundle contains new content.
         * New newsfeedId = the updated child to maintain order of newsfeed and offset.
         */
        NewsfeedObject[] n2 = g.fromJson(stringResponse.getBody(), NewsfeedObject[].class);
        assertEquals(n2[0].getNewsfeedId(), child.getContentId());
        assertEquals(n2[0].getNewsfeedContent().getPoster().getUserId(), TEST_UID);
        assertEquals(n2[0].getNewsfeedContent().getChildren().get(1).getContentTitle(), "testerino");
    }

    @Test
    public void test0021_EnsureContentPostersAreCategoryPublishers() throws UnirestException {

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "?includePublishing=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject user = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user.getCategoryPublishing().get(0).intValue(), categories[0].getCategoryId());

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "?includePublishing=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        UserObject user2 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user2.getCategoryPublishing().get(0).intValue(), categories[1].getCategoryId());
    }

    @Test
    public void test0022_TrendingPublishingUsers() throws UnirestException {

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/trending")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        Trending.TrendingResponse trending = g.fromJson(stringResponse.getBody(), Trending.TrendingResponse.class);
        assertEquals(trending.users.get(0).getUserId(), TEST_UID);
        assertEquals(trending.users.get(1).getUserId(), TEST2_UID);
    }

    @Test
    public void test0023_AddAndGetSavedContent() throws UnirestException {


        /**
         * Add content to saved content list
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/saved")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"contentId\": \"" + content[0].getContentId() + "\",\n" +
                        "\"action\": \"save\"\n" +
                        "}")
                .asString();

        SavedContentAction.SavedContentActionResponse add = g.fromJson(stringResponse.getBody(), SavedContentAction.SavedContentActionResponse.class);
        assertEquals(add.getAddedContentId(), content[0].getContentId());
        assertEquals(add.isSuccess(), true);

        /**
         * Check that content was added
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/saved")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        GetUserSavedContent.getSavedContent get = g.fromJson(stringResponse.getBody(), GetUserSavedContent.getSavedContent.class);
        assertEquals(get.getSavedContent().get(0).getContentId(), content[0].getContentId());

        /**
         * Try removing the saved content
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/saved")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"contentId\": \"" + content[0].getContentId() + "\",\n" +
                        "\"action\": \"unsave\"\n" +
                        "}")
                .asString();
        SavedContentAction.SavedContentActionResponse remove = g.fromJson(stringResponse.getBody(), SavedContentAction.SavedContentActionResponse.class);
        assertEquals(remove.getRemovedContentId(), content[0].getContentId());
        assertEquals(remove.isSuccess(), true);

        /**
         * Check that saved content was removed
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/saved")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        assertEquals(stringResponse.getBody(), "{\"savedContent\":[]}");
    }


    @Test
    public void test0024_AddAndGetContentComments() throws UnirestException {

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/comments")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST_UID + "\",\n" +
                        "\"comment\": \"awesome video!\"\n" +
                        "}")
                .asString();

        AddContentComment.AddContentCommentObject add = g.fromJson(stringResponse.getBody(), AddContentComment.AddContentCommentObject.class);
        assertEquals(add.isCommentAdded(), true);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/comments")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST2_UID + "\",\n" +
                        "\"comment\": \"wow this is so cool! definitely going to try it :)!\"\n" +
                        "}")
                .asString();
        AddContentComment.AddContentCommentObject add2 = g.fromJson(stringResponse.getBody(), AddContentComment.AddContentCommentObject.class);
        assertEquals(add2.isCommentAdded(), true);

        /**
         * Ensure last comment is posted after the first 2
         */
        try {
            Thread.currentThread().sleep(1000);
        } catch (Exception e) {
            System.out.println("caught");
        }

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/comments")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST2_UID + "\",\n" +
                        "\"comment\": \"wow another comment!\"\n" +
                        "}")
                .asString();
        AddContentComment.AddContentCommentObject add3 = g.fromJson(stringResponse.getBody(), AddContentComment.AddContentCommentObject.class);
        assertEquals(add3.isCommentAdded(), true);


        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/comments")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        CommentObject[] comments = g.fromJson(stringResponse.getBody(), CommentObject[].class);
        assertEquals(comments[0].getComment(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(comments[1].getComment(), "awesome video!");
        assertEquals(comments[2].getComment(), "wow this is so cool! definitely going to try it :)!");
        assertEquals(comments[3].getComment(), "wow another comment!");
        assertEquals(comments[0].getTimestamp().before(comments[2].getTimestamp()), true);

        /**
         * Save this uid and key to test comments in deleteTest, (new users are created and assigned TEST2 uid and key)
         */
        COMMENTER_UID = TEST2_UID;
        COMMENTER_KEY = TEST2_KEY;
        COMMENT_CONTENT_ID = content[0].getContentId();
        COMMENT_TEST_ID = comments[2].getCommentId();
    }


    @Test
    public void test0025_TestMandrillEmailsAndTokens() throws UnirestException {

        EmailSend.tokenResponseObject et = EmailSend.updateDBandSendWelcomeEmail(ROLLERSKATER_USERNAME, ADMIN_EMAIL);
        if (et != null) {
            /**
             * Check that the email expiration and verification got updated after sending the email.
             */
            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/email")
                    .header("accept", "application/json")
                    .asString();

            GetEmailVerification.GetEmailVerificationResponse evr = g.fromJson(stringResponse.getBody(), GetEmailVerification.GetEmailVerificationResponse.class);
            if (evr.getEmailExpiration() == null || evr.getEmailVerified() == 1) {
                //force fail
                assertEquals(false, true);
            }
            assertEquals(evr.getEmailVerified(), 0);

            /**
             * User verifies their email
             */
            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/email")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"userName\": \"" + ROLLERSKATER_USERNAME + "\",\n" +
                            "\"emailToken\": \"" + et.getEmailToken() + "\"\n" +
                            "}")
                    .asString();

            UserObject userObject = g.fromJson(stringResponse.getBody(), UserObject.class);
            assertEquals(userObject.getEmailVerified(), 1);

            /**
             * Email is not yet verified, resend the email verification to the user (requested through the app)
             */
            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/resendemail")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"email\": \"" + ROLLERSKATER_EMAIL + "\"\n" + "}")
                    .asString();
            ResendEmailVerification.EmailVerificationSentStatus status = g.fromJson(stringResponse.getBody(), ResendEmailVerification.EmailVerificationSentStatus.class);
            assertEquals(status.isSent(), true);

            /**
             * Check that the email expiration and verification got updated after resending the email.
             */
            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/email")
                    .header("accept", "application/json")
                    .asString();

            GetEmailVerification.GetEmailVerificationResponse evr2 = g.fromJson(stringResponse.getBody(), GetEmailVerification.GetEmailVerificationResponse.class);
            if (evr2.getEmailExpiration() == null || evr2.getEmailVerified() == 1) {
                //force fail
                assertEquals(false, true);
            }
            assertEquals(evr2.getEmailVerified(), 0);
        }

        EmailSend.tokenResponseObject et2 = EmailSend.updateDBandSendResetEmail(ROLLERSKATER_USERNAME, ADMIN_EMAIL);
        if (et2 != null) {
            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/reset")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"userName\": \"" + ROLLERSKATER_USERNAME + "\",\n" +
                            "\"newPassword\": \"qqqqqq\",\n" +
                            "\"resetToken\": \"" + et2.getEmailToken() + "\"\n" +
                            "}")
                    .asString();
            ResetPassword.resetSuccessObject rs2 = g.fromJson(stringResponse.getBody(), ResetPassword.resetSuccessObject.class);
            assertEquals(rs2.isSuccess(), true);

            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"userName\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
                            "\"password\" : \"qqqqqq\"\n" +
                            "}")
                    .asString();
            AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
            int tempId = a.getUserId();
            String tempKey = a.getKey();

            ROLLERSKATER_PASSWORD = "qqqqqq";
            assertEquals(tempId > -1, true);
            assertEquals(tempKey != null, true);
        }

        /**
         * Test for the content follower notification email... since it does not affect the DB, I used mandrill to ensure
         * the emails were sent properly.
         */
    }

    @Test
    public void test0026_ContentLikeAction() throws UnirestException {

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        ContentObject c[] = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(c[0].getLikes(), 0);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/likes")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST_UID + "\",\n" +
                        "\"action\": \"like\"\n" +
                        "}")
                .asString();
        ContentLikeAction.LikeActionObject la = g.fromJson(stringResponse.getBody(), ContentLikeAction.LikeActionObject.class);
        assertEquals(la.getActionTaken(), "like");

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        ContentObject c2[] = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(c2[0].getLikes(), 1);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/likes")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST2_UID + "\",\n" +
                        "\"action\": \"like\"\n" +
                        "}")
                .asString();
        ContentLikeAction.LikeActionObject la2 = g.fromJson(stringResponse.getBody(), ContentLikeAction.LikeActionObject.class);
        assertEquals(la2.getActionTaken(), "like");

        /**
         * Test for userLiked
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        ContentObject c3[] = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(c3[0].getLikes(), 2);
        assertEquals(c3[0].getUserLiked(), 1);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId() + "/likes")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST_UID + "\",\n" +
                        "\"action\": \"unlike\"\n" +
                        "}")
                .asString();
        ContentLikeAction.LikeActionObject la3 = g.fromJson(stringResponse.getBody(), ContentLikeAction.LikeActionObject.class);
        assertEquals(la3.getActionTaken(), "unlike");

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        ContentObject c4[] = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(c4[0].getLikes(), 1);
    }

    @Test
    public void test0027_ContentCurationTest() throws UnirestException {

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/contentcurated")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"We have excuse-proofed your fitness routine with our latest Class FitSugar.\",\n" +
                        "\"contentTitle\": \"10-Minute No-Equipment Home Workout\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=I6t0quh8Ick\"," +
                        "\"thumbnailUrl\": \"thumburl.com\"," +
                        "\"accepted\": 1" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/contentcurated")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"This one's hot!\",\n" +
                        "\"contentTitle\": \"Another Video!\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=827377fhU\"," +
                        "\"thumbnailUrl\": \"thumbguy.com\"," +
                        "\"accepted\": 1" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/contentcurated")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"content2\",\n" +
                        "\"contentTitle\": \"content2\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=content2\"," +
                        "\"thumbnailUrl\": \"thumb.com\"," +
                        "\"accepted\": 1" +
                        "\n}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/contentcurated")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl(), "https://www.youtube.com/watch?v=I6t0quh8Ick");
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumburl.com");

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/contentcurated?userId=" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl(), "https://www.youtube.com/watch?v=I6t0quh8Ick");
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumburl.com");
        assertEquals(content[0].getUserId(), TEST_UID);

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/contentcurated?categoryId=" + categories[0].getCategoryId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getCategoryId(), categories[0].getCategoryId());
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl(), "https://www.youtube.com/watch?v=I6t0quh8Ick");
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
        assertEquals(content[0].getThumbnailUrl(), "thumburl.com");
        assertEquals(content[0].getUserId(), TEST_UID);

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/contentcurated?categoryId=" + categories[1].getCategoryId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getCategoryId(), categories[1].getCategoryId());
        assertEquals(content[0].getContentType(), contentTypes[1].getContentTypeId());
        assertEquals(content[0].getContentTitle(), "content2");
        assertEquals(content[0].getContentUrl(), "https://www.youtube.com/watch?v=content2");
        assertEquals(content[0].getContentDescription(), "content2");
        assertEquals(content[0].getThumbnailUrl(), "thumb.com");
        assertEquals(content[0].getUserId(), TEST2_UID);
    }

    @Test
    public void test0028_FacebookLoginTest() throws UnirestException {
        TestUser user, user2, user3;
        /**
         * Authenticate FB API and create our testUsers
         */
        try {
            //version v2.4
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthAppId(Config.FB_APP_ID)
                    .setOAuthAppSecret(Config.FB_APP_SECRET)
                    .setOAuthAccessToken(Config.FB_APP_ACCESS_TOKEN)
                    .setOAuthPermissions("email,public_profile");
            FacebookFactory ff = new FacebookFactory(cb.build());
            Facebook facebook = ff.getInstance();

            user = facebook.createTestUser(Config.FB_APP_ID);
            user2 = facebook.createTestUser(Config.FB_APP_ID);
            user3 = facebook.createTestUser(Config.FB_APP_ID);
        } catch (Exception e) {
            Logging.log("High", e);
            return;
        }

        /**
         * Test login with three different test users using the test accessTokens
         */

        /**
         * iOS device
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/facebook")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\"accessToken\":\"" + user.getAccessToken() + "\"," +
                        "\"deviceName\":\"iPhone 5s\",\n" +
                        "\"deviceUUID\":\"eeaaa8d930919a6fc7675447ebacd0355dff2cd10f8b3b40aed1b7ac87383c10\",\n" +
                        "\"deviceType\":0\n" +
                        "}")
                .asString();
        AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        System.out.println(stringResponse.getBody());
        Integer testId = a.getUserId();
        String testKey = a.getKey();
        assertEquals(testId > 0, true);
        assertEquals(testKey != null, true);

        /**
         * Android device
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/facebook")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\"accessToken\":\"" + user2.getAccessToken() + "\"," +
                        "\"deviceName\":\"Galaxy 6\",\n" +
                        "\"deviceUUID\":\"CAAGbnO3h2g0BAGDu2NPSNNZA3shcsZCWJGgr" +
                        "aC74mM29DXQfRUSxzCGPbZA94udKkWdUBOXKvWHrAycEAtbMTmXAt9td" +
                        "0ZAM8qkP5kXCsiQbycWpj0JI7FVYeeLaUKxZAFCLT1SIDQ6YCJZ" +
                        "AUP0RKj8CZAXoa6ekYEX8gh1Xp5Ng0bdSvsUeuq48zRteNDwWWi" +
                        "xFSLhe6f4f7iqK4Ko4fQCrihuMtT99ITpLeXsALiLKwZDZD\",\n" +
                        "\"deviceType\":\"1\"\n" +
                        "}")
                .asString();
        AuthenticationObject a2 = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        Integer testId2 = a2.getUserId();
        String testKey2 = a2.getKey();
        assertEquals(testId2 > 0, true);
        assertEquals(testKey2 != null, true);

        /**
         * No device information provided
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/facebook")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\"accessToken\":\"" + user3.getAccessToken() + "\"" +
                        "}")
                .asString();
        AuthenticationObject a3 = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        Integer testId3 = a3.getUserId();
        String testKey3 = a3.getKey();
        assertEquals(testId3 > 0, true);
        assertEquals(testKey3 != null, true);
    }

    @Test
    public void test0029_FeedbackAndReportingTest() throws UnirestException {

        /**
         * Test feedback with different users
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/feedback")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"email\": \"" + TEST_EMAIL + "\",\n" +
                        "\"message\": \"Peak is so awesome! Use it every day during my workout :)\"" +
                        "\n}")
                .asString();
        SendFeedback.feedbackSuccessObject a = g.fromJson(stringResponse.getBody(), SendFeedback.feedbackSuccessObject.class);
        assertEquals(a.isSuccess(), true);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/feedback")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"email\": \"" + ROLLERSKATER_EMAIL + "\",\n" +
                        "\"message\": \"I like the app, but the content needs improvement.\"" +
                        "\n}")
                .asString();
        SendFeedback.feedbackSuccessObject b = g.fromJson(stringResponse.getBody(), SendFeedback.feedbackSuccessObject.class);
        assertEquals(b.isSuccess(), true);

        /**
         * Add new reporting types
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/reporting/types")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"reportingTypeName\": \"Fake profile picture\"\n" +
                        "\n}")
                .asString();
        AddReportingType.AddReportingTypeObject success = g.fromJson(stringResponse.getBody(), AddReportingType.AddReportingTypeObject.class);
        assertEquals(success.isReportingTypeAdded(), true);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/reporting/types")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"reportingTypeName\": \"Offensive comments\"\n" +
                        "\n}")
                .asString();
        success = g.fromJson(stringResponse.getBody(), AddReportingType.AddReportingTypeObject.class);
        assertEquals(success.isReportingTypeAdded(), true);

        /**
         * Ensure reporting types exist and can be retrieved
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/reporting/types")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        ReportingTypeObject types[] = g.fromJson(stringResponse.getBody(), ReportingTypeObject[].class);
        assertEquals(types[0].getReportingTypeName() != null, true);
        assertEquals(types[1].getReportingTypeName() != null, true);

        /**
         * Test reporting with different users
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/reporting")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"reportedUserId\": \"" + TEST2_UID + "\",\n" +
                        "\"typeId\": " + types[0].getReportingTypeId() + "," +
                        "\"message\": \"fake profile pic...\"" +
                        "\n}")
                .asString();
        ReportUser.reportUserSuccessObject c = g.fromJson(stringResponse.getBody(), ReportUser.reportUserSuccessObject.class);
        assertEquals(c.isSuccess(), true);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/reporting")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"reportedUserId\": \"" + TEST_UID + "\",\n" +
                        "\"typeId\": " + types[1].getReportingTypeId() + "," +
                        "\"message\": \"Being offensive in comments on Lebron's video\"" +
                        "\n}")
                .asString();
        ReportUser.reportUserSuccessObject d = g.fromJson(stringResponse.getBody(), ReportUser.reportUserSuccessObject.class);
        assertEquals(d.isSuccess(), true);
    }


    @Test
    public void test0030_MakeBundleTest() throws UnirestException {

        // create the root bundle
        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +//contentypes[0] is alwasy bundle
                        "\"contentDescription\": \"This is the root bundle.\",\n" +
                        "\"contentTitle\": \"Root bundle\",\n" +
                        "\"contentUrl\": \"doesnt matter\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumburl.com\"" +
                        "\n}")
                .asString();

        // test youtube video to be added into the bundle

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"This is the root bundle.\",\n" +
                        "\"contentTitle\": \"Child 1 of root bundle (yt video)\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=8277fhU\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumburl.com\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +//contentypes[0] is alwasy bundle
                        "\"contentDescription\": \"This is the root bundle.\",\n" +
                        "\"contentTitle\": \"Child 2 of root bundle (yt video)\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=8273hU\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumburl.com\"" +
                        "\n}")
                .asString();

        // add a child bundle
        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +//contentypes[0] is alwasy bundle
                        "\"contentDescription\": \"This is the root bundle.\",\n" +
                        "\"contentTitle\": \"Child bundle\",\n" +
                        "\"contentUrl\": \"doesnt matter\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumburl.com\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +//contentypes[0] is alwasy bundle
                        "\"contentDescription\": \"This is the root bundle.\",\n" +
                        "\"contentTitle\": \"Video of Child bundle\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=77fhU\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumburl.com\"" +
                        "\n}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentType=" + contentTypes[0].getContentTypeId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        // index 0 should be the root bundle, 1 should be the child bundle
        ContentObject bundles[] = g.fromJson(stringResponse.getBody(), ContentObject[].class);

        // add the child bundle as a child to the root bundle

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + bundles[0].getContentId() + "/add_child")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"childId\": \"" + bundles[1].getContentId() + "\"" +
                        "\n}")
                .asString();


        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentType=" + contentTypes[1].getContentTypeId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        // get 3 random uyt videos to place inside the bundles
        ContentObject videos[] = g.fromJson(stringResponse.getBody(), ContentObject[].class);

        // add 2 videos to root bundle, and 1 to child bundle


        Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + bundles[0].getContentId() + "/add_child")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"childId\": \"" + videos[0].getContentId() + "\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + bundles[0].getContentId() + "/add_child")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"childId\": \"" + videos[1].getContentId() + "\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + bundles[0].getContentId() + "/add_child")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"childId\": \"" + bundles[1].getContentId() + "\"" +
                        "\n}")
                .asString();


        Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + bundles[1].getContentId() + "/add_child")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"childId\": \"" + videos[2].getContentId() + "\"" +
                        "\n}")
                .asString();


        /**
         * Test for userAccess in bundles
         */

        // grab /content/bundles[0].getContentId() and check if it has 4 children, check child with content id bundles[1].getContentId() for 1 child
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + bundles[0].getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        System.out.println("userAccess in bundles: " + stringResponse.getBody());

        ContentObject[] content1 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content1[0].hasAccess(), 1);
        assertEquals(content1[0].getContentUrl() != null, true);

        /**
         * Ensure poster has access to this paid content
         */
        for (ContentObject c : content1[0].getChildren()) {
            if (c.getUserId() == TEST_UID) {


                System.out.println("poster: " + c.getUserId() + " TEST: " +TEST_UID);

                assertEquals(c.hasAccess(), 1);
                assertEquals(c.getContentUrl() != null, true);
            }
        }

        // get 3 random uyt videos to place inside the bundles
        ContentObject[] finalBundleResponseArray = g.fromJson(stringResponse.getBody(), ContentObject[].class);

        ContentObject finalBundleResponse = finalBundleResponseArray[0]; // always the first index bc we're sorting based on content id, and thats unique

        /**
         * Purchase the content on client side, then update the user's access. In this case we
         * purchase the entire bundle and then check if all the content is accessible.
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/access")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"contentId\": \"" + finalBundleResponse.getContentId() + "\"\n" +
                        "\n}")
                .asString();
        System.out.println("bundleId: " +finalBundleResponse.getContentId());
        GrantContentAccess.ContentAccessResponse hasAccess = g.fromJson(stringResponse.getBody(), GrantContentAccess.ContentAccessResponse.class);
        System.out.println(stringResponse.getBody());
        assertEquals(hasAccess.isSuccess(), true);

        /**
         * Check all the content inside the bundle is accessible
         */
        for (ContentObject c : finalBundleResponse.getChildren()) {
            System.out.println(c.getContentId() + "---" + c.getContentDescription());

            /**
             * Check the content to ensure we have access as this user and the url is displayed
             */
            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + c.getContentId())
                    .header("accept", "application/json")
                    .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                    .asString();

            ContentObject[] content3 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
            assertEquals(content3[0].hasAccess(), 1);
            assertEquals(content3[0].getUserId(), TEST_UID);
            if (content3[0].getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                assertEquals(content3[0].getContentUrl(), "doesnt matter");
            } else {
                assertEquals(content3[0].getContentUrl().equals("doesnt matter"), false);
            }
        }
        assertEquals(finalBundleResponse.getChildren().size(), 6);
    }


    @Test
    public void test0031_PushNotificationTest() throws UnirestException {

        /**
         * Test that notifications are updated in DB
         */

        /**
         * Create the admin and user accounts we are testing with
         */
        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + ADMIN_USERNAME + "\",\n" +
                        "\"password\" : \"" + ADMIN_PASSWORD + "\",\n" +
                        "\"email\" : \"" + ADMIN_EMAIL + "\"\n" +
                        "}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"test11\",\n" +
                        "\"password\" : \"test11\",\n" +
                        "\"email\" : \"test1@lol.com\"\n" +
                        "}")
                .asString();
        TEST_USERNAME = "test11";
        TEST_PASSWORD = TEST_USERNAME;

        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"test22\",\n" +
                        "\"password\" : \"test22\",\n" +
                        "\"email\" : \"test2@lol.com\"\n" +
                        "}")
                .asString();
        ROLLERSKATER_USERNAME = "test22";
        ROLLERSKATER_PASSWORD = ROLLERSKATER_USERNAME;

        /**
         * Authenticate the admin
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + ADMIN_USERNAME + "\",\n" +
                        "\"password\" : \"" + ADMIN_PASSWORD + "\",\n" +
                        "\"deviceUUID\" : \"" + ADMIN_DEVICE_UUID + "\",\n" +
                        "\"deviceName\" : \"" + ADMIN_DEVICE_NAME + "\",\n" +
                        "\"deviceType\" : " + ADMIN_DEVICE_TYPE + "\n" +
                        "}")
                .asString();
        AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        ADMIN_UID = a.getUserId();
        ADMIN_KEY = a.getKey();

        /**
         * Authenticate the other users with device info for testing
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + TEST_USERNAME + "\",\n" +
                        "\"password\" : \"" + TEST_PASSWORD + "\",\n" +
                        "\"deviceUUID\" : \"cz-HvHQ2oX8:APA91bGbM8F4HT0BOXdORmmu0xVYVM0RMhRGXOciUmG5V92H5v-1VuWY7Svj" +
                        "HHpOFOUeqATafD3CxPuqyzB_yg1TLAS2DlLoEGcUnsgBLW2knL-o1Q9e199hFu6eluexO8HainFRYTbW\",\n" +
                        "\"deviceName\" : \"otheruser2\",\n" +
                        "\"deviceType\" : " + 1 + "\n" +
                        "}")
                .asString();
        AuthenticationObject a2 = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST_UID = a2.getUserId();
        TEST_KEY = a2.getKey();

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"userName\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\",\n" +
                        "\"deviceUUID\" : \"99d82470564059f9c2e1918ffdb4a86a1869b5bf2fa7cffbadf897f553ef9c96\",\n" +
                        "\"deviceName\" : \"otheruser\",\n" +
                        "\"deviceType\" : " + 0 + "\n" +
                        "}")
                .asString();
        AuthenticationObject a3 = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST2_UID = a3.getUserId();
        TEST2_KEY = a3.getKey();

        /**
         * Upload a new category, all users get a notification
         */
        Unirest.post("http://localhost:" + Config.API_PORT + "/categories")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"categoryName\": \"yoga\",\n" +
                        "\"categoryThumbnail\": \"http://upperlimits.com/west-county/wp-content/uploads/2014/05/yoga.jpg\"\n" +
                        "}")
                .asString();

        /**
         * Have some users follow the admin
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"followingId\": \"" + ADMIN_UID + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();
        UserFollowAction.FollowActionObject b = g.fromJson(stringResponse.getBody(), UserFollowAction.FollowActionObject.class);
        assertEquals(b.getActionTaken(), "followed");

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"followingId\": \"" + ADMIN_UID + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();
        UserFollowAction.FollowActionObject c = g.fromJson(stringResponse.getBody(), UserFollowAction.FollowActionObject.class);
        assertEquals(c.getActionTaken(), "followed");

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"followingId\": \"" + ADMIN_UID + "\",\n" +
                        "\"action\": \"unfollow\"\n" +
                        "}")
                .asString();
        UserFollowAction.FollowActionObject d = g.fromJson(stringResponse.getBody(), UserFollowAction.FollowActionObject.class);
        assertEquals(d.getActionTaken(), "unfollowed");

        /**
         * Upload content as the admin
         */

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[0].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"admincontent\",\n" +
                        "\"contentTitle\": \"admincontent\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=admin\"," +
                        "\"contentPrice\": 1.99," +
                        "\"thumbnailUrl\": \"thumbadmin.com\"" +
                        "\n}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?userId=" + ADMIN_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        int contentId = content[0].getContentId();
        assertEquals(content[0].getContentPrice(), 1.99, 0.0);

        /**
         * Have both followers like the uploaded content
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + contentId + "/likes")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST_UID + "\",\n" +
                        "\"action\": \"like\"\n" +
                        "}")
                .asString();
        ContentLikeAction.LikeActionObject la = g.fromJson(stringResponse.getBody(), ContentLikeAction.LikeActionObject.class);
        assertEquals(la.getActionTaken(), "like");

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + contentId + "/likes")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST2_UID + "\",\n" +
                        "\"action\": \"like\"\n" +
                        "}")
                .asString();
        ContentLikeAction.LikeActionObject la1 = g.fromJson(stringResponse.getBody(), ContentLikeAction.LikeActionObject.class);
        assertEquals(la1.getActionTaken(), "like");

        /**
         * Have both followers comment on the uploaded content
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + contentId + "/comments")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST_UID + "\",\n" +
                        "\"comment\": \"awesome video!\"\n" +
                        "}")
                .asString();

        AddContentComment.AddContentCommentObject add = g.fromJson(stringResponse.getBody(), AddContentComment.AddContentCommentObject.class);
        assertEquals(add.isCommentAdded(), true);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + contentId + "/comments")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST2_UID + "\",\n" +
                        "\"comment\": \"wow this is so cool!\"\n" +
                        "}")
                .asString();

        AddContentComment.AddContentCommentObject add2 = g.fromJson(stringResponse.getBody(), AddContentComment.AddContentCommentObject.class);
        assertEquals(add2.isCommentAdded(), true);

        /**
         * Sleep to let the notifications go through (There are a lot of notifications...)
         */

        if (Config.NOTIFICATION_TOGGLE) {
            try {
                Thread.sleep(90000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Check admin user for follower notifications
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/notifications")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        GetUserNotifications.getUserNotificationsResponse notifications =
                g.fromJson(stringResponse.getBody(), GetUserNotifications.getUserNotificationsResponse.class);
        ArrayList<UserNotification> notif = notifications.getUserNotifications();
        if (notif != null) {
            for (UserNotification n : notif) {
                assertEquals(n.getNotificationStatus(), 1);
            }
        }

        /**
         * Check for notifications on both following users for uploaded content
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/notifications")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        GetUserNotifications.getUserNotificationsResponse notifications1 =
                g.fromJson(stringResponse.getBody(), GetUserNotifications.getUserNotificationsResponse.class);
        ArrayList<UserNotification> notif2 = notifications1.getUserNotifications();
        if (notif2 != null) {
            for (UserNotification n : notif2) {
                assertEquals(n.getNotificationStatus(), 1);
            }
        }

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/notifications")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();
        GetUserNotifications.getUserNotificationsResponse notifications2 =
                g.fromJson(stringResponse.getBody(), GetUserNotifications.getUserNotificationsResponse.class);
        ArrayList<UserNotification> notif3 = notifications2.getUserNotifications();
        if (notif3 != null) {
            for (UserNotification n : notif3) {
                assertEquals(n.getNotificationStatus(), 1);
            }
        }

        Config.NOTIFICATION_TOGGLE = false;
    }


    @Test
    public void test0032_UpdateContent() throws UnirestException {

        /**
         * Currently the response for this object is only the values the user updated. This is to avoid an additional
         * get of the content's current fields.
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        ContentObject objectToMorph = g.fromJson(stringResponse.getBody(), ContentObject.class);
        assertEquals(objectToMorph.getContentId(), content[0].getContentId());
        assertEquals(objectToMorph.getUserId(), ADMIN_UID);

        /**
         * Change only title
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + objectToMorph.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{\n\"contentTitle\": \"test_title\"" +

                        "}")
                .asString();

        assertEquals(stringResponse.getBody().contains("success"), true);

        /** Get the object again and check **/

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        objectToMorph = g.fromJson(stringResponse.getBody(), ContentObject.class);
        assertEquals(objectToMorph.getContentTitle(), "test_title");

        /** Update ALL values at once **/

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + objectToMorph.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{" +
                        "\n\"contentTitle\": \"title_test\"," +
                        "\n\"contentDescription\": \"description_test\"," +
                        "\n\"contentPrice\": 1.33," +
                        "\n\"categoryId\": "+categories[0].getCategoryId()+"," +
                        "\n\"contentUrl\": \"url_test\"," +

                        "\n\"contentUrl1080p\": \"url_1080p_test\"," +
                        "\n\"contentUrl720p\": \"url_720p_test\"," +
                        "\n\"contentUrl480p\": \"url_480p_test\"," +
                        "\n\"contentUrl360p\": \"url_360p_test\"," +
                        "\n\"contentUrl240p\": \"url_240p_test\"," +
                        "\n\"contentUrl144p\": \"url_144p_test\"," +

                        "\n\"contentPreview1080p\": \"preview_1080p_test\"," +
                        "\n\"contentPreview720p\": \"preview_720p_test\"," +
                        "\n\"contentPreview480p\": \"preview_480p_test\"," +
                        "\n\"contentPreview360p\": \"preview_360p_test\"," +
                        "\n\"contentPreview240p\": \"preview_240p_test\"," +
                        "\n\"contentPreview144p\": \"preview_144p_test\"," +

                        "\n\"thumbnail1080p\": \"thumbnail_1080p_test\"," +
                        "\n\"thumbnail720p\": \"thumbnail_720p_test\"," +
                        "\n\"thumbnail480p\": \"thumbnail_480p_test\"," +
                        "\n\"thumbnail360p\": \"thumbnail_360p_test\"," +
                        "\n\"thumbnail240p\": \"thumbnail_240p_test\"," +
                        "\n\"thumbnail144p\": \"thumbnail_144p_test\"," +
                        "\n\"videoLengthSeconds\": 91," +
                        "\n\"socialMediaVideo\": \"social_media_video_test\"," +

                        "\n\"processed\": \"0\"" +

                        "}")
                .asString();

        assertEquals(stringResponse.getBody().contains("success"), true);


        System.out.println("objectToMorphContentType: " + content[0].getContentType());


        /** Get the object again and check **/

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/" + content[0].getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        objectToMorph = g.fromJson(stringResponse.getBody(), ContentObject.class);
        assertEquals(objectToMorph.getContentTitle(), "title_test");
        assertEquals(objectToMorph.getContentDescription(), "description_test");
        assertEquals(objectToMorph.getContentPrice(), 1.33, 0D);
        assertEquals(objectToMorph.getCategoryId(), categories[0].getCategoryId());
        assertEquals(objectToMorph.getContentUrl(), "url_test");

        assertEquals(objectToMorph.getContentUrl1080p(), "url_1080p_test");
        assertEquals(objectToMorph.getContentUrl720p(), "url_720p_test");
        assertEquals(objectToMorph.getContentUrl480p(), "url_480p_test");
        assertEquals(objectToMorph.getContentUrl360p(), "url_360p_test");
        assertEquals(objectToMorph.getContentUrl240p(), "url_240p_test");
        assertEquals(objectToMorph.getContentUrl144p(), "url_144p_test");


        assertEquals(objectToMorph.getContentPreview1080p(), "preview_1080p_test");
        assertEquals(objectToMorph.getContentPreview720p(), "preview_720p_test");
        assertEquals(objectToMorph.getContentPreview480p(), "preview_480p_test");
        assertEquals(objectToMorph.getContentPreview360p(), "preview_360p_test");
        assertEquals(objectToMorph.getContentPreview240p(), "preview_240p_test");
        assertEquals(objectToMorph.getContentPreview144p(), "preview_144p_test");


        assertEquals(objectToMorph.getThumbnail1080p(), "thumbnail_1080p_test");
        assertEquals(objectToMorph.getThumbnail720p(), "thumbnail_720p_test");
        assertEquals(objectToMorph.getThumbnail480p(), "thumbnail_480p_test");
        assertEquals(objectToMorph.getThumbnail360p(), "thumbnail_360p_test");
        assertEquals(objectToMorph.getThumbnail240p(), "thumbnail_240p_test");
        assertEquals(objectToMorph.getThumbnail144p(), "thumbnail_144p_test");
        assertEquals(objectToMorph.getVideoLengthSeconds(), 91);
        assertEquals(objectToMorph.getSocialMediaVideo(), "social_media_video_test");

        assertEquals(objectToMorph.getProcessed(), 0);

    }

    @Test
    public void test0033_UpdateEmailNotification() throws UnirestException {

        /**
         * Update the user's email notification status in the database.
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        UserObject user = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user.getEmailNotifications(), 1);

        /**
         * Disable email notifications
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/notifications")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{\n\"emailNotifications\": 0\n" +
                        "}")
                .asString();
        UserObject user2 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user2.getEmailNotifications(), 0);

        /**
         * One more check
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        UserObject user3 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user3.getEmailNotifications(), 0);
    }

    @Test
    public void test0034_ContentHasAccessTest() throws UnirestException {

        /**
         * Upload content as the admin
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"newest\",\n" +
                        "\"contentTitle\": \"new\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=newadmin1\"," +
                        "\"contentPrice\": 3.99," +
                        "\"thumbnailUrl\": \"thumbnewadmin.comz\"" +
                        "\n}")
                .asString();
        System.out.println(stringResponse.getBody());
        ContentObject content = g.fromJson(stringResponse.getBody(), ContentObject.class);
        System.out.println("content contentId: " + content.getContentId());

        /**
         * Ensure we do have access to this paid content, and the url is available in the response
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();

        System.out.println("userAccess test content admin get: " + stringResponse.getBody());
        ContentObject[] content0 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content0[0].hasAccess(), 1);
        assertEquals(content0[0].getContentUrl(), "https://www.youtube.com/watch?v=newadmin1");

        /**
         * Ensure we don't have access to this paid content, and the url is null in the response
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        System.out.println("userAccess test content1: " + stringResponse.getBody());
        ContentObject[] content1 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content1[0].hasAccess(), 0);
        assertEquals(content1[0].getContentUrl(), null);

        /**
         * Purchase the content on client side, then update the user's access
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/access")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"contentId\": \"" + content.getContentId() + "\"\n" +
                        "\n}")
                .asString();
        GrantContentAccess.ContentAccessResponse hasAccess = g.fromJson(stringResponse.getBody(), GrantContentAccess.ContentAccessResponse.class);
        assertEquals(hasAccess.isSuccess(), true);

        /**
         * Check the content to ensure we have access as this user and the url is displayed
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        System.out.println(stringResponse.getBody());
        ContentObject[] content2 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content2[0].hasAccess(), 1);
        assertEquals(content2[0].getUserId(), ADMIN_UID);
        assertEquals(content2[0].getContentUrl(), "https://www.youtube.com/watch?v=newadmin1");
    }

    @Test
    public void test0035_ContentViewsTest() throws UnirestException {

        /**
         * Upload content as the admin
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"viewtest\",\n" +
                        "\"contentTitle\": \"yup\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=viewtest\"," +
                        "\"contentPrice\": 3.99," +
                        "\"thumbnailUrl\": \"newnew.com\"" +
                        "\n}")
                .asString();
        ContentObject content = g.fromJson(stringResponse.getBody(), ContentObject.class);
        System.out.println("content contentId: " + content.getContentId());

        /**
         * Ensure this content has 0 views
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        ContentObject[] content1 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content1[0].getViews(), 0);
        assertEquals(content1[0].getUserViewed(), 0);

        /**
         * View the content on client side
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content.getContentId() + "/views")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + ADMIN_UID + "\"\n" +
                        "\n}")
                .asString();
        ContentViewAction.ViewResponse viewResponse = g.fromJson(stringResponse.getBody(), ContentViewAction.ViewResponse.class);
        assertEquals(viewResponse.isSuccess(), true);

        /**
         * Check the content to ensure it got a view.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        ContentObject[] content2 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content2[0].getViews(), 1);
        assertEquals(content2[0].getUserId(), ADMIN_UID);
        //testUID did not view this content
        assertEquals(content2[0].getUserViewed(), 0);

        /**
         * Check the total views for this user
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/views")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        GetTotalViews.TotalViewsResponse tv = g.fromJson(stringResponse.getBody(), GetTotalViews.TotalViewsResponse.class);
        assertEquals(tv.getTotalViews(), 1);

        /**
         * View the content on client side again
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/content/" + content.getContentId() + "/views")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"userId\": \"" + TEST_UID + "\"\n" +
                        "\n}")
                .asString();
        ContentViewAction.ViewResponse viewResponse2 = g.fromJson(stringResponse.getBody(), ContentViewAction.ViewResponse.class);
        assertEquals(viewResponse2.isSuccess(), true);

        /**
         * Check the content to ensure it got another view.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        ContentObject[] content3 = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content3[0].getViews(), 2);
        assertEquals(content3[0].getUserId(), ADMIN_UID);
        //now testUID did view it
        assertEquals(content3[0].getUserViewed(), 1);

        /**
         * Check the total views for this user
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/views")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        GetTotalViews.TotalViewsResponse tv2 = g.fromJson(stringResponse.getBody(), GetTotalViews.TotalViewsResponse.class);
        assertEquals(tv2.getTotalViews(), 2);
    }

    @Test
    public void test0036_DeleteTest() throws UnirestException {

        /**
         * Test all the deletes in the system
         */

        /**
         * Upload content as the admin
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + ADMIN_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"contentType\": \"" + contentTypes[1].getContentTypeId() + "\",\n" +
                        "\"contentDescription\": \"newest\",\n" +
                        "\"contentTitle\": \"new\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=newadmin\"," +
                        "\"contentPrice\": 3.99," +
                        "\"thumbnailUrl\": \"thumbnewadmin.com\"" +
                        "\n}")
                .asString();
        ContentObject content = g.fromJson(stringResponse.getBody(), ContentObject.class);

        /**
         * Content delete test as Admin (publisher)
         */

        stringResponse = Unirest.delete("http://localhost:" + Config.API_PORT + "/content/" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY + "")
                .asString();
        DeleteContent.DeleteContentResponse d = g.fromJson(stringResponse.getBody(), DeleteContent.DeleteContentResponse.class);
        assertEquals(d.contentDeleted(), true);

        /**
         * Try to access content as another user
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?contentId=" + content.getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();
        System.out.println("content deleted stringresponse: " + stringResponse.getBody());
        assertEquals(stringResponse.getBody(), "[]");

        /**
         * Test user delete test
         */

        stringResponse = Unirest.delete("http://localhost:" + Config.API_PORT + "/users/" + SKYDIVER_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + SKYDIVER_UID + "," + SKYDIVER_KEY + "")
                .asString();
        DeleteUser.DeleteUserResponse u = g.fromJson(stringResponse.getBody(), DeleteUser.DeleteUserResponse.class);
        assertEquals(u.userDeleted(), true);

        /**
         * Try to access user as another user
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + SKYDIVER_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();
        assertEquals(stringResponse.getStatus(), 404);

        /**
         * Comment delete test (delete a previously posted comment from ContentCommentsTest)
         */

        stringResponse = Unirest.delete("http://localhost:" + Config.API_PORT + "/comments/" + COMMENT_TEST_ID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + COMMENTER_UID + "," + COMMENTER_KEY + "")
                .asString();
        DeleteComment.DeleteCommentResponse c = g.fromJson(stringResponse.getBody(), DeleteComment.DeleteCommentResponse.class);
        assertEquals(c.commentDeleted(), true);

        /**
         * Try to access the comment as another user
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/" + COMMENT_CONTENT_ID + "/comments")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        CommentObject[] co = g.fromJson(stringResponse.getBody(), CommentObject[].class);

        for (int i = 0; i < co.length; i++) {
            System.out.println("contentCommentslist: " + co[i]);

            assertEquals(co[i].getContentId() == COMMENT_TEST_ID, false);
        }
    }

    @Test
    public void test0037_OrderTest() throws UnirestException {

        /**
         * Test order system, inserting, getting order details and (todo cmcan) updating the order.
         */

        /**
         * Test for bundle order.
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/order")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"orderUUID\":\"27614847GDgvc.1\",\n" +
                        "\"orderType\": "+Config.ORDER_TYPE_BUNDLE+",\n" +
                        "\"publisherId\" : " + content[0].getPoster().getUserId() + ",\n" +
                        "\"buyerId\" :  " + TEST2_UID + ",\n" +
                        "\"contentId\" :  " + content[0].getContentId() + ",\n" +
                        "\"orderOriginId\" :  " + Config.ORDER_ORIGIN_APPLE + "\n" +
                        "}")
                .asString();
        System.out.println(stringResponse.getBody());
        CreateOrder.CreateOrderResponse c = g.fromJson(stringResponse.getBody(), CreateOrder.CreateOrderResponse.class);
        assertEquals(c.isSuccess(), true);

        /**
         * Test for subscription order.
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/order")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"orderUUID\":\"27614847GDgvc3123123.1\",\n" +
                        "\"orderType\": "+Config.ORDER_TYPE_SUBSCRIPTION+",\n" +
                        "\"buyerId\" :  " + TEST_UID + ",\n" +
                        "\"orderOriginId\" :  " + Config.ORDER_ORIGIN_GOOGLE + "\n" +
                        "}")
                .asString();
        System.out.println(stringResponse.getBody());
        CreateOrder.CreateOrderResponse c2 = g.fromJson(stringResponse.getBody(), CreateOrder.CreateOrderResponse.class);
        assertEquals(c2.isSuccess(), true);

        /**
         * Get the inserted bundle order's details.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/order?contentId=" + content[0].getContentId())
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        System.out.println(stringResponse.getBody());
        OrderObject o = g.fromJson(stringResponse.getBody(), OrderObject.class);
        assertEquals(o.getOrderStatus(), "success");
        assertEquals(o.getOrderType(), "bundle");
        assertEquals(o.getOrderOrigin(), "apple");
        assertEquals(o.getContentId(), content[0].getContentId());
        assertEquals(o.getBuyerId(), TEST2_UID);
        assertEquals(o.getPublisherId(), content[0].getPoster().getUserId());
        assertEquals(o.getDelivered(), 0);

        /**
         * Get the inserted subscription order's details.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/order")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        System.out.println(stringResponse.getBody());
        OrderObject o2 = g.fromJson(stringResponse.getBody(), OrderObject.class);
        assertEquals(o2.getOrderStatus(), "success");
        assertEquals(o2.getOrderType(), "subscription");
        assertEquals(o2.getOrderOrigin(), "google");
        assertEquals(o2.getContentId(), -1);
        assertEquals(o2.getBuyerId(), TEST_UID);
        assertEquals(o2.getPublisherId(), -1);
        assertEquals(o2.getDelivered(), 0);

        /**
         * Ensure this user now has access to any content.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();
        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);

        for(int i = 0; i < content.length; i++){
            if(content[i].getContentType() == StaticRules.BUNDLE_CONTENT_TYPE){
                for(ContentObject j : content[i].getChildren()){
                    assertEquals(j.getContentUrl() != null, true);
                    assertEquals(j.getHasAccess(), 1);
                }
            }
        }

        /**
         * Update the order's delivered status (also the publisher and whitespell balances)
         */


    }


    /**
     * DO NOT COPY SHOULD BE LAST TEST
     */

    @Test
    public void test1000_UserLogoutTest() throws UnirestException {

        /**
         * Test logout
         */

        System.out.println("Logout keys:" + TEST2_UID + " : " + TEST2_KEY);

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/logout")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();
        ExpireAuthentication.LogoutObject d = g.fromJson(stringResponse.getBody(), ExpireAuthentication.LogoutObject.class);
        assertEquals(d.isLoggedOut(), true);

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        assertEquals(stringResponse.getStatus(), 401);
    }


    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String returnGetRequest(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", "Mozilla/5.0 ;Windows NT 6.1; WOW64; Trident/7.0; rv:11.0; like Gecko");

        //int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            if (inputLine.length() > 0) {
                response.append(inputLine);
            }
        }
        in.close();

        return response.toString();

    }

    public static boolean isOnline(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", "Mozilla/5.0 ;Windows NT 6.1; WOW64; Trident/7.0; rv:11.0; like Gecko");

        int responseCode = con.getResponseCode();

        return responseCode == 200;

    }


}