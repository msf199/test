package whitespell.model;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class AuthenticationObject {

    String key;

    int user_id;

    long expires = -1;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public int getUserId() {
        return this.user_id;
    }

}
