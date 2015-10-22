package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.endpoints.UpdateStatus;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         9/28/2015
 */
public class UpdateContent extends EndpointHandler {

    private static final String URL_CONTENT_ID = "contentId";

    private static final String CONTENT_USER_ID = "userId";
    private static final String CONTENT_TITLE = "contentTitle";
    private static final String CONTENT_DESCRIPTION = "contentDescription";
    private static final String CONTENT_PRICE = "contentPrice";
    private static final String CATEGORY_ID = "categoryId";

    /** Video URLs **/
    private static final String CONTENT_URL = "contentUrl";
    private static final String CONTENT_URL_1080P = "contentUrl1080p";
    private static final String CONTENT_URL_720P = "contentUrl720p";
    private static final String CONTENT_URL_480P = "contentUrl480p";
    private static final String CONTENT_PREVIEW_720P = "contentPreview720p";

    private static final String CONTENT_TITLE_DB = "content_title";
    private static final String CONTENT_DESCRIPTION_DB = "content_description";
    private static final String CONTENT_PRICE_DB = "content_price";
    private static final String CATEGORY_ID_DB = "category_id";

    private static final String CONTENT_URL_DB = "content_url";
    private static final String CONTENT_URL_1080P_DB = "content_url_1080p";
    private static final String CONTENT_URL_720P_DB = "content_url_720p";
    private static final String CONTENT_URL_480P_DB = "content_url_480p";
    private static final String CONTENT_PREVIEW_720P_DB = "content_preview_720p";

    private static final String PROCESSED = "processed";


