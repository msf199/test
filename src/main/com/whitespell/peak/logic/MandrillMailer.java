package main.com.whitespell.peak.logic;

import com.cribbstechnologies.clients.mandrill.exception.RequestFailedException;
import com.cribbstechnologies.clients.mandrill.model.MandrillHtmlMessage;
import com.cribbstechnologies.clients.mandrill.model.MandrillMessageRequest;
import com.cribbstechnologies.clients.mandrill.model.MandrillRecipient;
import com.cribbstechnologies.clients.mandrill.model.response.message.SendMessageResponse;
import com.cribbstechnologies.clients.mandrill.request.MandrillMessagesRequest;
import com.cribbstechnologies.clients.mandrill.request.MandrillRESTRequest;
import com.cribbstechnologies.clients.mandrill.util.MandrillConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.com.whitespell.peak.logic.config.Config;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Troc
 *         7/22/2015
 *         net.eraproject.api.util
 */
public class MandrillMailer {

    private static MandrillRESTRequest request = new MandrillRESTRequest();
    private static MandrillConfiguration config = new MandrillConfiguration();
    private static MandrillMessagesRequest messagesRequest = new MandrillMessagesRequest();
    private static ObjectMapper mapper = new ObjectMapper();
    private static HttpClient client;

    static {
        config.setApiKey(Config.MANDRILL_API_KEY);
        config.setApiVersion(Config.MANDRILL_API_VERSION);
        config.setBaseURL(Config.MANDRILL_API_URL);
        request.setConfig(config);
        request.setObjectMapper(mapper);
        messagesRequest.setRequest(request);
        client = HttpClientBuilder.create().build();
        request.setHttpClient(client);
    }

    public static boolean sendEmail(String fromEmail, String fromName, String subject, String htmlMessage, String toEmail) {
        MandrillMessageRequest mmr = new MandrillMessageRequest();
        MandrillHtmlMessage message = new MandrillHtmlMessage();
        Map<String, String> headers = new HashMap<>();
        message.setFrom_email(fromEmail);
        message.setFrom_name(fromName);
        message.setHeaders(headers);
        message.setHtml(htmlMessage);
        message.setSubject(subject);
        MandrillRecipient[] recipients = new MandrillRecipient[]{new MandrillRecipient(toEmail, toEmail)};
        message.setTo(recipients);
        message.setTrack_clicks(true);
        message.setTrack_opens(true);
        //String[] tags = new String[]{"tag1", "tag2", "tag3"};
        //message.setTags(tags); //TODO: tag implementation? @ pim?
        mmr.setMessage(message);

        try {
            SendMessageResponse response = messagesRequest.sendMessage(mmr);
            return true;
        } catch (RequestFailedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
