package tests.com.whitespell.peak;

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
import main.com.whitespell.peak.model.CategoryObject;
import main.com.whitespell.peak.model.ContentTypeObject;
import main.com.whitespell.peak.model.ErrorObject;
import main.com.whitespell.peak.model.UserObject;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/21/15
 *         tests.com.whitespell.peak
 */
public class Tests extends Server {

    static String TEST_DB_NAME = "test_" + (System.currentTimeMillis() / 1000);

    static String TEST_USERNAME = "pimdewitte";
    static String TEST_PASSWORD = "3#$$$$$494949($(%*__''";
    static String TEST_EMAIL = "pimdewitte95@gmail.com";
    static int TEST_UID = -1;
    static String TEST_KEY;

    static CategoryObject[] categories;
    static ContentTypeObject[] contentTypes;

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

    public static void main(String[] args) {
        Config.TESTING = true;
        Config.CONFIGURATION_FILE = "tests.prop";
        Server.start();
        API = "http://localhost:" + Config.API_PORT;
    }

    @Test
    public void startTests() throws Exception {

        // load the system with test properties
        Config.TESTING = true;
        Config.CONFIGURATION_FILE = "tests.prop";

        // start the server
        Server.start();

        API = "http://localhost:" + Config.API_PORT;
    }

    @Test
        public void newDatabase() throws IOException {


            if (Config.DB_USER.equals("testpeak")) { // ensure we are on the test server
                // truncate peak_ci_test_ddl


                /**
                 * CREATING THE TEST DATABASE
                 */
                try {
                    StatementExecutor executor = new StatementExecutor("CREATE DATABASE " + TEST_DB_NAME + ";");
                    executor.execute(new ExecutionBlock() {

                        @Override
                        public void process(PreparedStatement ps) throws SQLException {

                            ps.executeUpdate();
                        }
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                }

                /**
                 * USING THE TEST DATABASE
                 */

                try {
                    StatementExecutor executor = new StatementExecutor("use " + TEST_DB_NAME + ";");
                    executor.execute(new ExecutionBlock() {

                        @Override
                        public void process(PreparedStatement ps) throws SQLException {

                            ps.executeUpdate();
                        }
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                }

                /**
                 * EXECUTING DDL ON TEST DATABASE
                 */

                String[] queries = TestFunctions.readFile("ddl/peak.sql", StandardCharsets.UTF_8).split(";");

                for (int i = 0; i < queries.length; i++) {
                    if (queries[i] == null || queries[i].length() < 2 || queries[i].isEmpty()) {
                        continue;
                    }
                    try {
                        StatementExecutor executor = new StatementExecutor(queries[i]);
                        executor.execute(new ExecutionBlock() {

                            @Override
                            public void process(PreparedStatement ps) throws SQLException {

                                ps.executeUpdate();
                            }
                        });
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
    public void waitForOnlineTest() {
        int attempts = 20;

        boolean isOnline = false;

        while (attempts > 0 && !isOnline) {
            try {
                isOnline = HttpHandler.isOnline(API + "/ping");
            } catch (Exception e) {

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
    public void forceErrorTest() throws UnirestException {
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
        assertEquals("There was no endpoint found on this path", e.getErrorMessage());
        assertEquals("EndpointDispatcher", e.getClassName());
        assertEquals(404, e.getHttpStatusCode());
        assertEquals(124, e.getErrorId());
    }

    @Test
    public void createAccountTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\""+TEST_USERNAME+"\",\n" +
                        "\"password\" : \""+TEST_PASSWORD+"\",\n" +
                        "\"email\" : \""+TEST_EMAIL+"\"\n" +
                        "}")
                .asString();

        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication") // misspeled S on purpose to cause an error.
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\""+TEST_USERNAME+"\",\n" +
                        "\"password\" : \""+TEST_PASSWORD+"\"\n" +
                        "}")
                .asString();




        AuthenticationObject a = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
        TEST_UID = a.getUserId();
        TEST_KEY = a.getKey();

        assertEquals(a.getUserId() > -1, true);
    }




    @Test
    public void categoriesTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/content/categories") 
                .header("accept", "application/json")
                .body("{\n" +
                        "\"category_name\": \"skydiving\",\n" +
                        "\"category_thumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n" +
                        "}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/categories") 
                .header("accept", "application/json")
                .body("{\n" +
                        "\"category_name\": \"roller-skating\",\n" +
                        "\"category_thumbnail\": \"https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSh0tZytkPcFHRPQrTjC9O6a1TFGi8_XvD0TWtRLARQGsra9LjO\"\n" +
                        "}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/categories")
                .header("accept", "application/json")
                .asString();

        categories = g.fromJson(stringResponse.getBody(), CategoryObject[].class);
        assertEquals(categories.length, 2);
        assertEquals(categories[0].getCategory_name(), "skydiving");
        assertEquals(categories[1].getCategory_name(), "roller-skating");
    }

    @Test
    public void followCategoriesTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/user/"+TEST_UID+"/categories")
                .header("accept", "application/json")
                .header("X-Authentication", ""+TEST_UID+","+TEST_KEY+"")
                .body("{\n" +
                        "\"category_id\": \""+categories[0].getCategory_id()+"\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();
        Unirest.post("http://localhost:" + Config.API_PORT + "/user/"+TEST_UID+"/categories")
                .header("accept", "application/json")
                .header("X-Authentication", ""+TEST_UID+","+TEST_KEY+"")
                .body("{\n" +
                        "\"category_id\": \""+categories[1].getCategory_id()+"\",\n" +
                        "\"action\": \"follow\"\n" +
                        "}")
                .asString();
    }

    @Test
    public void createPublishers() throws UnirestException {
        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\""+SKYDIVER_USERNAME+"\",\n" +
                        "\"password\" : \""+SKYDIVER_PASSWORD+"\",\n" +
                        "\"email\" : \""+SKYDIVER_EMAIL+"\"\n" +
                        "}")
                .asString();

        UserObject skydiver = g.fromJson(stringResponse.getBody(), UserObject.class);
        SKYDIVER_UID = skydiver.getUserId();

        //todo(pim) authenticate as user
        //todo(pim) call to publish in this category




        stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/users")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"username\":\""+ROLLERSKATER_USERNAME+"\",\n" +
                        "\"password\" : \""+ROLLERSKATER_PASSWORD+"\",\n" +
                        "\"email\" : \""+ROLLERSKATER_EMAIL+"\"\n" +
                        "}")
                .asString();
        UserObject rollerskater = g.fromJson(stringResponse.getBody(), UserObject.class);
        ROLLERSKATER_UID = rollerskater.getUserId();
    }
    @Test
    public void contentTypesTest() throws UnirestException {
        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"content_type_name\": \"youtube\"\n"
                        +"}")
                .asString();

        Unirest.post("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .body("{\n" +
                        "\"content_type_name\": \"instagram\"\n"
                        +"}")
                .asString();

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/types")
                .header("accept", "application/json")
                .asString();

        contentTypes = g.fromJson(stringResponse.getBody(), ContentTypeObject[].class);
        assertEquals(contentTypes.length, 2);
        assertEquals(contentTypes[0].getContent_type_name(), "youtube");
        assertEquals(contentTypes[1].getContent_type_name(), "instagram");
    }

    @Test
    public void getPublishersByCategory(){
        //todo(pim) do a search on /users where publishing list contains numbers category[0] and category[1] and output as json
        // then follow these users
        // then post content as the publishers
        // then generate newsfeed based on their content
    }

    @Test
    public void incurCreateAccountErrors() {
        //todo(pim) create accounts with usernames and too long strings that are already taken and should give us errors
    }




}
