package main.com.whitespell.peak.logic;

import com.cribbstechnologies.clients.mandrill.exception.RequestFailedException;
import com.cribbstechnologies.clients.mandrill.model.*;
import com.cribbstechnologies.clients.mandrill.request.MandrillMessagesRequest;
import com.cribbstechnologies.clients.mandrill.request.MandrillRESTRequest;
import com.cribbstechnologies.clients.mandrill.request.MandrillTemplatesRequest;
import com.cribbstechnologies.clients.mandrill.util.MandrillConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.com.whitespell.peak.logic.config.Config;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Troc & Cory McAn(cmcan), Whitespell LLC
 *         7/22/2015
 */
public class MandrillMailer {

    private static MandrillTemplatesRequest templatesRequest = new MandrillTemplatesRequest();
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
        templatesRequest.setRequest(request);
    }

    public static boolean sendTokenTemplatedMessage(String fromEmail, String fromName, String subject, String host, String username, String token, String templateName, String htmlName, String toEmail) {
        MandrillTemplatedMessageRequest request = new MandrillTemplatedMessageRequest();
        MandrillMessage message = new MandrillMessage();
        Map<String, String> headers = new HashMap<>();
        message.setFrom_email(fromEmail);
        message.setFrom_name(fromName);
        message.setHeaders(headers);
        message.setSubject(subject);
        MandrillRecipient[] recipients = new MandrillRecipient[]{new MandrillRecipient(toEmail, toEmail)};
        message.setTo(recipients);
        message.setTrack_clicks(true);
        message.setTrack_opens(true);

        request.setMessage(message);
        List<TemplateContent> content = new ArrayList<>();
        request.setTemplate_content(content);
        request.setTemplate_name(templateName);
        List<MergeVar> globalMergeVars = new ArrayList<>();
        globalMergeVars.add(new MergeVar("PLATFORM", Config.PLATFORM_NAME));
        globalMergeVars.add(new MergeVar("CONTACT_EMAIL", Config.PLATFORM_EMAIL_SEND_ADDRESS));
        globalMergeVars.add(new MergeVar("NAME", username));
        globalMergeVars.add(new MergeVar("HOST", host));
        globalMergeVars.add(new MergeVar("TOKEN", token));
        globalMergeVars.add(new MergeVar("URL", Config.PLATFORM_HOME_PAGE_URL+ "/email/" + htmlName + ".html?token=" + token + "&username=" + username));
        message.setGlobal_merge_vars(globalMergeVars);

        try {
            messagesRequest.sendTemplatedMessage(request);
            return true;
        } catch (RequestFailedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendContentNotificationTemplatedMessage(String fromEmail, String fromName, String subject, String host, String username, String contentName, String contentUrl, String templateName, String thumbnailUrl, String toEmail) {
        MandrillTemplatedMessageRequest request = new MandrillTemplatedMessageRequest();
        MandrillMessage message = new MandrillMessage();
        Map<String, String> headers = new HashMap<>();
        message.setFrom_email(fromEmail);
        message.setFrom_name(fromName);
        message.setHeaders(headers);
        message.setSubject(subject);
        MandrillRecipient[] recipients = new MandrillRecipient[]{new MandrillRecipient(toEmail, toEmail)};
        message.setTo(recipients);
        message.setTrack_clicks(true);
        message.setTrack_opens(true);

        request.setMessage(message);
        List<TemplateContent> content = new ArrayList<>();
        request.setTemplate_content(content);
        request.setTemplate_name(templateName);
        List<MergeVar> globalMergeVars = new ArrayList<>();
        globalMergeVars.add(new MergeVar("PLATFORM", Config.PLATFORM_NAME));
        globalMergeVars.add(new MergeVar("CONTACT_EMAIL", Config.PLATFORM_EMAIL_SEND_ADDRESS));
        globalMergeVars.add(new MergeVar("NAME", username));
        globalMergeVars.add(new MergeVar("HOST", host));
        globalMergeVars.add(new MergeVar("UPLOADER_THUMB", thumbnailUrl));
        globalMergeVars.add(new MergeVar("VIDEO_NAME", contentName));
        globalMergeVars.add(new MergeVar("CONTENT_URL", contentUrl));
        message.setGlobal_merge_vars(globalMergeVars);

        try {
            messagesRequest.sendTemplatedMessage(request);
            return true;
        } catch (RequestFailedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
