package main.com.whitespell.peak.model;

import main.com.whitespell.peak.logic.logging.Logging;

/**
 * @author  Cory McAn for Whitespell
 *          8/5/2015
 *          whitespell.model
 */
public class CommentObject implements Comparable<CommentObject>{

    public CommentObject(){
        this.userId = -1;
        this.contentId = -1;
        this.likes = 0;
        this.comment = "";
        this.date = null;
    }

    public CommentObject(int contentId, int userId, String comment, DateObject date){
        this.contentId = contentId;
        this.userId = userId;
        this.comment = comment;
        this.date = date;
    }

    int contentId;
    int userId;
    int likes;
    String comment;
    DateObject date;

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public DateObject getDate() {
        return date;
    }

    public void setDate(DateObject date) {
        this.date = date;
    }

    /**
     * Comparator for dates to allow the comments list to be sorted by date and time posted.
     */
    public int compareTo(CommentObject o) {

        CommentObject other = o;
        DateObject date1 = null;
        DateObject date2 = null;
        try {
            date1 = this.getDate();
            date2 = other.getDate();
        }
        catch(Exception e){
            Logging.log("High", e);
        }

        return date1.compareTo(date2);
    }
}
