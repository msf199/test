package main.com.whitespell.peak.logic.endpoints.ping;

import com.google.gson.Gson;
import main.com.whitespell.peak.logic.EndpointInterface;
import main.com.whitespell.peak.logic.RequestObject;

import java.io.IOException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */

/**
 * Simple ping endpoint to ensure the server is online. In the future we will add things such as
 * Request in the last minute.. etc.
 */
public class Ping extends EndpointInterface {

    @Override
    public void call(final RequestObject context) throws IOException {

        Gson g = new Gson();
        PingObject p = new PingObject();
        String response = g.toJson(p);
        context.getResponse().setStatus(200);
        context.getResponse().getWriter().write(response);

    }

    public class PingObject {
        boolean ping = true;
    }

}
