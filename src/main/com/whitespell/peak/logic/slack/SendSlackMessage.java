package main.com.whitespell.peak.logic.slack;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         12/27/15
 *         main.com.whitespell.peak.logic.slack
 */

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.logic.logging.Logging;


/**
 * @@@@ WARNING: @@@@@
 *
 * The SendSlackMessgage object should be called ONLY from the notification thread and NOT from individual endpoints! This might slow the user down.
 *
 */
public class SendSlackMessage {

    private static final String WEBHOOK_URL = "https://hooks.slack.com/services/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";

    private final String message;
    public SendSlackMessage(String message) {
        this.message = message;
    }
    public void sendNotification() {
        try {
            String response = Unirest.post(WEBHOOK_URL)
                     .body("{\n" +
                             "\"text\": \"" + message + "\"\n" +
                             "\n}").asString().getBody();
            Logging.log("Info", "Sent slack notification, "+response+"");
        } catch (UnirestException e) {
           Logging.log("High", e);
        }

    }
}
