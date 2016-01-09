package main.com.whitespell.peak.logic.endpoints.content.types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/3/2015
 */
public class AddReportingType extends EndpointHandler {


    private static final String PAYLOAD_REPORTING_TYPE_NAME = "reportingTypeName";

    @Override
    protected void setUserInputs() {
        payloadInput.put(PAYLOAD_REPORTING_TYPE_NAME, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    private static final String INSERT_REPORTING_TYPE_QUERY = "INSERT INTO `reporting_type`(`reporting_type_name`) VALUES (?)";

    @Override
    public void safeCall(RequestObject context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        final String reportingTypeName = payload.get(PAYLOAD_REPORTING_TYPE_NAME).getAsString();

        /**
         * Require authentication
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        int[] success = {-1};
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_REPORTING_TYPE_QUERY);
            executor.execute(ps -> {
                ps.setString(1, reportingTypeName);

                int rows = ps.executeUpdate();

                if(rows <= 0){
                    success[0] = 0;
                }else{
                    success[0] = 1;
                }

            });
        } catch (SQLException e) {
            Logging.log("High", e);
            if (e.getMessage().contains("Duplicate entry")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.DUPLICATE_REPORTING_TYPE);
                return;
            }
        }

        if(success[0] == 0){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_ADD_REPORTING_TYPE);
            return;
        }

        context.getResponse().setStatus(HttpStatus.OK_200);
        AddReportingTypeObject object = new AddReportingTypeObject();
        object.setReportingTypeAdded(true);
        Gson g = new Gson();
        String json = g.toJson(object);
        context.getResponse().getWriter().write(json);
        return;
    }

    public class AddReportingTypeObject {

        private boolean reportingTypeAdded;

        public boolean isReportingTypeAdded() {
            return this.reportingTypeAdded;
        }

        public void setReportingTypeAdded(boolean reportingTypeAdded) {
            this.reportingTypeAdded = reportingTypeAdded;
        }

    }
}
