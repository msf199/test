package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         9/3/2015
 */
public class ReportUser extends EndpointHandler {

    private static final String SUBMITTER_USER_ID = "userId";
    private static final String REPORTED_USER_ID = "reportedUserId";
    private static final String REPORT_TYPE_ID = "typeId";
    private static final String REPORT_MESSAGE = "message";

    private static final String INSERT_REPORT = "INSERT INTO `reporting`(`submitter_user_id`,`reported_user_id`,`reporting_type_id`,`message`) VALUES(?,?,?,?)";

    @Override
    protected void setUserInputs() {
        urlInput.put(SUBMITTER_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(REPORTED_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(REPORT_TYPE_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(REPORT_MESSAGE, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject json = context.getPayload().getAsJsonObject();

        int userId = Integer.parseInt(context.getUrlVariables().get(SUBMITTER_USER_ID));
        int reportedUserId = json.get(REPORTED_USER_ID).getAsInt();
        int typeId = json.get(REPORT_TYPE_ID).getAsInt();
        String message = json.get(REPORT_MESSAGE).getAsString();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (a.getUserId() == userId);

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Update DB with report from user about another user
         */
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_REPORT);

            executor.execute(ps -> {
                ps.setInt(1, userId);
                ps.setInt(2, reportedUserId);
                ps.setInt(3, typeId);
                ps.setString(4, message);

                int rows = ps.executeUpdate();

                /**
                 * reporting details updated in table
                 */
                if (rows <= 0) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COULD_NOT_REPORT_USER);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        reportUserSuccessObject f = new reportUserSuccessObject();
        f.setSuccess(true);

        Gson g = new Gson();
        String response = g.toJson(f);
        context.getResponse().setStatus(200);
        try {
            context.getResponse().getWriter().write(response);
        } catch (Exception e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public static class reportUserSuccessObject {

        boolean success;

        reportUserSuccessObject(){
            this.success = false;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

    }
        
}
