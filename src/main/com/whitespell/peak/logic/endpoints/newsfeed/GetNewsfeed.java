package main.com.whitespell.peak.logic.endpoints.newsfeed;


import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.NewsfeedObject;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         7/15/2015
 */
public class GetNewsfeed extends EndpointHandler {

        private static final String GET_NEWSFEED_QUERY = "SELECT `newsfeed_object` FROM `newsfeed` WHERE `user_id` = ?";
        private static final String PROCESSING_URL_USER_ID = "userId";

        @Override
        protected void setUserInputs() {
            urlInput.put(PROCESSING_URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        }

        @Override
        public void safeCall(final RequestObject context) throws IOException {
            int user_id = Integer.parseInt(context.getUrlVariables().get(PROCESSING_URL_USER_ID));

            com.mashape.unirest.http.HttpResponse<String> stringResponse;
            Gson g = new Gson();
            ArrayList<NewsfeedObject> newsfeedObjects = new ArrayList<>();

            try {
                // make a call to the users endpoint with the current offset
                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + user_id + "?includeFollowing=1&includeCategories=1")
                        .header("accept", "application/json")
                        .header("X-Authentication", "" + 134 + ",la7v7j7i5631q8u532uo9214hl")
                        .asString();
                UserObject user = g.fromJson(stringResponse.getBody(), UserObject.class);

                // count the amount of users we added.
                int usersAdded = 0;
                if (user.getUserFollowing() != null && user.getUserFollowing().size() > 0) {
                    for (int i : user.getUserFollowing()) {
                        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + i)
                                .header("accept", "application/json")
                                .header("X-Authentication", "" + 134 + ",la7v7j7i5631q8u532uo9214hl")
                                .asString();
                        UserObject followingUser = g.fromJson(stringResponse.getBody(), UserObject.class);

                        if(stringResponse.getStatus() == 200){
                            usersAdded++;
                        }

                        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/?userId=" + i)
                                .header("accept", "application/json")
                                .header("X-Authentication", "" + 134 + ",la7v7j7i5631q8u532uo9214hl")
                                .asString();
                        ContentObject[] content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
                        System.out.println("Processing newsfeed for user " + user.getUserId());
                        for(ContentObject c: content) {
                            newsfeedObjects.add(new NewsfeedObject(newsfeedObjects.size(), followingUser, c));
                        }
                    }
                }

                if(newsfeedObjects.size() < 1) {
                    System.out.println("Nothing to post, show trending");
                    //todo(cmcan) make it so that trending is based on categories and content
                    stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + user_id + "?includeCategories=1")
                            .header("accept", "application/json")
                            .header("X-Authentication", "" + 134 + ",la7v7j7i5631q8u532uo9214hl")
                            .asString();
                    System.out.println("userWCategory"+stringResponse.getBody());
                    UserObject userTrending = g.fromJson(stringResponse.getBody(), UserObject.class);
                    if (userTrending.getCategoryFollowing() != null && userTrending.getCategoryFollowing().size() > 0) {
                        for (int i : userTrending.getCategoryFollowing()) {

                            stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content?categoryId=" + i)
                                    .header("accept", "application/json")
                                    .header("X-Authentication", "" + 134 + ",la7v7j7i5631q8u532uo9214hl")
                                    .asString();
                            ContentObject[] content = g.fromJson(stringResponse.getBody(), ContentObject[].class);
                            System.out.println("contentCategory: "+stringResponse.getBody());

                            for(ContentObject c: content) {
                                int contentUserId = c.getUserId();
                                stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + contentUserId+"?includeFollowing=1&includeCategories=1")
                                        .header("accept", "application/json")
                                        .header("X-Authentication", "" + 134 + ",la7v7j7i5631q8u532uo9214hl")
                                        .asString();
                                System.out.println("contentUser: "+stringResponse.getBody());
                                UserObject contentUser = g.fromJson(stringResponse.getBody(), UserObject.class);
                                newsfeedObjects.add(new NewsfeedObject(newsfeedObjects.size(), contentUser, c));
                            }
                        }
                    }


                } else {
                    System.out.println("posting");
                }

                final Gson f = new Gson();
                String response = f.toJson(newsfeedObjects);
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
