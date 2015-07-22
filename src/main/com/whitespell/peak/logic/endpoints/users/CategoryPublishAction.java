package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Pim de Witte(wwadewitte), Pim de Witte, Whitespell Inc., Whitespell LLC
 *         5/4/2015
 *         whitespell.model
 */
public class CategoryPublishAction extends EndpointHandler {

    private static final String ACTION_KEY = "action";
    private static final String CATEGORY_ID_KEY = "categoryId";
    private static final String USER_ID_KEY = "user_id";

    @Override
    protected void setUserInputs() {
        urlInput.put(USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(ACTION_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        payloadInput.put(CATEGORY_ID_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);

    }

    private static final String CHECK_PUBLISHING_CATEGORY_QUERY = "SELECT 1 FROM `category_publishing` WHERE `user_id` = ? AND `category_id` = ? LIMIT 1";

    private static final String INSERT_PUBLISH_CATEGORY_QUERY = "INSERT INTO `category_publishing`(`user_id`, `category_id`, `timestamp`) VALUES (?,?,?)";
    private static final String DELETE_PUBLISH_CATEGORY_QUERY = "DELETE FROM `category_publishing` WHERE `user_id` = ? AND `category_id` = ?";

    @Override
    public void safeCall(RequestObject context) throws IOException {

        JsonObject payload = context.getPayload().getAsJsonObject();

        String category_id_string = payload.get(CATEGORY_ID_KEY).getAsString();

        //local variables
        final int user_id = Integer.parseInt(context.getUrlVariables().get(USER_ID_KEY));
        final int category_id = Integer.parseInt(category_id_string);
        final String action = payload.get(ACTION_KEY).getAsString();
        final Timestamp now = new Timestamp(new Date().getTime());

        /**
         * Check that the action being performed is valid.
         */

        boolean validAction = action.equalsIgnoreCase("publish") || action.equalsIgnoreCase("unpublish");

        /**
         * If the action is invalid throw a null value error.
         */

        if (!validAction) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        /**
         * currently "admin" will updated category_publishing table
         */
        boolean isMe = a.getUserId() == user_id;

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Create the {@link CategoryPublishAction.ActionResponse}.
         */
        final ActionResponse response = new ActionResponse();

        /**
         * Check to see if the user is already publishing the publishing category id.
         */

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_PUBLISHING_CATEGORY_QUERY);
            executor.execute(ps -> {
                ps.setString(1, String.valueOf(user_id));
                ps.setString(2, String.valueOf(category_id));

                ResultSet results = ps.executeQuery();
                if (results.next()) {
                    response.setCurrentlyPublishing(true);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }

        switch (action) {

            case "publish":

                /**
                 * If already publishing, throw error.
                 */

                if (response.isCurrentlyPublishing()) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ALREADY_PUBLISHING_CATEGORY);
                    return;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(INSERT_PUBLISH_CATEGORY_QUERY);
                    executor.execute(ps -> {
                        ps.setString(1, String.valueOf(user_id));
                        ps.setString(2, String.valueOf(category_id));
                        ps.setString(3, now.toString());

                        ps.executeUpdate();

                        response.setSuccess(true);
                        response.setActionTaken("published");
                    });
                } catch (MySQLIntegrityConstraintViolationException e) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_SUCH_CATEGORY);
                } catch (SQLException e) {
                    if (e.getMessage().contains(""))
                        Logging.log("High", e);
                }
                break;

            case "unpublish":

                /**
                 * If not currently publishing, throw error.
                 */

                if (!response.isCurrentlyPublishing()) {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_PUBLISHING_CATEGORY);
                    return;
                }
                try {
                    StatementExecutor executor = new StatementExecutor(DELETE_PUBLISH_CATEGORY_QUERY);
                    executor.execute(ps -> {
                        ps.setString(1, String.valueOf(user_id));
                        ps.setString(2, String.valueOf(category_id));

                        ps.executeUpdate();

                        response.setSuccess(true);
                        response.setActionTaken("unpublish");
                    });
                } catch (SQLException e) {
                    Logging.log("High", e);
                }
                break;
        }


        /**
         * If the action taken was successfully performed then write the response.
         */
        if (response.isSuccess()) {
            context.getResponse().setStatus(HttpStatus.OK_200);
            PublishCategoryActionObject followObject = new PublishCategoryActionObject();
            followObject.setActionTaken(response.getActionTaken());
            Gson g = new Gson();
            String json = g.toJson(followObject);
            context.getResponse().getWriter().write(json);
        } else {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }


    private static class ActionResponse {

        private boolean success;
        private String actionTaken;
        private boolean currentlyPublishing;

        public ActionResponse() {
            this.success = false;
            this.currentlyPublishing = false;
            this.actionTaken = null;
        }

        public boolean isCurrentlyPublishing() {
            return currentlyPublishing;
        }

        public void setCurrentlyPublishing(boolean currentlyPublishing) {
            this.currentlyPublishing = currentlyPublishing;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getActionTaken() {
            return actionTaken;
        }

        public void setActionTaken(String actionTaken) {
            this.actionTaken = actionTaken;
        }

    }

    public class PublishCategoryActionObject {

        String action_taken;

        public String getActionTaken() {
            return this.action_taken;
        }

        public void setActionTaken(String action_taken) {
            this.action_taken = action_taken;
        }

    }


}
