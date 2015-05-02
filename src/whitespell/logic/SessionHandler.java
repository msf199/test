package whitespell.logic;

import whitespell.model.Session;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.logic
 */
public class SessionHandler {

    public static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    /**
     * Adding new sessions is done when a HTTP request comes in without a session header. The websocket connection should already be established
     * at that time, and the session ID is sent over the web socket and associated with the session.
     */

    public static String generateSessionId() {
        String uuid = UUID.randomUUID().toString();
        sessions.put(uuid, new Session(uuid, null, null));
        return uuid;
    }

    public static Session getSession(String key) {
        return sessions.get(key);
    }
    public static boolean sessionExists(String key) {
        return sessions.containsKey(key);
    }
}
