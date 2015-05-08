package whitespell.model.baseapi;

import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.net.websockets.socketio.SocketIOClient;

import java.util.HashMap;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public abstract class Endpoint implements ApiInterface {

    public abstract void call(RequestContext context);

}
