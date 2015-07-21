package main.com.whitespell.peak.model;

/**
 * Created by cory on 15/07/15.
 */
public class NewsfeedObject {


    public int newsfeed_id;
    public String newsfeed_object;
    public UserObject user;
    public ContentObject content;

    public NewsfeedObject(int newsfeed_id, UserObject following, ContentObject content){
        this.newsfeed_id = newsfeed_id;
        user = following;
        this.content = content;
    }

    public NewsfeedObject(int newsfeed_id, String newsfeed_object) {
        this.newsfeed_id = newsfeed_id;
        this.newsfeed_object = newsfeed_object;
    }

    public NewsfeedObject(String newsfeed_object) {
        this.newsfeed_object = newsfeed_object;
        this.newsfeed_id = 0;
    }

    public NewsfeedObject() {
        this.newsfeed_object = "";
        this.newsfeed_id = 0;
    }

    public int getNewsfeed_id() {
        return newsfeed_id;
    }

    public void setNewsfeed_id(int newsfeed_id) {
        this.newsfeed_id = newsfeed_id;
    }


    public String getNewsfeed_object() {
        return newsfeed_object;
    }

    public void setNewsfeed_object(String newsfeed_object) {
        this.newsfeed_object = newsfeed_object;
    }


}
