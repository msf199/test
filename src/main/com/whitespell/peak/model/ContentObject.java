package main.com.whitespell.peak.model;

/**
 * @author  Cory McAn for Whitespell
 *          6/23/2015
 *          whitespell.model
 */
public class ContentObject {
    int content_type;
    String content_title;
    String content_url;
    String content_description;

    public ContentObject(int content_type, String content_title,
                         String content_url, String content_description) {
        this.content_type = content_type;
        this.content_title = content_title;
        this.content_url = content_url;
        this.content_description = content_description;
    }

    public int getContent_type() { return content_type; }

    public String getContent_title() { return content_title; }

    public String getContent_url() { return content_url; }

    public String getContent_description() {
        return content_description;
    }
}