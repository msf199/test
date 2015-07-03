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
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

    /**
     * @author Cory McAn(cmcan), Whitespell LLC
     *         6/30/15
     *         whitespell.model
     */
    public class UpdateProfile extends EndpointHandler {
    private static final String URL_USER_ID = "user_id";

    private static final String PAYLOAD_USERNAME_KEY = "username";
    private static final String PAYLOAD_DISPLAYNAME_KEY = "displayname";
    private static final String PAYLOAD_THUMBNAIL_KEY = "thumbnail";
    private static final String PAYLOAD_COVER_PHOTO_KEY = "cover_photo";
    private static final String PAYLOAD_SLOGAN_KEY = "slogan";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_DISPLAYNAME_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_THUMBNAIL_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_COVER_PHOTO_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(PAYLOAD_SLOGAN_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    private static final String CHECK_USERNAME_TAKEN_QUERY = "SELECT `user_id`, `username` FROM `user` WHERE `username` = ? AND `user_id` != ? LIMIT 1";

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();
        String temp = "", temp1 = "", temp2 = "", temp3 = "", temp4 = "";
        final ArrayList<String> updateKeys = new ArrayList<>();
        final ArrayList<String> updateValues = new ArrayList<>();

        if (j.get(PAYLOAD_USERNAME_KEY) != null) {
            temp = j.get(PAYLOAD_USERNAME_KEY).getAsString();
            updateKeys.add(PAYLOAD_USERNAME_KEY);
            updateValues.add(temp);
        }
        if (j.get(PAYLOAD_DISPLAYNAME_KEY) != null) {
            temp1 = j.get(PAYLOAD_DISPLAYNAME_KEY).getAsString();
            updateKeys.add(PAYLOAD_DISPLAYNAME_KEY);
            updateValues.add(temp1);
        }
        if (j.get(PAYLOAD_THUMBNAIL_KEY) != null) {
            temp2 = j.get(PAYLOAD_THUMBNAIL_KEY).getAsString();
            updateKeys.add(PAYLOAD_THUMBNAIL_KEY);
            updateValues.add(temp2);
        }
        if (j.get(PAYLOAD_COVER_PHOTO_KEY) != null) {
            temp3 = j.get(PAYLOAD_COVER_PHOTO_KEY).getAsString();
            updateKeys.add(PAYLOAD_COVER_PHOTO_KEY);
            updateValues.add(temp3);
        }
        if (j.get(PAYLOAD_SLOGAN_KEY) != null) {
            temp4 = j.get(PAYLOAD_SLOGAN_KEY).getAsString();
            updateKeys.add(PAYLOAD_SLOGAN_KEY);
            updateValues.add(temp4);
        }

        final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
        final String username = temp;
        final String displayname = temp1;
        final String thumbnail = temp2;
        final String cover_photo = temp3;
        final String slogan = temp4;

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * 401 Unauthorized: Check if username exists
         */
        if (updateKeys.contains(PAYLOAD_USERNAME_KEY)) {
            try {
                StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_TAKEN_QUERY);
                final int finalUser_id = user_id;
                final String finalUsername = username;
                final boolean[] returnCall = {false};
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {
                        ps.setString(1, finalUsername);
                        ps.setInt(2, finalUser_id);
                        ResultSet s = ps.executeQuery();
                        if (s.next()) {
                            if (s.getString(PAYLOAD_USERNAME_KEY).equalsIgnoreCase(finalUsername)) {
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERNAME_TAKEN);
                            }
                            returnCall[0] = true;
                            return;
                        }
                    }
                });
                /**
                 * This check is put in place if the request should be closed and no more logic should be executed after the query result is received. Because it is a nested function, we need to check with a finalized array
                 * whether we should return, and do so if necessary.
                 */
                if (returnCall[0]) {
                    return;
                }
            } catch (SQLException e) {
                Logging.log("High", e);
                return;
            }
        }


        /**
         * Construct the SET string based on the fields the user wants to update.
         */
        StringBuilder setString = new StringBuilder();
        int count = 1;
        int size = updateKeys.size();
        for (String s : updateKeys) {
            if (count == 1) {
                setString.append("UPDATE `user` SET ");
            }
            if (count == size) {
                setString.append("`" + s + "` = ? ");
            } else {
                setString.append("`" + s + "` = ?, ");
            }
            count++;
        }
        setString.append("WHERE `user_id` = ?");
        final String UPDATE_USER = setString.toString();
        System.out.println(UPDATE_USER);

        //try to update user
        try {
            StatementExecutor executor = new StatementExecutor(UPDATE_USER);
            final int finalUser_id = user_id;
            final String finalUsername = username;
            final String finalDisplayname = displayname;
            final String finalThumbnail = thumbnail;
            final String finalCoverPhoto = cover_photo;
            final String finalSlogan = slogan;

            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    UserObject user = null;
                    int count = 1;

                    if (updateValues.contains(finalUsername)) {
                        ps.setString(count, finalUsername);
                        count++;
                    }
                    if (updateValues.contains(finalDisplayname)) {
                        ps.setString(count, finalDisplayname);
                        count++;
                    }
                    if (updateValues.contains(finalThumbnail)) {
                        ps.setString(count, finalThumbnail);
                        count++;
                    }
                    if (updateValues.contains(finalCoverPhoto)) {
                        ps.setString(count, finalCoverPhoto);
                        count++;
                    }
                    if (updateValues.contains(finalSlogan)) {
                        ps.setString(count, finalSlogan);
                        count++;
                    }
                    ps.setInt(count, finalUser_id);

                    final int update = ps.executeUpdate();

                    if (update > 0) {
                        //outputs only the updated user fields, others will be ""
                        user = new UserObject(finalUser_id, finalUsername, finalDisplayname, finalThumbnail,
                                finalCoverPhoto, finalSlogan);
                    } else {
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USER_NOT_EDITED);
                        return;
                    }

                    Gson g = new Gson();
                    String response = g.toJson(user);
                    context.getResponse().setStatus(200);
                    try {
                        context.getResponse().getWriter().write(response);
                    } catch (Exception e) {
                        Logging.log("High", e);
                        return;
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            return;
        }//end update profile
    }
}