    private static final String GET_CONTENT_QUERY = "SELECT * FROM `content` WHERE `content_id` = ?";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(CONTENT_TITLE, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_DESCRIPTION, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PRICE, StaticRules.InputTypes.REG_DOUBLE_OPTIONAL);
        payloadInput.put(CONTENT_PRICE, StaticRules.InputTypes.REG_DOUBLE_OPTIONAL);
        payloadInput.put(CONTENT_URL, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_URL_1080P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_URL_720P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_URL_480P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PREVIEW_720P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PROCESSED, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();
        String temp_title="", temp_description="", temp_url="", temp_url_1080p="", temp_url_720p="", temp_url_480p="", temp_preview_720p="";
        Double temp_price = 0.00;
        int temp_category_id=-1, temp_processed=0;
        final ArrayList<String> updateKeys = new ArrayList<>();

        if (j.get(CONTENT_TITLE) != null) {
            temp_title = j.get(CONTENT_TITLE).getAsString();
            updateKeys.add(CONTENT_TITLE_DB);
       }
        if (j.get(CONTENT_DESCRIPTION) != null) {
            temp_description = j.get(CONTENT_DESCRIPTION).getAsString();
            updateKeys.add(CONTENT_DESCRIPTION_DB);
        }
        if (j.get(CONTENT_PRICE) != null) {
            temp_price = j.get(CONTENT_PRICE).getAsDouble();
            updateKeys.add(CONTENT_PRICE_DB);
        }
        if (j.get(CATEGORY_ID) != null) {
            temp_category_id = j.get(CATEGORY_ID).getAsInt();
            updateKeys.add(CATEGORY_ID_DB);
        }
        if (j.get(CONTENT_URL) != null) {
            temp_url = j.get(CONTENT_URL).getAsString();
            updateKeys.add(CONTENT_URL_DB);
        }
        if (j.get(CONTENT_URL_1080P) != null) {
            temp_url_1080p = j.get(CONTENT_URL_1080P).getAsString();
            updateKeys.add(CONTENT_URL_1080P_DB);
        }
        if (j.get(CONTENT_URL_720P) != null) {
            temp_url_720p = j.get(CONTENT_URL_720P).getAsString();
            updateKeys.add(CONTENT_URL_720P_DB);
        }
        if (j.get(CONTENT_URL_480P) != null) {
            temp_url_480p = j.get(CONTENT_URL_480P).getAsString();
            updateKeys.add(CONTENT_URL_480P_DB);
        }
        if (j.get(CONTENT_PREVIEW_720P) != null) {
            temp_preview_720p = j.get(CONTENT_PREVIEW_720P).getAsString();
            updateKeys.add(CONTENT_PREVIEW_720P_DB);
        }
        if (j.get(PROCESSED) != null) {
            temp_processed = j.get(PROCESSED).getAsInt();
            updateKeys.add(PROCESSED);
        }

        if(updateKeys.isEmpty()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final int final_content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));
        final String final_title = temp_title;
        final String final_description = temp_description;
        final Double final_price = temp_price;
        final int final_category_id = temp_category_id;
        final String final_url = temp_url;
        final String final_url_1080p = temp_url_1080p;
        final String final_url_720p = temp_url_720p;
        final String final_url_480p = temp_url_480p;
        final String final_preview_720p = temp_preview_720p;
        final int final_processed = temp_processed;

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Ensure user attempting to update content is the uploader
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_QUERY);

            executor.execute(ps -> {
                ps.setInt(1, final_content_id);

                ResultSet results = ps.executeQuery();

                if(results.next()){
                    if(results.getInt("user_id") != a.getUserId()){
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHORIZED);
                        return;
                    }
                }
                });
        }catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return;
        }

        /**
         * Construct the SET string based on the fields the user wants to update.
         */

        StringBuilder setString = new StringBuilder();
        int count = 1;
        int size = updateKeys.size();
        for (String s : updateKeys) {
            if (count == 1) {
                setString.append("UPDATE `content` SET ");
            }
            if (count == size) {
                setString.append("`" + s + "` = ? ");
            } else {
                setString.append("`" + s + "` = ?, ");
            }
            count++;
        }
        setString.append("WHERE `content_id` = ?");
        final String UPDATE_CONTENT = setString.toString();

        //try to update user
        try {
            System.out.println(UPDATE_CONTENT);
            StatementExecutor executor = new StatementExecutor(UPDATE_CONTENT);

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    ContentObject content = null;
                    int count = 1;

                    if (updateKeys.contains(CONTENT_TITLE_DB)) {
                        ps.setString(count, final_title);
                        System.out.println("Set string " + count + " to " + final_title);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_DESCRIPTION_DB)) {
                        ps.setString(count, final_description);
                        System.out.println("Set string " + count + " to " + final_description);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_PRICE_DB)) {
                        ps.setDouble(count, final_price);
                        System.out.println("Set double " + count + " to " + final_price);
                        count++;
                    }
                    if (updateKeys.contains(CATEGORY_ID_DB)) {
                        ps.setInt(count, final_category_id);
                        System.out.println("Set int " + count + " to " + final_category_id);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_URL_DB)) {
                        ps.setString(count, final_url);
                        System.out.println("Set string " + count + " to " + final_url);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_URL_1080P_DB)) {
                        ps.setString(count, final_url_1080p);
                        System.out.println("Set string " + count + " to " + final_url_1080p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_URL_720P_DB)) {
                        ps.setString(count, final_url_720p);
                        System.out.println("Set string " + count + " to " + final_url_720p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_URL_480P_DB)) {
                        ps.setString(count, final_url_480p);
                        System.out.println("Set string " + count + " to " + final_url_480p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_PREVIEW_720P_DB)) {
                        ps.setString(count, final_preview_720p);
                        System.out.println("Set string " + count + " to " + final_preview_720p);
                        count++;
                    }

                    if (updateKeys.contains(PROCESSED)) {
                        ps.setInt(count, final_processed);
                        System.out.println("Set int " + count + " to " + final_processed);
                        count++;
                    }

                    ps.setInt(count, final_content_id);
                    System.out.println("Set int " + count + " to " + final_content_id);

                    final int update = ps.executeUpdate();

                    UpdateStatus status = null;
                    if (update > 0) {
                        //outputs only the updated user fields, others will be "" or -1

                        status = new UpdateStatus("success");

                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_EDITED);
                        return;
                    }

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
            });
        } catch (SQLException e) {
            if(e.getMessage().contains("CONSTRAINT `FK_content_category_id`")) {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_SUCH_CATEGORY);
                return;
            }
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }//end update content
    }
}
