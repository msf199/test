package main.com.whitespell.peak.model.authentication;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/21/15
 *         main.com.whitespell.peak.model.authentication
 */
public class AuthenticationObject {

    String key;

    int user_id;

    long expires = -1;

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getUserId() {
        return this.user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

}
