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
    public ArrayList<Integer> userFollowers = null;

    /**
     * A list of the categories this user is following
     */
    public ArrayList<Integer> categoryFollowing = null;

    /**
     * A list of the categories this user is publishing in
     */
    public ArrayList<Integer> categoryPublishing = null;

    int userId;
    int publisher;
    int emailVerified;
    int emailNotification;
    String userName;
    String displayName;
    String email;
    String thumbnail;
    String coverPhoto;
    String slogan;

    public UserObject() {
        this.userId = -1;
        this.userName = null;
        this.displayName = null;
        this.email = null;
        this.thumbnail = null;
        this.coverPhoto = null;
        this.slogan = null;
        this.publisher = 0;
    }

    public UserObject(ArrayList<Integer> categoryFollowing, ArrayList<Integer> userFollowers, ArrayList<Integer> userFollowing, ArrayList<Integer> categoryPublishing, int userId, String userName, String displayName, String email, String thumbnail, String cover_photo, String slogan, int publisher) {
        this.categoryFollowing = categoryFollowing;
        this.userFollowers = userFollowers;
        this.userFollowing = userFollowing;
        this.categoryPublishing = categoryPublishing;
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.email = email;
        this.thumbnail = thumbnail;
        this.coverPhoto = cover_photo;
        this.slogan = slogan;
        this.publisher = publisher;
    }

    public UserObject(int userId, String userName, String displayName, String email, String thumbnail, String cover_photo, String slogan, int publisher) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.email = email;
        this.thumbnail = thumbnail;
        this.coverPhoto = cover_photo;
        this.slogan = slogan;
        this.publisher = publisher;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPublisher() {
        return publisher;
    }

    public void setPublisher(int publisher) {
        this.publisher = publisher;
    }

    public int getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(int emailVerified) {
        this.emailVerified = emailVerified;
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

    public int getEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(int emailNotification) {
        this.emailNotification = emailNotification;
    }

    public ArrayList<Integer> getUserFollowing() { return userFollowing; }

    public ArrayList<Integer> getUserFollowers() { return userFollowers; }

    public ArrayList<Integer> getCategoryFollowing() {
        return categoryFollowing;
    }

    public ArrayList<Integer> getCategoryPublishing() {
        return categoryPublishing;
    }
}
