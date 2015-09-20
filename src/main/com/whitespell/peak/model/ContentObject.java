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
    String thumbnailUrl;
    int userLiked = 0;
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

    public UserObject getPoster() {
        return poster;
    }

    public void setPoster(UserObject poster) {
        this.poster = poster;
    }

    public void addChild(ContentObject child) {
        this.children.add(child);
    }
}