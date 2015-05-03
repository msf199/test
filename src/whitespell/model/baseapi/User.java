package whitespell.model.baseapi;

import whitespell.net.websockets.socketio.SocketIOClient;

import java.util.List;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class User {

    private List<String> sessionUUIDs;
    private List<String> ips;

    public User() {
    }

    public void addSession(String session) {

    }
}
