package main.com.whitespell.peak.model;

import main.com.whitespell.peak.logic.logging.Logging;

import java.sql.Timestamp;

/**
 * @author  Cory McAn for Whitespell
 *          8/5/2015
 *          whitespell.model
 */
public class CommentObject implements Comparable<CommentObject>{

    public CommentObject(){
        this.commentId = -1;
        this.contentId = -1;
        this.likes = 0;
        this.comment = "";
        this.timestamp = null;
    }

    public CommentObject(int contentId, String comment, Timestamp timestamp){
        this.contentId = contentId;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    int commentId;
    int contentId;
    int likes;
    String comment;
    Timestamp timestamp;
    UserObject poster;

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public UserObject getPoster() {
        return poster;
    }

    public void setPoster(UserObject poster) {
        this.poster = poster;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }


    /**
     * Comparator for dates to allow the comments list to be sorted by timestamp and time posted.
     */
    public int compareTo(CommentObject o) {

        CommentObject other = o;
        Timestamp date1 = null;
        Timestamp date2 = null;
        try {
            date1 = this.getTimestamp();
            date2 = other.getTimestamp();
        }
        catch(Exception e){
            Logging.log("High", e);
        }

        return date1.compareTo(date2);
    }
}
