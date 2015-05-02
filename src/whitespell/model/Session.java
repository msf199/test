package whitespell.model;

import whitespell.logic.UnitHandler;
import whitespell.net.websockets.socketio.SocketIOClient;

import java.util.HashMap;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public class Session {

    /**
     * The Session object is responsible for placing itself at the right endpoint. For example, a session comes in with id x at Endpoint x,
     * You can have multiple Unit displays per session on different pages. Therefore the session object has a hashmap of ads.
     * Each ad has it's own websocket connection. Each session stores the user, and the ad stores the websocket client.
     */

    private User user;

    /**
     * An ad can be shown multiple times within 1 session, and 1 session can contain multiple of the same or unique ads. A random key is generated
     * on each request for an ad, and that key is returned in the HTTP header and sent out back over the websocket connection to find the right ad.
     *
     * The map is <RequestUUID(String), Unit>
     */
    private HashMap<String, Unit> ads = new HashMap<>();

    private long startTime;

    /**
     * A session is initiated once for one page. So if there are 3 ads on the page, only 1 session
     * @param user
     * @param client
     */
    public Session(String uuid, User user, SocketIOClient client) {
        this.user = user;
        this.startTime = System.currentTimeMillis();
    }

    public Unit getAd(String adId) {
        return ads.get(adId);
    }

    /**
     * putPersonalizedAd first check if the ad exists and then takes over the static details from the ad model (such as the name and maybe some other vars)
     *
     * @param requestUUID     The request ID that comes with each API call for an ad. The request ID is returned in the header so that we can map a websocket to an ad.
     * @param adUUID          The unique ID for the ad that is being shown. This way we can make sure we show unique ads.
     */

    public void putPersonalizedAd(String requestUUID, String adUUID) {
        Unit adBase = UnitHandler.getAd(adUUID);
        if(adBase != null) {
            ads.put(requestUUID, new Unit(adUUID));
        } else {
            System.out.println("(putPersonalizedAd) Request made for ad that doesn't exist with uuid: " + adUUID);
        }
    }
    public long getStartTime() {
        return this.startTime;
    }

}
