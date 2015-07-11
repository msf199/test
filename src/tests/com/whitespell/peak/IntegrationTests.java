package com.whitespell.peak;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
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
import java.sql.PreparedStatement;
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
    static int TEST2_UID;
    static String TEST2_KEY;

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
         * Authenticate first user we just created
         */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication") // misspeled S on purpose to cause an error.
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
        * Authenticate the 2nd User
        * */

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\"\n" +
                        "}")
                .asString();


        AuthenticationObject b = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST2_UID = b.getUserId();
        TEST2_KEY = b.getKey();

        assertEquals(b.getUserId() > -1, true);

        /**
         * Get the UserObject from the users/userid endpoint
         */

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/"+TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        System.out.println(stringResponse.getBody());
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
                        "\"category_name\": \"skydiving\",\n" +
                        "\"category_thumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n" +
                        "}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/categories")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"category_name\": \"roller-skating\",\n" +
                        "\"category_thumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n" +
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
        Unirest.post("http://localhost:" + Config.API_PORT + "/user/" + TEST_UID + "/categories")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"category_id\": \"" + categories[0].getCategoryId() + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();
        Unirest.post("http://localhost:" + Config.API_PORT + "/user/" + TEST_UID + "/categories")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"category_id\": \"" + categories[1].getCategoryId() + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();

        //todo (pim) get categories_following from user object and test whether they are skydivign and rollerskating
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


        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\"" + ROLLERSKATER_USERNAME + "\",\n" +
                        "\"password\" : \"" + ROLLERSKATER_PASSWORD + "\",\n" +
                        "\"email\" : \"" + ROLLERSKATER_EMAIL + "\"\n" +
                        "}")
                .asString();
        UserObject rollerskater = g.fromJson(stringResponse.getBody(), UserObject.class);
        ROLLERSKATER_UID = rollerskater.getUserId();
    }

    @Test
    public void test9_contentTypesTest() throws UnirestException {

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"content_type_name\": \"youtube\"\n"
                        + "}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"content_type_name\": \"instagram\"\n"
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
        HttpResponse<String> a = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/following")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n" +
                        "\"following_id\": \"" + TEST2_UID + "\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();

        System.out.println(a.getBody());
    }

    @Test
    public void testB_contentTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST2_UID + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST2_UID + "," + TEST2_KEY + "")
                .body("{\n" +
                        "\"content_type\": \""+contentTypes[0].getContentTypeId()+"\",\n" +
                        "\"content_description\": \"We have excuse-proofed your fitness routine with our latest Class FitSugar.\",\n" +
                        "\"content_title\": \"10-Minute No-Equipment Home Workout\",\n" +
                        "\"content_url\": \"https://www.youtube.com/watch?v=I6t0quh8Ick\"\n}")
                .asString();


        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
        assertEquals(content[0].getContentType(), contentTypes[0].getContentTypeId());
        assertEquals(content[0].getContentTitle(), "10-Minute No-Equipment Home Workout");
        assertEquals(content[0].getContentUrl(), "https://www.youtube.com/watch?v=I6t0quh8Ick");
        assertEquals(content[0].getContentDescription(), "We have excuse-proofed your fitness routine with our latest Class FitSugar.");
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
        System.out.println(stringResponse.getBody());

        /**
         * Change only thumbnail
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"thumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n}")
                .asString();
        UserObject userEdit = g.fromJson(stringResponse.getBody(), UserObject.class);
        System.out.println(stringResponse.getBody());
        assertEquals(userEdit.getUserId(), TEST_UID);
        assertEquals(userEdit.getThumbnail(), "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO");

        /**
         * Change only cover_photo
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID)
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"cover_photo\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n}")
                .asString();
        UserObject userEdit2 = g.fromJson(stringResponse.getBody(), UserObject.class);
        System.out.println(stringResponse.getBody());
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
                        "\"cover_photo\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\",\n" +
                        "\"slogan\": \"slogan\"\n" +
                        "}")
                .asString();
        UserObject userEdit3 = g.fromJson(stringResponse.getBody(), UserObject.class);
        System.out.println(stringResponse.getBody());
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
        System.out.println(stringResponse.getBody());
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
        System.out.println(stringResponse.getBody());
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

        /**
         * Change only password
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"" + TEST_PASSWORD + "\",\n" +
                        "\"new_password\": \"!@#$%^&*()~\"\n" +
                        "}")
                .asString();

        /**
         * Change both email & password
         */
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users/" + TEST_UID + "/settings")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .body("{\n\"password\": \"!@#$%^&*()~\",\n" +
                        "\"new_password\": \")(*&^%$#@!~\",\n" +
                        "\"email\": \"newtestemail2@lol.com\"\n" +
                        "}")
                .asString();
    }


    @Test
    public void testG_search() throws UnirestException{

        /**
         * Test search for the content we've added.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/search?q=excuse")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        System.out.println(stringResponse.getBody()
        );

        assertEquals(stringResponse.getBody().contains("10-Minute No-Equipment Home Workout"), true);

        /**
         * Test search for the user we've added.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/search?q=even")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        System.out.println(stringResponse.getBody());

        assertEquals(stringResponse.getBody().contains("evenneweruser2"), true);

        /**
         * Test search for the category we've added.
         */
        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/search?q=roller")
                .header("accept", "application/json")
                .header("X-Authentication", "" + TEST_UID + "," + TEST_KEY + "")
                .asString();

        System.out.println(stringResponse.getBody());

        assertEquals(stringResponse.getBody().contains("["+categories[1].getCategoryId()+"]"), true);
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