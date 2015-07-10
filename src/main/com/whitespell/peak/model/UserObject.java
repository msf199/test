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
    public ArrayList<String> userFollowing = new ArrayList<>();

    /**
     * A list of the users that are following this user
     */
    public ArrayList<String> usersFollowed = new ArrayList<>();

    /**
     * A list of the categories this user is following
     */
    public ArrayList<String> categoryFollowing = new ArrayList<>();

    /**
     * A list of the categories this user is publishing in
     */
    public ArrayList<String> categoryPublishing = new ArrayList<>();

    int userId;
    String userName;
    String displayName;
    String email;
    String thumbnail;
    String coverPhoto;
    String slogan;

    public UserObject() {
        this.userId = -1;
        this.userName = "";
        this.displayName = "";
        this.email = "";
        this.thumbnail = "";
        this.coverPhoto = "https://www.rmiguides.com/_includes/_images/Everest-Header-7.jpg?v=2014_03_04";
        this.slogan = "";
    }


    public UserObject(int userId, String userName, String displayName, String email, String thumbnail, String cover_photo, String slogan) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.email = email;
        this.thumbnail = thumbnail;
        this.coverPhoto = cover_photo;
        this.slogan = slogan;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int user_id) {
        this.userId = user_id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

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

    public void followUser(String username) { userFollowing.add(username); }

    public void followCategory(String category) { categoryFollowing.add(category); }

    public void publishCategory(String category) { categoryPublishing.add(category); }

    public ArrayList<String> getUserFollowing() { return userFollowing; }

    public ArrayList<String> getUsersFollowed() { return usersFollowed; }

    public ArrayList<String> getCategoryFollowing() {
        return categoryFollowing;
    }

    public ArrayList<String> getCategoryPublishing() {
        return categoryPublishing;
    }
}
