package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/1/2015
 */
public class ReportUser extends EndpointHandler {

    private static final String REPORT_USER_ID = "userId";
    private static final String REPORT_TYPE_ID = "typeId";
    private static final String REPORT_MESSAGE = "message";

    private static final String INSERT_REPORT = "INSERT INTO `reporting`(`submitter_id`,`user_id`,`type_id`,`message`) VALUES(?,?,?)";

    @Override
    protected void setUserInputs() {
        urlInput.put(REPORT_USER_ID, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(REPORT_TYPE_ID, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(REPORT_MESSAGE, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        
    }
}