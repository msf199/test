package main.com.whitespell.peak.model;

/**
 * Created by cory on 15/07/15.
 */
public class NewsfeedObject {


    public int newsfeed_id;
    public UserObject user;
    public ContentObject content;

    public NewsfeedObject() {
        this.newsfeed_id = 0;
    }

    public NewsfeedObject(int newsfeed_id, UserObject following, ContentObject content){
        this.newsfeed_id = newsfeed_id;
        user = following;
        this.content = content;
    }

    public int getNewsfeed_id() {
        return newsfeed_id;
    }

    public void setNewsfeed_id(int newsfeed_id) {
        this.newsfeed_id = newsfeed_id;
    }

    public UserObject getNewsfeedUser(){
        return user;
    }

    public ContentObject getNewsfeedContent(){
        return content;
    }
}
