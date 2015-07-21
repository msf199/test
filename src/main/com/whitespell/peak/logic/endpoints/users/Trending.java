package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.GenericAPIActions;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class Trending extends EndpointHandler {

    private static final String QS_SEARCH_QUERY_KEY = "q";

    /**
     * What we're getting from user
     */
    private static final String USER_ID_KEY = "user_id";
    private static final String USERNAME_KEY = "username";
    private static final String DISPLAYNAME_KEY = "displayname";
    private static final String THUMBNAIL_KEY = "thumbnail";

    /**
     * What we're getting from categories
     */
    private static final String CATEGORY_ID_KEY = "category_id";

    /**
     * What we're getting from content
     */

    private static final String CONTENT_ID_KEY = "content_id";
    private static final String CONTENT_TYPE_KEY = "content_type";
    private static final String CONTENT_URL_KEY = "content_url";
    private static final String CONTENT_DESCRIPTION_KEY = "content_description";
    private static final String CONTENT_TITLE_KEY = "content_title";
    private static final String CONTENT_THUMBNAIL_KEY = "thumbnail_url";




    @Override
    protected void setUserInputs() {
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int limit =  GenericAPIActions.getLimit(context.getQueryString());
        int offset =  GenericAPIActions.getOffset(context.getQueryString());


        ArrayList<UserObject> tempUsers = new ArrayList<>();
        ArrayList<Integer> tempCategories = new ArrayList<>();
        ArrayList<ContentObject> tempContent = new ArrayList<>();

        /**
         * Initialize the locks to ensure each query is finished with processing before rendering the result
         * (Purposely chose 3 different objects, and not a boolean array due to multithreading)
         */

        final boolean[] userThreadLockRemoved = {false};
        final boolean[] categoryThreadLockRemoved = {false};
        final boolean[] contentThreadLockRemoved = {false};

        /**
         * Get all the users that match the search query
         */


        new Thread(
                () -> {
                    try {
                    StatementExecutor executor = new StatementExecutor("SELECT `"+USER_ID_KEY+"`, `"+USERNAME_KEY+"`,`"+DISPLAYNAME_KEY+"`, `"+THUMBNAIL_KEY+"` FROM `user` ORDER BY `user_id` DESC LIMIT "+limit+"");
                        executor.execute(ps -> {
                            ResultSet results = ps.executeQuery();

                            while(results.next()) {
                                System.out.println(results.getString(USERNAME_KEY));
                                tempUsers.add(new UserObject(
                                results.getInt(USER_ID_KEY),
                                results.getString((USERNAME_KEY)),
                                results.getString(DISPLAYNAME_KEY),
                                null,
                                results.getString(THUMBNAIL_KEY),
                                null,
                                null
                                ));
                            }

                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        userThreadLockRemoved[0] = true;
                    }
                }
        ).start();

        /**
         * Get all the categories that match the search query
         */

        new Thread(
                () -> {
                    try {
                        StatementExecutor executor = new StatementExecutor("SELECT `"+CATEGORY_ID_KEY+"` FROM `category` ORDER BY `category_id` DESC LIMIT "+limit+"");
                        executor.execute(ps -> {
                            ResultSet results = ps.executeQuery();

                            while(results.next()) {
                                System.out.println(results.getString(CATEGORY_ID_KEY));
                                tempCategories.add(results.getInt(CATEGORY_ID_KEY));
                            }

                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        categoryThreadLockRemoved[0] = true;
                    }
                }
        ).start();

        /**
         * Get all the content that matches the search query
         */

        new Thread(
                () -> {
                    try {
                        StatementExecutor executor = new StatementExecutor("SELECT * FROM `content`" +
                                " ORDER BY `content_id` DESC LIMIT "+limit+"");
                        executor.execute(ps -> {
                            ResultSet results = ps.executeQuery();

                            while(results.next()) {
                                tempContent.add(new ContentObject(results.getInt(CONTENT_ID_KEY), results.getInt(CONTENT_TYPE_KEY), results.getString(CONTENT_TITLE_KEY),
                                        results.getString(CONTENT_URL_KEY), results.getString(CONTENT_DESCRIPTION_KEY),  results.getString(CONTENT_THUMBNAIL_KEY)));
                            }

                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        contentThreadLockRemoved[0] = true;
                    }
                }
        ).start();

        while(!userThreadLockRemoved[0] || !categoryThreadLockRemoved[0] || !contentThreadLockRemoved[0]) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        TrendingResponse search = new TrendingResponse(tempUsers, tempCategories, tempContent);

        /**
         * In this endpoint we want to:
         * Concurrently query the database for three different queries in different
         */


        Gson g = new Gson();
        String response = g.toJson(search);
        context.getResponse().setStatus(200);
        try {
            context.getResponse().getWriter().write(response);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public class TrendingResponse {


        public TrendingResponse(ArrayList<UserObject> users, ArrayList<Integer> categories, ArrayList<ContentObject> content) {
            this.users = users;
            this.categories = categories;
            this.content = content;
        }

        public ArrayList<UserObject> users;
        public ArrayList<Integer> categories;
        public ArrayList<ContentObject> content;
    }
}
