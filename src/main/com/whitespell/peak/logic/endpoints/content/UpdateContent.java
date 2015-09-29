package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.GenericAPIActions;
import main.com.whitespell.peak.logic.RequestObject;
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

    private static final String GET_CONTENT_QUERY = "SELECT * FROM `content` WHERE `content_id` = ?";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(CONTENT_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(CONTENT_TITLE, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_DESCRIPTION, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PRICE, StaticRules.InputTypes.REG_DOUBLE_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();
        String temp = "", temp1 = "";
        Double temp2 = 0.00;
        final ArrayList<String> updateKeys = new ArrayList<>();

        if (j.get(CONTENT_TITLE) != null) {
            temp = j.get(CONTENT_TITLE).getAsString();
            updateKeys.add("content_title");
       }
        if (j.get(CONTENT_DESCRIPTION) != null) {
            temp1 = j.get(CONTENT_DESCRIPTION).getAsString();
            updateKeys.add("content_description");
        }
        if (j.get(CONTENT_PRICE) != null) {
            temp2 = j.get(CONTENT_PRICE).getAsDouble();
            updateKeys.add("content_price");
        }

        if(temp.equals("") && temp1.equals("") && temp2 == 0.00){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        final int user_id = j.get(CONTENT_USER_ID).getAsInt();
        final int content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));
        final String content_title = temp;
        final String content_description = temp1;
        final Double content_price = temp2;

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated() || user_id != a.getUserId()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Ensure user attempting to update content is the uploader
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_QUERY);

            executor.execute(ps -> {
                ps.setInt(1, content_id);

                ResultSet results = ps.executeQuery();

                if(results.next()){
                    if(results.getInt("user_id") != user_id){
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
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
            StatementExecutor executor = new StatementExecutor(UPDATE_CONTENT);
            final int finalContentId = content_id;
            final String finalContentTitle = content_title;
            final String finalContentDescription = content_description;
            final Double finalContentPrice = content_price;

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    ContentObject content = null;
                    int count = 1;

                    if (updateKeys.contains("content_title")) {
                        ps.setString(count, finalContentTitle);
                        count++;
                    }
                    if (updateKeys.contains("content_description")) {
                        ps.setString(count, finalContentDescription);
                        count++;
                    }
                    if (updateKeys.contains("content_price")) {
                        ps.setDouble(count, finalContentPrice);
                        count++;
                    }
                    ps.setInt(count, finalContentId);

                    final int update = ps.executeUpdate();

                    if (update > 0) {
                        //outputs only the updated user fields, others will be "" or -1

                        content = new ContentObject(user_id, finalContentId, 0, finalContentTitle,
                                "", finalContentDescription, "");
                        content.setContentPrice(finalContentPrice);

                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_EDITED);
                        return;
                    }

                    Gson g = new Gson();
                    String response = g.toJson(content);
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
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }//end update content
    }
}
