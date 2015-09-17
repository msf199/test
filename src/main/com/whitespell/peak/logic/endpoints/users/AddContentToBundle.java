package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         8/29/15
 *         main.com.whitespell.peak.logic.endpoints.users
 *         https://docs.google.com/document/d/1h9lzMmwLpuK3DEsu2j5Eth6m5_pDUxGgrq_Fh0xY2zU/edit
 */
public class AddContentToBundle extends EndpointHandler{

    /**
     * bundle_match links parents to children for bundles.
     */

    private static final String ADD_TO_BUNDLE_INSERT_QUERY = "INSERT INTO `bundle_match`(`parent_content_id`, `child_content_id`) VALUES (?,?)";
    private static final String CHECK_BUNDLE_OWNERSHIP_QUERY = "SELECT `content_type`, `user_id` FROM `content` WHERE `content_id` = ? LIMIT 1";
    private static final String UPDATE_TO_CHILD = "UPDATE `content` SET `is_child` = 1 WHERE `content_id` = ?";

    private static final String URL_CONTENT_ID_KEY = "contentId"; // content id is the parent

    private static final String PAYLOAD_CHILD_ID_KEY = "childId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_CHILD_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        /**
         * Ensure that the user is authenticated properly
         */

        JsonObject payload = context.getPayload().getAsJsonObject();

        final int PARENT_CONTENT_ID = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID_KEY));
        final int CHILD_CONTENT_ID = payload.get(PAYLOAD_CHILD_ID_KEY).getAsInt();

        if(PARENT_CONTENT_ID == CHILD_CONTENT_ID) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHORIZED, "child and parent can not have the same id");
            return;
        }

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }



        /** Check if content type is bundle, and if bundle is owned by the user **/

        try {
            StatementExecutor executor = new StatementExecutor(CHECK_BUNDLE_OWNERSHIP_QUERY);
            final int FINAL_PARENT_CONTENT_ID = PARENT_CONTENT_ID;
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {
                    ps.setInt(1, FINAL_PARENT_CONTENT_ID);
                    ResultSet s = ps.executeQuery();
                    if (s.next()) {
                        if(s.getInt("content_type") != StaticRules.BUNDLE_CONTENT_TYPE) {
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHORIZED, "Object you are accessing is not a bundle");
                            return;
                        }

                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Bundle the parent and the child
         */


        try {
            StatementExecutor executor = new StatementExecutor(ADD_TO_BUNDLE_INSERT_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, PARENT_CONTENT_ID);
                ps.setInt(2, CHILD_CONTENT_ID);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    Gson g = new Gson();
                    String json = g.toJson(new AddContentToBundleModel(true));
                    try {

                        /** if all is sucessful, update content id of content being added to child to true, which will put it only insides bundles */

                        try {
                            StatementExecutor executor_childupdate = new StatementExecutor(UPDATE_TO_CHILD);
                            executor_childupdate.execute(ps_childupdate -> {
                                ps_childupdate.setInt(1, CHILD_CONTENT_ID);

                                int rows_childupdate = ps_childupdate.executeUpdate();
                                if (rows_childupdate <= 0) {
                                    System.out.println("Failed to set childness");
                                }
                            });

                        } catch (SQLException e) {
                            Logging.log("High", e);
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                            return;
                        }


                        context.getResponse().getWriter().write(json);
                    } catch (IOException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }

                } else {
                    System.out.println("Failed to add content to bundle");
                    throw new SQLException("No content added to a bundle");
                }
            });
        } catch (SQLException e) {
            if(e.getClass().toString().contains("MySQLIntegrityConstraintViolationException")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHORIZED, "Child id is already a child of this parent");

                return;
            }
            Logging.log("High", e);
            return;
        }

    }

    public class AddContentToBundleModel {
        boolean addedToBundle;
        public AddContentToBundleModel(boolean addedToBundle)  {
            this.addedToBundle = addedToBundle;
        }
    }

}
