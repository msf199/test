package main.com.whitespell.peak.model;

/**
 * Created by cory on 15/07/15.
 */
public class NewsfeedObject {
    public NewsfeedObject(int newsfeed_id, UserObject user, ContentObject content) {
        this.newsfeed_id = newsfeed_id;
        this.user = user;
        this.content= content;
    }

    public int newsfeed_id;
    public UserObject user;
    public ContentObject content;
}
