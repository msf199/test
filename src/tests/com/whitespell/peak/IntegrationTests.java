package com.whitespell.peak;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.users.CategoryFollowAction;
import main.com.whitespell.peak.logic.endpoints.users.Trending;
import main.com.whitespell.peak.logic.endpoints.users.UserFollowAction;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.Pool;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.*;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

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
    static String TEST_EMAIL = "pimdewitte95@gmail.com";
    static int TEST_UID = -1;
    static String TEST_KEY;
    static int TEST2_UID = -1;
    static String TEST2_KEY;
    static int ADMIN_UID = -1;
    static String ADMIN_KEY;

    static CategoryObject[] categories;
    static ContentTypeObject[] contentTypes;
    static ContentObject[] content;

    static String SKYDIVER_USERNAME = "skydiver10";
    static String SKYDIVER_PASSWORD = "3#$$$$$494949($(%*__''";
    static String SKYDIVER_EMAIL = "skydiver10@gmail.com";
    static int SKYDIVER_UID;

    static String ROLLERSKATER_USERNAME = "rollerskater10";
    static String ROLLERSKATER_PASSWORD = "3#$$$$$494949($(%*__''";
    static String ROLLERSKATER_EMAIL = "rollerskater10@gmail.com";
    static int ROLLERSKATER_UID;

    static String API = null;

    Gson g = new Gson();
    HttpResponse<String> stringResponse = null;
    HttpResponse<JsonNode> jsonResponse = null;


    @Test
    public void test1_startTests() throws Exception {

        // load the system with test properties
        Config.TESTING = true;
        Config.CONFIGURATION_FILE = "tests.prop";

        // start the server
        Server.start();

        API = "http://localhost:" + Config.API_PORT;
    }

    @Test
    public void test2_newDatabase() throws IOException {


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

            String[] queries = readFile("ddl/peak.sql", StandardCharsets.UTF_8).split(";");

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
        }
    }


    @Test
    public void test3_waitForOnlineTest() {
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
    public void test4_forceErrorTest() throws UnirestException {
        /**
         * Force an error
         */


        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/nosuchendpoint")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\"testaccount1\",\n" +
                        "\"password\" : \"1234567\",\n" +
                        "\"email\" : \"test123\"\n" +
                        "}")
                .asString();


        ErrorObject e = g.fromJson(stringResponse.getBody(), ErrorObject.class);
        assertEquals("There was no endpoint found on this path. Make sure you're using the right method (GET,POST,etc.)", e.getErrorMessage());
        assertEquals("EndpointDispatcher", e.getClassName());
        assertEquals(404, e.getHttpStatusCode());
        assertEquals(124, e.getErrorId());
    }

    @Test
    public void test5_createAccountTest() throws UnirestException {

        /**
         * Create the account we are testing with
         */
        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\"" + TEST_USERNAME + "\",\n" +
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
                        "\"username\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
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
                        "\"username\":\"coryqq\",\n" +
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
                        "\"username\":\"" + TEST_USERNAME + "\",\n" +
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
                        "\"username\":\"" + TEST_EMAIL + "\",\n" +
                        "\"password\" : \"" + TEST_PASSWORD + "\"\n" +
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
                        "\"username\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\"\n" +
                        "}")
                .asString();


        AuthenticationObject c = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST2_UID = c.getUserId();
        TEST2_KEY = c.getKey();

        assertEquals(b.getUserId() > -1, true);

        /**
         * Authenticate the 2nd User with email
         * */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\"" + ROLLERSKATER_EMAIL + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\"\n" +
                        "}")
                .asString();


        AuthenticationObject d = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST2_UID = d.getUserId();
        TEST2_KEY = d.getKey();

        assertEquals(b.getUserId() > -1, true);

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
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/"+ TEST2_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        UserObject user2 = g.fromJson(stringResponse.getBody(), UserObject.class);

        assertEquals(user2.getUserId(), TEST2_UID);
        assertEquals(user2.getUserName(), ROLLERSKATER_USERNAME);
    }


    @Test
    public void test6_categoriesTest() throws UnirestException {
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
    public void test7_followCategoriesTest() throws UnirestException {
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


        //todo (pim) get categories_following from user object and test whether they are skydivign and rollerskating
        assertEquals(h.getActionTaken(), "followed");
        assertEquals(f.getActionTaken(), "followed");
    }

    @Test
    public void test8_createPublishers() throws UnirestException {
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\"" + SKYDIVER_USERNAME + "\",\n" +
                        "\"password\" : \"" + SKYDIVER_PASSWORD + "\",\n" +
                        "\"email\" : \"" + SKYDIVER_EMAIL + "\"\n" +
                        "}")
                .asString();

        UserObject skydiver = g.fromJson(stringResponse.getBody(), UserObject.class);
        SKYDIVER_UID = skydiver.getUserId();

        //todo(pim) authenticate as user
        //todo(pim) safeCall to publish in this category
    }

    @Test
    public void test9_contentTypesTest() throws UnirestException {

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"contentTypeName\": \"youtube\"\n"
                        + "}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"contentTypeName\": \"instagram\"\n"
                        + "}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .asString();
        contentTypes = g.fromJson(stringResponse.getBody(), ContentTypeObject[].class);
        assertEquals(contentTypes.length, 2);
        assertEquals(contentTypes[0].getContentTypeName(), "youtube");
        assertEquals(contentTypes[1].getContentTypeName(), "instagram");
    }

    @Test
    public void testA_followTest() throws UnirestException {
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
    }

    @Test
    public void testB_contentTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \""+categories[0].getCategoryId()+"\",\n" +
                        "\"contentType\": \""+contentTypes[0].getContentTypeId()+"\",\n" +
                        "\"contentDescription\": \"We have excuse-proofed your fitness routine with our latest Class FitSugar.\",\n" +
                        "\"contentTitle\": \"10-Minute No-Equipment Home Workout\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=I6t0quh8Ick\"," +
                        "\"thumbnailUrl\": \"thumburl.com\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \""+categories[0].getCategoryId()+"\",\n" +
                        "\"contentType\": \""+contentTypes[0].getContentTypeId()+"\",\n" +
                        "\"contentDescription\": \"This one's hot!\",\n" +
                        "\"contentTitle\": \"Another Video!\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=827377fhU\"," +
                        "\"thumbnailUrl\": \"thumbguy.com\"" +
                        "\n}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"categoryId\": \""+categories[1].getCategoryId()+"\",\n" +
                        "\"contentType\": \""+contentTypes[1].getContentTypeId()+"\",\n" +
                        "\"contentDescription\": \"content2\",\n" +
                        "\"contentTitle\": \"content2\",\n" +
                        "\"contentUrl\": \"https://www.youtube.com/watch?v=content2\"," +
                        "\"thumbnailUrl\": \"thumb.com\"" +
                        "\n}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content")
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

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?userId=" + TEST_UID)
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

        //todo(make sure this works)
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?categoryId=" + categories[0].getCategoryId() )
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

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?categoryId=" + categories[1].getCategoryId() )
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
    public void testC_getPublishersByCategory() {
        //todo(pim) do a search on /users where publishing list contains numbers category[0] and category[1] and output as json
        // then follow these users
        // then post content as the publishers
        // then generate newsfeed based on their content
    }

    @Test
    public void testD_incurCreateAccountErrors() {
        //todo(pim) create accounts with usernames and too long strings that are already taken and should give us errors
    }

	@Test
	public void testE_editUser() throws UnirestException{

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
                        "\"username\": \"p1mw1n\",\n" +
                        "\"displayname\": \"new\",\n" +
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
                .body("{\n\"username\": \"evenneweruser\"\n}")
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
                .body("{\n\"username\": \"evenneweruser2\",\n" +
                        "\"slogan\": \"slogan\"\n" +
                        "}")
                .asString();
        UserObject userEdit5 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userEdit5.getUserId(), TEST_UID);
        assertEquals(userEdit5.getUserName(), "evenneweruser2");
        assertEquals(userEdit5.getSlogan(), "slogan");
	}

    @Test
    public void testF_editSettings() throws UnirestException{

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
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"!@#$%^&*()~\",\n" +
                        "\"newPassword\": \""+TEST_PASSWORD+"\",\n" +
                        "\"email\": \"newtestemail@lol.com\"\n" +
                        "}")
                .asString();
        UserObject user3 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user3.getEmail(), "newtestemail@lol.com");
    }


    @Test
    public void testG_search() throws UnirestException{

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
    public void testH_getUserFollowing() throws UnirestException{

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"followingId\": \"" + SKYDIVER_UID + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();

        UserFollowAction.FollowActionObject a = g.fromJson(stringResponse.getBody(), UserFollowAction.FollowActionObject.class);
        assertEquals(a.getActionTaken(),"followed");

        /**
         * List followers for user that followed other users previously
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/?includeFollowing=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject userThatFollows = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(userThatFollows.getUserFollowing().get(0).intValue(), TEST2_UID);
        assertEquals(userThatFollows.getUserFollowing().get(1).intValue(), SKYDIVER_UID);
    }

    @Test
    public void testI_getCategoryFollowing() throws UnirestException{

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
    public void testJ_getUsers() throws UnirestException{

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/?offset=1&limit=50")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject[] a = g.fromJson(stringResponse.getBody(), UserObject[].class);
        assertEquals(a[0].getUserId(), TEST_UID);
        assertEquals(a[1].getUserId(), TEST2_UID);
    }

    @Test
    public void testK_getNewsfeed() throws UnirestException{

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/newsfeed/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        NewsfeedObject[] n = g.fromJson(stringResponse.getBody(), NewsfeedObject[].class);
        for(int i = 0; i < 3; i++){
            assertEquals(n[i].getNewsfeed_id(), i);
            if(i == 0) {
                assertEquals(n[i].getNewsfeedUser().getUserId(), TEST_UID);
                assertEquals(n[i].getNewsfeedContent().getContentTitle(), "10-Minute No-Equipment Home Workout");
            }else if(i == 1){
                assertEquals(n[i].getNewsfeedUser().getUserId(), TEST_UID);
                assertEquals(n[i].getNewsfeedContent().getContentTitle(), "Another Video!");
            }else if(i == 2){
                assertEquals(n[i].getNewsfeedUser().getUserId(), TEST2_UID);
                assertEquals(n[i].getNewsfeedContent().getContentTitle(), "content2");
            }
        }
    }

    @Test
    public void testL_ensureContentPostersAreCategoryPublishers() throws UnirestException{

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID+"?includePublishing=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        UserObject user = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user.getCategoryPublishing().get(0).intValue(), categories[0].getCategoryId());

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID+"?includePublishing=1")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .asString();

        UserObject user2 = g.fromJson(stringResponse.getBody(), UserObject.class);
        assertEquals(user2.getCategoryPublishing().get(0).intValue(), categories[1].getCategoryId());
    }

    @Test
    public void testM_TrendingPublishingUsers() throws UnirestException{

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/trending")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        Trending.TrendingResponse trending = g.fromJson(stringResponse.getBody(), Trending.TrendingResponse.class);
        assertEquals(trending.users.get(0).getUserId(), TEST_UID);
        assertEquals(trending.users.get(1).getUserId(), TEST2_UID);
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
            if(inputLine.length() > 0) {
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