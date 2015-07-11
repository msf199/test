package main.com.whitespell.peak.model;

/**
 * @author  Cory McAn for Whitespell
 *          6/23/2015
 *          whitespell.model
 */
public class ContentObject {

    int contentId;
    int contentType;
    String contentTitle;
    String contentUrl;
    String contentDescription;
    int likes = 100;
    String thumbnail = "https://s-media-cache-ak0.pinimg.com/originals/c2/1e/ce/c21ecebc560514fe3e48ca5eef1c09b8.jpg";

    public ContentObject(int contentId, int contentType, String contentTitle,
                         String contentUrl, String contentDescription, String thumbnail) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.contentTitle = contentTitle;
        this.contentUrl = contentUrl;
        this.contentDescription = contentDescription;
        this.thumbnail = thumbnail;
    }

    public int getContentType() { return contentType; }

    public String getContentTitle() { return contentTitle; }

    public String getContentUrl() { return contentUrl; }

    public String getContentDescription() { return contentDescription; }

    public int getLikes() { return likes; }

    public String getThumbnail() { return thumbnail; }
}