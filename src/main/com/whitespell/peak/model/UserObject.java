package main.com.whitespell.peak.model;

import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
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
    String displayname;
    String email;
    String thumbnail;
    String coverPhoto;
    String slogan;

    public UserObject() {
        this.user_id = -1;
        this.username = "";
        this.displayname = "";
        this.email = "";
        this.thumbnail = "";
        this.coverPhoto = "https://www.rmiguides.com/_includes/_images/Everest-Header-7.jpg?v=2014_03_04";
        this.slogan = "";
    }


    public UserObject(int user_id, String username, String displayname, String email, String thumbnail, String cover_photo, String slogan) {
        this.user_id = user_id;
        this.username = username;
        this.displayname = displayname;
        this.email = email;
        this.thumbnail = thumbnail;
        this.coverPhoto = cover_photo;
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

    public String getDisplayname() { return displayname; }

    public void setDisplayname(String displayname) { this.displayname = displayname; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getSlogan() { return slogan; }

    public void setSlogan(String slogan) { this.slogan = slogan; }

    public String getCoverPhoto() { return coverPhoto; }

    public void setCoverPhoto(String cover_photo) { this.coverPhoto = cover_photo; }

    public void followUser(String username) { user_following.add(username); }

    public void followCategory(String category) { category_following.add(category); }

    public void publishCategory(String category) { category_publishing.add(category); }

    public ArrayList<String> getUserFollowing() { return user_following; }

    public ArrayList<String> getCategoryFollowing() {
        return category_following;
    }

    public ArrayList<String> getCategoryPublishing() {
        return category_publishing;
    }
}
