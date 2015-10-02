package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         8/03/15
 */
public class GetBundle extends EndpointHandler {

    private static final String GET_BUNDLE_CONTENT_ID_QUERY = "SELECT `content_id` FROM `bundles` WHERE `user_id` = ? AND `bundle_name` = ?";
    private static final String GET_CONTENT_OBJECT_QUERY = "SELECT * FROM `content` WHERE `content_id` = ?";

    private static final String QS_BUNDLE_ID = "bundleId";

    private static final String URL_USER_ID = "userId";

    private static final String CONTENT_CATEGORY_ID = "category_id";
    private static final String CONTENT_ID_KEY = "content_id";
    private static final String CONTENT_TYPE_ID = "content_type";
    private static final String CONTENT_TITLE = "content_title";
    private static final String CONTENT_URL = "content_url";
    private static final String CONTENT_DESCRIPTION = "content_description";
    private static final String CONTENT_THUMBNAIL = "thumbnail_url";

    @Override
    protected void setUserInputs() {
        queryStringInput.put(QS_BUNDLE_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        int bundle_id = Integer.parseInt(context.getQueryString().get(QS_BUNDLE_ID)[0]);

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (user_id == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Get the user's saved list with the given bundleId
         */
        final GetBundleResponse getBundleResponse = new GetBundleResponse();
        try {
            StatementExecutor executor = new StatementExecutor(GET_BUNDLE_CONTENT_ID_QUERY);
            final int finalUser_id = user_id;
            final int finalBundle_id = bundle_id;

            executor.execute(ps -> {
                ps.setInt(1, finalUser_id);
                ps.setInt(2, finalBundle_id);

                ResultSet results = ps.executeQuery();

                while (results.next()) {
                    try {
                        StatementExecutor executor2 = new StatementExecutor(GET_CONTENT_OBJECT_QUERY);
                        final int finalContent_id = results.getInt(CONTENT_ID_KEY);
                        executor2.execute(ps2 -> {
                            ps2.setInt(1, finalContent_id);

                            ResultSet results2 = ps2.executeQuery();

                            if (results2.next()) {
                                ContentObject c = new ContentObject(results2.getInt(CONTENT_CATEGORY_ID), results2.getInt("user_id"), results2.getInt(CONTENT_ID_KEY),
                                        results2.getInt(CONTENT_TYPE_ID), results2.getString(CONTENT_TITLE), results2.getString(CONTENT_URL), results2.getString(CONTENT_DESCRIPTION),
                                        results2.getString(CONTENT_THUMBNAIL));
                                getBundleResponse.addToUserList(c);
                                getBundleResponse.setBundleId(finalBundle_id);
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        if (e.getMessage().contains("fk_lists_content_saved_id")) {
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                        }else if(e.getMessage().contains("fk_lists_saved_user_id")){
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                        }
                        return;
                    }
                }
                Gson g = new Gson();
                String response = g.toJson(getBundleResponse);
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

    public class GetBundleResponse {

        public GetBundleResponse() {
            this.bundle = new ArrayList<>();
            this.bundleId = 0;
        }

        public ArrayList<ContentObject> getBundle() {
            return bundle;
        }

        public void addToUserList(ContentObject content) {
            bundle.add(content);
        }

        public int getBundleId() {
            return bundleId;
        }

        public void setBundleId(int bundleId) {
            this.bundleId = bundleId;
        }

        public ArrayList<ContentObject> bundle;
        public int bundleId;
    }
}



