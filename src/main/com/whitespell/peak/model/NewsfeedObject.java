package main.com.whitespell.peak.model;

/**
 * Created by pim on 7/15/15.
 */
public class NewsfeedObject implements Comparable<NewsfeedObject> {

    public NewsfeedObject(long newsfeedId, ContentObject content) {
        this.newsfeedId = newsfeedId;
        this.content= content;
    }

    public long newsfeedId;
    public ContentObject content;

    public ContentObject getNewsfeedContent() {
        return content;
    }

    public long getNewsfeedId() {
        return newsfeedId;
    }

    public void setNewsfeedId(long newsfeedId) {
        this.newsfeedId = newsfeedId;
    }

    /**
     * Comparator for newsfeedids.
     */
    public int compareTo(NewsfeedObject o) {
        return this.getNewsfeedId() > o.getNewsfeedId() ? 1 : -1;
    }
}

