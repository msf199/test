package whitespell.model;

import whitespell.net.websockets.socketio.SocketIOClient;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/14/15
 *         whitespell.model
 */

/**
 * The unit class is generally instanced for each session. A session can have multiple unit instances because he or she can have
 * multiple pages open with different units on each. Each ad has an UUID, the UUID
 */

public class Unit {

    /**
     * The SocketIOClient is the client to which the unit is shown. This is a direct connection that is initiated at the same time
     * as the first HTTP call to the API. Once the HTTP call returns the session, the session is sent over the websocket connection.
     */
    private SocketIOClient client;
    public SocketIOClient getClient() {
        return client;
    }

    private final String uuid;

    public final String getUUID() {
        return uuid;
    }

    public Unit(String uuid) {
        this.uuid = uuid;
    }

    private UnitProcess process;

    public void setProcess(UnitProcess p) {
        this.process = p;
    }

}
