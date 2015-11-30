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

    // original content urk
    String contentUrl;

    public String getContentUrl360p() {
        return contentUrl360p;
    }

    public String getContentUrl240p() {
        return contentUrl240p;
    }

    public String getContentUrl144p() {
        return contentUrl144p;
    }

    public String getContentPreview1080p() {
        return contentPreview1080p;
    }

    public String getContentPreview480p() {
        return contentPreview480p;
    }

    public String getContentPreview360p() {
        return contentPreview360p;
    }

    public String getContentPreview240p() {
        return contentPreview240p;
    }

    public String getContentPreview144p() {
        return contentPreview144p;
    }

    public String getThumbnail1080p() {
        return thumbnail1080p;
    }

    public String getThumbnail720p() {
        return thumbnail720p;
    }

    public String getThumbnail480p() {
        return thumbnail480p;
    }

    public String getThumbnail360p() {
        return thumbnail360p;
    }

    public String getThumbnail240p() {
        return thumbnail240p;
    }

    public String getThumbnail144p() {
        return thumbnail144p;
    }

    String contentUrl1080p;
    String contentUrl720p;
    String contentUrl480p;
    String contentUrl360p;
    String contentUrl240p;
    String contentUrl144p;

    //  PREVIEW url
    String contentPreview1080p;
    String contentPreview720p;
    String contentPreview480p;
    String contentPreview360p;
    String contentPreview240p;
    String contentPreview144p;

    //  thumbnail url
    String thumbnail1080p;
    String thumbnail720p;
    String thumbnail480p;
    String thumbnail360p;
    String thumbnail240p;
    String thumbnail144p;

    public String getSocialMediaVideo() {
        return socialMediaVideo;
    }

    String socialMediaVideo;

    public int getVideoLengthSeconds() {
        return videoLengthSeconds;
    }

    int videoLengthSeconds;



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
    int processed = 0;
    int parent = -1;

    public String getContentUrl1080p() {
        return contentUrl1080p;
    }

    public String getContentUrl720p() {
        return contentUrl720p;
    }

    public String getContentUrl480p() {
        return contentUrl480p;
    }

    public String getContentPreview720p() {
        return contentPreview720p;
    }

    public int getHasAccess() {
        return hasAccess;
    }

    public int getProcessed() {
        return processed;
    }

    public int getParent() {
        return parent;
    }

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

    public ContentObject(
                         int categoryId,
                         int userId,
                         int contentId,
                         int contentType,
                         String contentTitle,
                         String contentUrl,
                         String contentUrl1080p,
                         String contentUrl720p,
                         String contentUrl480p,
                         String contentUrl360p,
                         String contentUrl240p,
                         String contentUrl144p,
                         String contentPreview1080p,
                         String contentPreview720p,
                         String contentPreview480p,
                         String contentPreview360p,
                         String contentPreview240p,
                         String contentPreview144p,
                         String thumbnail1080p,
                         String thumbnail720p,
                         String thumbnail480p,
                         String thumbnail360p,
                         String thumbnail240p,
                         String thumbnail144p,
                         int videoLengthSeconds,
                         String socialMediaVideo,
                         String contentDescription,
                         String thumbnailUrl,
                         double contentPrice,
                         int processed,
                         int parent
    ){
        this.userId = userId;
        this.categoryId = categoryId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.contentTitle = contentTitle;
        this.contentUrl = contentUrl;


        this.contentUrl1080p = contentUrl1080p;
        this.contentUrl720p = contentUrl720p;
        this.contentUrl480p = contentUrl480p;
        this.contentUrl360p = contentUrl360p;
        this.contentUrl240p = contentUrl240p;
        this.contentUrl144p = contentUrl144p;



        this.contentPreview1080p = contentPreview1080p;
        this.contentPreview720p = contentPreview720p;
        this.contentPreview480p = contentPreview480p;
        this.contentPreview360p = contentPreview360p;
        this.contentPreview240p = contentPreview240p;
        this.contentPreview144p = contentPreview144p;

        this.thumbnail1080p = thumbnail1080p;
        this.thumbnail720p = thumbnail720p;
        this.thumbnail480p = thumbnail480p;
        this.thumbnail360p = thumbnail360p;
        this.thumbnail240p = thumbnail240p;
        this.thumbnail144p = thumbnail144p;

        this.socialMediaVideo = socialMediaVideo;

        this.videoLengthSeconds = videoLengthSeconds;


        this.contentDescription = contentDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.contentPrice = contentPrice;
        this.processed = processed;
        this.parent = parent;
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

    public void setContentUrl144p(String contentUrl144p) {
        this.contentUrl144p = contentUrl144p;
    }

    public void setContentUrl1080p(String contentUrl1080p) {
        this.contentUrl1080p = contentUrl1080p;
    }

    public void setContentUrl720p(String contentUrl720p) {
        this.contentUrl720p = contentUrl720p;
    }

    public void setContentUrl480p(String contentUrl480p) {
        this.contentUrl480p = contentUrl480p;
    }

    public void setContentUrl360p(String contentUrl360p) {
        this.contentUrl360p = contentUrl360p;
    }

    public void setContentUrl240p(String contentUrl240p) {
        this.contentUrl240p = contentUrl240p;
    }
}