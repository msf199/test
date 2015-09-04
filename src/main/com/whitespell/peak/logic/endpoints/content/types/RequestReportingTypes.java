package main.com.whitespell.peak.logic.endpoints.content.types;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ReportingTypeObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/3/2015
 */
public class RequestReportingTypes extends EndpointHandler {


    private static final String GET_REPORTING_TYPES = "SELECT * FROM `reporting_type`";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        /**
         * Require authentication
         */
        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Get the reporting types
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_REPORTING_TYPES);
            executor.execute(ps -> {

                final ResultSet results = ps.executeQuery();
                ArrayList<ReportingTypeObject> reportingTypes = new ArrayList<>();
                while (results.next()) {

                    ReportingTypeObject d = new ReportingTypeObject(results.getInt("reporting_type_id"), results.getString("reporting_type_name"));
                    reportingTypes.add(d);
                }

                Gson g = new Gson();
                String response = g.toJson(reportingTypes);
                context.getResponse().setStatus(200);
                try {
                    context.getResponse().getWriter().write(response);
                } catch (Exception e) {
                    Logging.log("High", e);
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    @Override
    protected void setUserInputs() {

    }
}