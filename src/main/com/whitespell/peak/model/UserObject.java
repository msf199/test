package main.com.whitespell.peak.model;

import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class UserObject {

    public ArrayList<String> user_following = new ArrayList<>();
    public ArrayList<String> category_following = new ArrayList<>();
    public ArrayList<String> category_publishing = new ArrayList<>();
    int user_id;
    String username;
    String email;
    String thumbnail;
    public UserObject() {
        this.user_id = -1;
        this.username = "";
        this.email = "";
        this.thumbnail = "";
    }
    public UserObject(int user_id, String username, String email, String thumbnail) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.thumbnail = thumbnail;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
