package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
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

    /**
     * @author Cory McAn(cmcan), Whitespell LLC
     *         6/30/15
     *         whitespell.model
     */
    public class UpdateProfile extends EndpointHandler {


        private static final String UPDATE_USER = "UPDATE `user` SET `username` = ?, `displayname` = ? `slogan` = ? WHERE `user_id` = ?";
        private static final String GET_USER = "SELECT `user_id`, `username`, `displayname`, `email`, `thumbnail`, `cover_photo`, `slogan` FROM `user` WHERE `user_id` = ?";

        private static final String URL_USER_ID = "user_id";

		private static final String PAYLOAD_USERNAME_KEY = "username";
        private static final String PAYLOAD_DISPLAYNAME_KEY = "displayname";
        private static final String PAYLOAD_SLOGAN_KEY = "slogan";

		private static final String EMAIL_KEY = "email";
		private static final String THUMBNAIL_KEY = "thumbnail";
		private static final String COVER_PHOTO_KEY = "cover_photo";


        @Override
        protected void setUserInputs() {
            urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
			payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
            payloadInput.put(PAYLOAD_DISPLAYNAME_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
			payloadInput.put(PAYLOAD_SLOGAN_KEY, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        }

        private static final String CHECK_USERNAME_TAKEN_QUERY = "SELECT `user_id`, `username` FROM `user` WHERE `username` = ? AND `user_id` != ? LIMIT 1";

        @Override
        public void safeCall(final RequestObject context) throws IOException {

            final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
            String username = context.getPayload().getAsJsonObject().get(PAYLOAD_USERNAME_KEY).getAsString();
            String displayname = context.getPayload().getAsJsonObject().get(PAYLOAD_DISPLAYNAME_KEY).getAsString();
			String slogan = context.getPayload().getAsJsonObject().get(PAYLOAD_SLOGAN_KEY).getAsString();

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
            try {
                StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_TAKEN_QUERY);
                final int finalUser_id = user_id;
                final String finalUsername = username;
                final boolean[] returnCall = {false};
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {
                        ps.setString(1, finalUsername);
                        ps.setInt(3, finalUser_id);
                        ResultSet s = ps.executeQuery();
                        if (s.next()) {
                            if (s.getString("username").equalsIgnoreCase(finalUsername)) {
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


            /**
             * Get the user values
             */
            try {
                StatementExecutor executor = new StatementExecutor(GET_USER);
                final int finalUser_id = user_id;
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {

                        UserObject user = null;

                        ps.setInt(1, finalUser_id);

                        final ResultSet results = ps.executeQuery();

                        if (results.next()) {

                            final String username = results.getString(PAYLOAD_USERNAME_KEY);
                            final String displayname = results.getString(PAYLOAD_DISPLAYNAME_KEY);
                            final String email = results.getString(EMAIL_KEY);
                            final String thumbnail = results.getString(THUMBNAIL_KEY);
                            final String cover_photo = results.getString(COVER_PHOTO_KEY);
                            final String slogan = results.getString(PAYLOAD_SLOGAN_KEY);

                            /**
                             * Check if Thumbnail changed, if yes, Update the Thumbnail
                             */
                            //if(!old_thumbnail.equals(thumbnail)) {
                            try {
                                StatementExecutor executor = new StatementExecutor(UPDATE_USER);
                                final int finalUser_id = user_id;
                                final String finalUsername = username;
                                final String finalDisplayname = displayname;
                                final String finalEmail = email;
                                final String finalThumbnail = thumbnail;
                                final String finalCoverPhoto = cover_photo;
                                final String finalSlogan = slogan;


                                executor.execute(new ExecutionBlock() {
                                    @Override
                                    public void process(PreparedStatement ps) throws SQLException {

                                        UserObject user = null;

                                        ps.setString(1, finalThumbnail);
                                        ps.setInt(2, finalUser_id);

                                        final int update = ps.executeUpdate();

                                        if (update > 0) {
                                            user = new UserObject(finalUser_id, finalUsername, finalDisplayname, finalEmail, finalThumbnail,
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
                            }//end update Thumbnail
                            //}//end if old_thumbnail.equals(newThumb)

                        } else {
                            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USER_NOT_FOUND);
                            return;
                        }
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
                return;
            }
        }
    }

