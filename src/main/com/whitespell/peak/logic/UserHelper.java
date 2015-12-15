package main.com.whitespell.peak.logic;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         10/6/2015
 */
public class UserHelper {


    private static final String FIND_FOLLOWERS_QUERY = "SELECT `user_id` FROM `user_following` WHERE `following_id` = ?";
    private static final String FIND_FOLLOWING_QUERY = "SELECT `following_id` FROM `user_following` WHERE `user_id` = ?";
    private static final String FIND_CATEGORIES_QUERY = "SELECT `category_id` FROM `category_following` WHERE `user_id` = ?";
    private static final String FIND_PUBLISHING_QUERY = "SELECT `category_id` FROM `category_publishing` WHERE `user_id` = ?";
    private static final String GET_USER = "SELECT `user_id`, `username`, `displayname`, `email`, `thumbnail`, `cover_photo`, `slogan`, `publisher`, `email_verified`, `email_notifications`, `subscriber` FROM `user` WHERE `user_id` = ?";
    private static final String USERNAME_KEY = "username";
    private static final String DISPLAYNAME_KEY = "displayname";
    private static final String EMAIL_KEY = "email";
    private static final String THUMBNAIL_KEY = "thumbnail";
    private static final String COVER_PHOTO_KEY = "cover_photo";
    private static final String SLOGAN_KEY = "slogan";
    private static final String PUBLISHER_KEY = "publisher";


    public UserObject getUserById(int userId, boolean getFollowers, boolean getFollowing, boolean getCategories, boolean getPublishing){
        final UserObject[] u = {null};


        final ArrayList<Integer> initialFollowers = new ArrayList<>();
        if(getFollowers) {
            try {
                StatementExecutor executor = new StatementExecutor(FIND_FOLLOWERS_QUERY);
                executor.execute(ps -> {
                    ps.setInt(1, (userId));

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        initialFollowers.add(results.getInt("user_id"));
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }
        }


        final ArrayList<Integer> initialFollowing = new ArrayList<>();
        if(getFollowing) {
            try {
                StatementExecutor executor = new StatementExecutor(FIND_FOLLOWING_QUERY);
                executor.execute(ps -> {
                    ps.setInt(1, (userId));

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        initialFollowing.add(results.getInt("following_id"));
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }
        }

        final ArrayList<Integer> initialCategories = new ArrayList<>();
        if(getCategories){
            try {
                StatementExecutor executor = new StatementExecutor(FIND_CATEGORIES_QUERY);
                executor.execute(ps -> {
                    ps.setInt(1, (userId));

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        initialCategories.add(results.getInt("category_id"));
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }
        }

        final ArrayList<Integer> initialPublishing = new ArrayList<>();
        if(getPublishing){
            try {
                StatementExecutor executor = new StatementExecutor(FIND_PUBLISHING_QUERY);
                executor.execute(ps -> {
                    ps.setInt(1, (userId));

                    ResultSet results = ps.executeQuery();
                    while (results.next()) {
                        initialPublishing.add(results.getInt("category_id"));
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }
        }

        try {
            StatementExecutor executor = new StatementExecutor(GET_USER);
            final int finalUser_id = userId;
            executor.execute(ps -> {

                ps.setInt(1, finalUser_id);

                final ResultSet results = ps.executeQuery();

                if (results.next()) {
                    u[0] = new UserObject(initialCategories, initialFollowers, initialFollowing, initialPublishing, results.getInt("user_id"), results.getString(USERNAME_KEY), results.getString(DISPLAYNAME_KEY),
                            results.getString(EMAIL_KEY), results.getString(THUMBNAIL_KEY), results.getString(COVER_PHOTO_KEY), results.getString(SLOGAN_KEY), results.getInt(PUBLISHER_KEY));
                    u[0].setEmailVerified(results.getInt("email_verified"));
                    u[0].setEmailNotifications(results.getInt("email_notifications"));
                    u[0].setSubscriber(results.getInt("subscriber"));
                }


            });
        } catch (SQLException e) {
            Logging.log("High", e);

        }

        return u[0];
    }
}
