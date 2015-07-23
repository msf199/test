package main.com.whitespell.peak.logic.endpoints.newsfeed;


import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.GenericAPIActions;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.NewsfeedObject;
import main.com.whitespell.peak.model.UserObject;
import main.com.whitespell.peak.model.authentication.AuthenticationObject;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         7/15/2015
 */
public class GetNewsfeed extends EndpointHandler {

    private static final String PROCESSING_URL_USER_ID = "userId";
    private static final String NEWSFEED_SIZE_LIMIT = "limit";
    private static final String NEWSFEED_OFFSET = "offset";

    //Admin is the user that will generate the newsfeed.
    private static int ADMIN_UID = -1;
    private static String ADMIN_KEY;

    @Override
    protected void setUserInputs() {
        urlInput.put(PROCESSING_URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        queryStringInput.put(NEWSFEED_SIZE_LIMIT, StaticRules.InputTypes.REG_INT_OPTIONAL);
        queryStringInput.put(NEWSFEED_OFFSET, StaticRules.InputTypes.REG_INT_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        int user_id = Integer.parseInt(context.getUrlVariables().get(PROCESSING_URL_USER_ID));

        com.mashape.unirest.http.HttpResponse<String> stringResponse;
        Gson g = new Gson();
        ArrayList<NewsfeedObject> newsfeedObjects = new ArrayList<>();
        int limit = GenericAPIActions.getLimit(context.getQueryString());
        int offset = GenericAPIActions.getOffset(context.getQueryString());
        boolean outputNewsfeed = false;

        /**
         * Authenticate as "admin" to create newsfeed and allow integrationTest
         */
        try {
            stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/authentication")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"username\":\"coryqq\",\n" +
                            "\"password\" : \"qqqqqq\"\n" +
                            "}")
                    .asString();
            AuthenticationObject ao = g.fromJson(stringResponse.getBody(), AuthenticationObject.class);
            ADMIN_UID = ao.getUserId();
            ADMIN_KEY = ao.getKey();

            assertEquals(ao.getUserId() > -1, true);

            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + user_id + "?includeFollowing=1&includeCategories=1")
                    .header("accept", "application/json")
                    .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY)
                    .asString();

            UserObject userTrending = g.fromJson(stringResponse.getBody(), UserObject.class);
            if (userTrending.getCategoryFollowing() != null && userTrending.getCategoryFollowing().size() > 0) {
                for (int i : userTrending.getCategoryFollowing()) {

                    stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?categoryId=" + i)
                            .header("accept", "application/json")
                            .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY)
                            .asString();
                    ContentObject[] content = g.fromJson(stringResponse.getBody(), ContentObject[].class);

                    for (ContentObject c : content) {
                        int contentUserId = c.getUserId();
                        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + contentUserId + "?includeFollowing=1&includeCategories=1")
                                .header("accept", "application/json")
                                .header("X-Authentication", "" + ADMIN_UID + "," + ADMIN_KEY)
                                .asString();
                        UserObject contentUser = g.fromJson(stringResponse.getBody(), UserObject.class);
                        newsfeedObjects.add(new NewsfeedObject(newsfeedObjects.size(), contentUser, c));

                        if (newsfeedObjects.size() == (limit + offset)) {
                            outputNewsfeed = true;
                            break;
                        }
                    }
                    if (outputNewsfeed) {
                        break;
                    }
                }
            }

            ArrayList<NewsfeedObject> newsfeedResponse = new ArrayList<>();
            for (NewsfeedObject n : newsfeedObjects) {
                int currId = n.getNewsfeed_id();
                if (currId >= offset) {
                    newsfeedResponse.add(n);
                }
            }

            final Gson f = new Gson();
            String response = f.toJson(newsfeedResponse);
            context.getResponse().setStatus(200);
            try {
                context.getResponse().getWriter().write(response);
            } catch (Exception e) {
                Logging.log("High", e);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
