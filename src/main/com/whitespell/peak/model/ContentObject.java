package main.com.whitespell.peak.model;

import java.util.ArrayList;

/**
 * @author  Cory McAn for Whitespell
 *          6/23/2015
 *          whitespell.model
 */
public class ContentObject {

    /**
     * List of categories this content belongs to
     */

    int userId;
    int contentId;
    int contentType;
    int categoryId;
    String contentTitle;
    String contentUrl;
    String contentDescription;
    int likes = 100;
    String thumbnailUrl;

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

    public ContentObject(int contentId, int contentType, String contentTitle,
                         String contentUrl, String contentDescription, String thumbnailUrl) {
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

    public String getThumbnailUrl() { return thumbnailUrl; }
}