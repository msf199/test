package main.com.whitespell.peak.model;

import main.com.whitespell.peak.StaticRules;

import java.util.ArrayList;

/**
 * @author  Cory McAn for Whitespell
 *          6/23/2015
 *          whitespell.model
 */
public class ContentObject {

    int userId;
    int contentId;
    int contentType;
    int categoryId;
    String contentTitle;
    String contentUrl;
    String contentDescription;
    int likes = 0;
    int views = 0;
    String thumbnailUrl;
    int userLiked = 0;
    int userSaved = 0;
    int userViewed = 0;
    double contentPrice = 0.00;
    int hasAccess = 0;
    int recommended = 0;
    UserObject poster;
    ArrayList<ContentObject> children = new ArrayList<>();

    public boolean hasEmptyBundleChildren() {
        for(ContentObject c : children) {
            if(c.getContentType() == StaticRules.BUNDLE_CONTENT_TYPE) {
                if(c.getChildren() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public ContentObject(int categoryId, int userId, int contentId, int contentType, String contentTitle,
                         String contentUrl, String contentDescription, String thumbnailUrl, double contentPrice){
        this.userId = userId;
        this.categoryId = categoryId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.contentTitle = contentTitle;
        this.contentUrl = contentUrl;
        this.contentDescription = contentDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.contentPrice = contentPrice;
    }

    public ContentObject(int categoryId, int userId, int contentId, int contentType, String contentTitle,
                         String contentUrl, String contentDescription, String thumbnailUrl){
        this.userId = userId;
        this.categoryId = categoryId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.contentTitle = contentTitle;
        this.contentUrl = contentUrl;
        this.contentDescription = contentDescription;
        this.thumbnailUrl = thumbnailUrl;
    }

    public ContentObject(int userId, int contentId, int contentType, String contentTitle,
                         String contentUrl, String contentDescription, String thumbnailUrl) {
        this.userId = userId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.contentTitle = contentTitle;
        this.contentUrl = contentUrl;
        this.contentDescription = contentDescription;
        this.thumbnailUrl = thumbnailUrl;
    }


    public void setChildren(ArrayList<ContentObject> children) {
        this.children = children;
    }
    public ArrayList<ContentObject> getChildren() {
        return this.children;
    }

    public int getUserId() {
        return userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getContentId() {
        return contentId;
    }

    public int getContentType() { return contentType; }

    public String getContentTitle() { return contentTitle; }

    public String getContentUrl() { return contentUrl; }

    public String getContentDescription() { return contentDescription; }

    public int getLikes() { return likes; }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getThumbnailUrl() { return thumbnailUrl; }

    public int getUserLiked() {
        return userLiked;
    }

    public void setUserLiked(int userLiked) {
        this.userLiked = userLiked;
    }

    public double getContentPrice() {
        return contentPrice;
    }

    public void setContentPrice(double contentPrice) {
        this.contentPrice = contentPrice;
    }

    public UserObject getPoster() {
        return poster;
    }

    public void setPoster(UserObject poster) {
        this.poster = poster;
    }

    public int hasAccess() {
        return hasAccess;
    }

    public int getRecommended() {
        return recommended;
    }

    public void setRecommended(int recommended) {
        this.recommended = recommended;
    }

    public int getUserSaved() {
        return userSaved;
    }

    public void setUserSaved(int userSaved) {
        this.userSaved = userSaved;
    }

    public int getUserViewed() {
        return userViewed;
    }

    public void setUserViewed(int userViewed) {
        this.userViewed = userViewed;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setHasAccess(int hasAccess) {
        this.hasAccess = hasAccess;
    }

    public void addChild(ContentObject child) {
        this.children.add(child);
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }
}