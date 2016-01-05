package main.com.whitespell.peak.logic.notifications;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.UpdateStatus;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.notifications.impl.MassNotification;

import java.io.IOException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         1/4/16
 *         main.com.whitespell.peak.logic.notifications
 */
public class iOSMassNotification extends EndpointHandler {

    private static final String PAYLOAD_TEXT_KEY = "text";
    private static final String PAYLOAD_PASSWORD_KEY = "password";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_TEXT_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(PAYLOAD_PASSWORD_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();
        String text = j.get(PAYLOAD_TEXT_KEY).getAsString();
        String password = j.get(PAYLOAD_PASSWORD_KEY).getAsString();

        /**
         * Master password is required.
         */
        if (!password.equals(StaticRules.MASTER_PASS)) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Send notification to specified devices.
         */
        Server.NotificationService.offerNotification(new MassNotification(text, Config.IOS_DEVICE_TYPE_ID));

        UpdateStatus status = new UpdateStatus("success");
        Gson g = new Gson();
        String response = g.toJson(status);
        context.getResponse().setStatus(200);
        try {
            context.getResponse().getWriter().write(response);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }
}
