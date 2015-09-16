package main.com.whitespell.peak.model;

/**
 * Created by pim on 7/15/15.
 */
public class NewsfeedObject implements Comparable<NewsfeedObject> {

    public NewsfeedObject(long newsfeedId, UserObject user, ContentObject content) {
        this.newsfeedId = newsfeedId;
        this.user = user;
        this.content= content;
    }

    public long newsfeedId;
    public UserObject user;
    public ContentObject content;

    final static int TIMESTAMP_LENGTH = 13;

    public ContentObject getNewsfeedContent() {
        return content;
    }
    public UserObject getNewsfeedUser() {
        return user;
    }

    public long getNewsfeedId() {
        return newsfeedId;
    }

    public void setNewsfeedId(long newsfeedId) {
        this.newsfeedId = newsfeedId;
    }

    public boolean hasContent(long contentId){
        String stringNewsfeedId = String.valueOf(newsfeedId);
        String currContentId = String.valueOf(contentId);

        String newsfeedContentId = stringNewsfeedId.substring(TIMESTAMP_LENGTH);

        if(newsfeedContentId.equals(currContentId)){
            return true;
        }
        return false;
    }

    /**
     * Comparator for newsfeedids.
     */
    public int compareTo(NewsfeedObject o) {
        return this.getNewsfeedId() > o.getNewsfeedId() ? 1 : -1;
    }
}

