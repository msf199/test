package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.GenericAPIActions;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.UserHelper;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class Trending extends EndpointHandler {

    /**
     * What we're getting from user
     */

    private static final String USER_ID_KEY = "user_id";
    private static final String USERNAME_KEY = "username";
    private static final String DISPLAYNAME_KEY = "displayname";
    private static final String THUMBNAIL_KEY = "thumbnail";
    private static final String PUBLISHER_KEY = "publisher";
    private static final String COUNT_KEY = "count";

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
         * Get all the users that match the search query, now sorted by users with highest amount of content posted being shown first
         */

        new Thread(
                () -> {
                    final Map<UserObject, Integer> map = new HashMap<>();

                    try {
                        StatementExecutor executor = new StatementExecutor("SELECT `"+USER_ID_KEY+"`, `"+USERNAME_KEY+"`,`"+DISPLAYNAME_KEY+"`, `"+THUMBNAIL_KEY+"`, `"+PUBLISHER_KEY+"` FROM `user` WHERE `"+PUBLISHER_KEY+"` = 1 ORDER BY `user_id` DESC LIMIT "+limit+"");
                        executor.execute(ps -> {
                            ResultSet results = ps.executeQuery();

                            while (results.next()) {
                                try {
                                    StatementExecutor executor2 = new StatementExecutor("SELECT COUNT(*) AS `count` FROM `content` WHERE `user_id` = ? LIMIT 1");
                                    executor2.execute(ps2 -> {
                                        ps2.setInt(1, results.getInt(USER_ID_KEY));
                                        ResultSet results2 = ps2.executeQuery();

                                        if (results2.next()) {
                                            if (results2.getInt(COUNT_KEY) > 0) {
                                                map.put(new UserObject(
                                                        results.getInt(USER_ID_KEY),
                                                        results.getString((USERNAME_KEY)),
                                                        results.getString(DISPLAYNAME_KEY),
                                                        null,
                                                        results.getString(THUMBNAIL_KEY),
                                                        null,
                                                        null, 1
                                                ), results2.getInt(COUNT_KEY));
                                            }
                                        }
                                    });
                                } catch (SQLException e) {
                                    Logging.log("High", e);
                                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                                    return;
                                }
                            }
                        });
                        final Map<UserObject, Integer> resultMap = sortByValues(map);
                        for(Map.Entry e : resultMap.entrySet()){
                            tempUsers.add((UserObject)e.getKey());
                        }
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
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
                                tempCategories.add(results.getInt(CATEGORY_ID_KEY));
                            }

                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
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
                                ContentObject c = new ContentObject(results.getInt(USER_ID_KEY), results.getInt(CONTENT_ID_KEY), results.getInt(CONTENT_TYPE_KEY), results.getString(CONTENT_TITLE_KEY),
                                        results.getString(CONTENT_URL_KEY), results.getString(CONTENT_DESCRIPTION_KEY),  results.getString(CONTENT_THUMBNAIL_KEY));
                                c.setContentPrice(results.getDouble("content_price"));
                                tempContent.add(c);
                            }

                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    } finally {
                        contentThreadLockRemoved[0] = true;
                    }
                }
        ).start();

        while(!userThreadLockRemoved[0] || !categoryThreadLockRemoved[0] || !contentThreadLockRemoved[0]) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                return;
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
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
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

    /**
     * Comparator used to output highest posting users first in feed
     */

    public static <K, V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<>(map.entrySet());

        Collections.sort(entries, (Map.Entry<K, V> o1, Map.Entry<K, V> o2) -> o2.getValue().compareTo(o1.getValue()));

        Map<K,V> sortedMap = new LinkedHashMap<>();
        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}

