package main.com.whitespell.peak.model;

import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class UserObject {

    /**
     * A list of the users this user is following
     */
    public ArrayList<String> user_following = new ArrayList<>();

    /**
     * A list of the categories this user is following
     */
    public ArrayList<String> category_following = new ArrayList<>();


    /**
     * A list of the categories this user is publishing in
     */
    public ArrayList<String> category_publishing = new ArrayList<>();

    int user_id;
    String username;
    String email;
    String thumbnail;
    String cover_photo;
    String slogan;

    public UserObject() {
        this.user_id = -1;
        this.username = "";
        this.email = "";
        this.thumbnail = "";
        this.cover_photo = "https://www.rmiguides.com/_includes/_images/Everest-Header-7.jpg?v=2014_03_04";
        this.slogan = "Fitness is 24/7.";
    }

    public UserObject(int user_id, String username, String email, String thumbnail, String slogan) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.thumbnail = thumbnail;
        this.slogan = slogan;
    }

    public UserObject(int user_id, String username, String email, String thumbnail, String cover_photo, String slogan) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.thumbnail = thumbnail;
        this.cover_photo = cover_photo;
        this.slogan = slogan;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSlogan() { return slogan; }

    public void setSlogan(String slogan) { this.slogan = slogan; }

    public String getCover_photo() { return cover_photo; }

    public void setCover_photo(String cover_photo) { this.cover_photo = cover_photo; }

    public void followUser(String username) { user_following.add(username); }

    public void followCategory(String category) { category_following.add(category); }

    public void publishCategory(String category) { category_publishing.add(category); }

    public ArrayList<String> getUser_following() { return user_following; }

    public ArrayList<String> getCategory_following() {
        return category_following;
    }

    public ArrayList<String> getCategory_publishing() {
        return category_publishing;
    }
}
