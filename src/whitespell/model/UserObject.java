package whitespell.model;

import whitespell.net.websockets.socketio.SocketIOClient;

import java.util.List;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class UserObject {

    public UserObject (int user_id, String username, String email, String thumbnail) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.thumbnail = thumbnail;
    }

    int user_id;
    String username;
    String email;
    String thumbnail;

    public int getUserId() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
    public String getThumbnail() {
        return thumbnail;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
