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
    public ArrayList<Integer> userFollowing = null;
    /**
     * A list of the users that are following this user
     */
    public ArrayList<Integer> usersFollowed = null;

    /**
     * A list of the categories this user is following
     */
    public ArrayList<Integer> categoryFollowing = null;

    /**
     * A list of the categories this user is publishing in
     */
    public ArrayList<Integer> categoryPublishing = null;

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
        this.coverPhoto = "";
        this.slogan = "";
    }

    public UserObject(ArrayList<Integer> categoryFollowing, ArrayList<Integer> userFollowing, int userId, String userName, String displayName, String email, String thumbnail, String cover_photo, String slogan) {
        this.categoryFollowing = categoryFollowing;
        this.userFollowing = userFollowing;
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.email = email;
        this.thumbnail = thumbnail;
        this.coverPhoto = cover_photo;
        this.slogan = slogan;
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

    public void setCoverPhoto(String coverPhoto) { this.coverPhoto = coverPhoto; }

    public void followUser(int userId) { userFollowing.add(userId); }

    public void followCategory(int categoryId) { categoryFollowing.add(categoryId); }

    public void publishCategory(int categoryId) { categoryPublishing.add(categoryId); }

    public ArrayList<Integer> getUserFollowing() { return userFollowing; }

    public ArrayList<Integer> getUsersFollowed() { return usersFollowed; }

    public ArrayList<Integer> getCategoryFollowing() {
        return categoryFollowing;
    }

    public ArrayList<Integer> getCategoryPublishing() {
        return categoryPublishing;
    }
}
