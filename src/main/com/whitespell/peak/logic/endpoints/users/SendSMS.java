package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;
import main.com.whitespell.peak.StaticRules;
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
public class SendSMS extends EndpointHandler {


    public static String PAYLOAD_NUMBER_KEY = "phoneNumber";

    private static String ACCOUNT_SID = "ACc499e1798e0e0e87609f4085e2fb4a8c";
    private static String ACCOUNT_TOKEN = "865157b3bb805b5a2c94cc8d1f348608";

    /**
     * Live AccountSID
     Used to exercise the REST API
     ACada6f79de791dd1fa03f595ca9979601

     Live AuthToken
     Keep this somewhere safe and secure
     552ff644a8af36ad2ebc24eb0af477a6
     */

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_NUMBER_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();

        Phonenumber.PhoneNumber phone = new Phonenumber.PhoneNumber();
        String number = null;

        if(j.get(PAYLOAD_NUMBER_KEY) != null){
            number = j.get(PAYLOAD_NUMBER_KEY).getAsString();
        }


        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, ACCOUNT_TOKEN);

        Account account = client.getAccount();

        MessageFactory messageFactory = account.getMessageFactory();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", number)); // Replace with a valid phone number for your account.
        params.add(new BasicNameValuePair("From", "+12013350570")); // Replace with a valid phone number for your account.
        params.add(new BasicNameValuePair("Body", "Upfit Android Download: http://bit.ly/1PrTgab, Upfit iOS Download: http://apple.co/1QNUVFB"));
        try {
            Message sms = messageFactory.create(params);
        } catch (TwilioRestException e) {
            Logging.log("Twilio error", e);
        }


        Gson g = new Gson();
        SmsObject p = new SmsObject();
        String response = g.toJson(p);
        context.getResponse().setStatus(200);
        context.getResponse().getWriter().write(response);

    }

    public class SmsObject {
        boolean ping = true;
    }

}
