package whitespell.model;

import java.util.HashMap;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/13/15
 *         whitespell.model
 */
public abstract class UnitProcess {
    private Unit parentAd;
    private Session parentSession;

    public UnitProcess(Unit parentAd, Session parentSession) {
        this.parentAd = parentAd;
        this.parentSession = parentSession;
    }
    private HashMap<String, SessionEventHandler> keyHandlerStorage = new HashMap<>();

    public Session getParentSession() {
        return this.parentSession;
    }
    public Unit getParentAd() {
        return this.parentAd;
    }


    protected abstract void bindHandlers();

    protected HashMap<String, SessionEventHandler> processEventHandlers = new HashMap<>();

    protected abstract void process();

    public void addEventHandler(String event, SessionEventHandler s) {
        processEventHandlers.put(event, s);
    }

    protected abstract class SessionEventHandler {
        protected abstract void handle();
    }
}
