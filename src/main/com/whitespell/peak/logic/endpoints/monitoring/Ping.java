package main.com.whitespell.peak.logic.endpoints.monitoring;

import com.google.gson.Gson;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */

/**
 * Simple ping endpoint to ensure the server is online. In the future we will add things such as
 * Request in the last minute.. etc.
 */
public class Ping extends EndpointHandler {

    @Override
    public void safeCall(final RequestObject context) throws IOException {
        Gson g = new Gson();
        PingObject p = new PingObject();
        String response = g.toJson(p);
        context.getResponse().setStatus(200);
        context.getResponse().getWriter().write(response);

    }

    @Override
    protected void setUserInputs() {

    }

    public class PingObject {
        boolean ping = true;
    }

}
