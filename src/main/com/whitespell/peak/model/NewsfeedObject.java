package main.com.whitespell.peak.model;

/**
 * Created by cory on 15/07/15.
 */
public class NewsfeedObject {


    public int newsfeedId;
    public UserObject user;
    public ContentObject content;

    public NewsfeedObject() {
        this.newsfeedId = 0;
    }

    public NewsfeedObject(int newsfeedId, UserObject following, ContentObject content){
        this.newsfeedId = newsfeedId;
        user = following;
        this.content = content;
    }

    public int getNewsfeedId() {
        return newsfeedId;
    }

    public void setNewsfeedId(int newsfeedId) {
        this.newsfeedId = newsfeedId;
    }

    public UserObject getNewsfeedUser(){
        return user;
    }

    public ContentObject getNewsfeedContent(){
        return content;
    }
}
