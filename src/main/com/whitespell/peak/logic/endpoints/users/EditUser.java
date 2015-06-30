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
    public class EditUser extends EndpointHandler {


        private static final String EDIT_USER = "UPDATE `user` SET `username` = ?, `email` = ?, `thumbnail` = ?, `cover_photo` = ?, `slogan` = ? WHERE `user_id` = ?";

        private static final String URL_USER_ID = "user_id";

		private static final String PAYLOAD_USERNAME_KEY = "username";
		private static final String PAYLOAD_EMAIL_KEY = "email";
		private static final String PAYLOAD_THUMBNAIL_KEY = "thumbnail";
		private static final String PAYLOAD_COVER_PHOTO_KEY = "cover_photo";
		private static final String PAYLOAD_SLOGAN_KEY = "slogan";

        @Override
        protected void setUserInputs() {
            urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
			payloadInput.put(PAYLOAD_USERNAME_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
			payloadInput.put(PAYLOAD_EMAIL_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
			payloadInput.put(PAYLOAD_THUMBNAIL_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
			payloadInput.put(PAYLOAD_COVER_PHOTO_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
			payloadInput.put(PAYLOAD_SLOGAN_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
        }

        private static final String CHECK_USERNAME_OR_EMAIL_QUERY = "SELECT `username`, `email` FROM `user` WHERE `username` = ? OR `email` = ? LIMIT 1";

        @Override
        public void safeCall(final RequestObject context) throws IOException {

            final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
            String username = context.getPayload().getAsJsonObject().get(PAYLOAD_USERNAME_KEY).getAsString();
            String email = context.getPayload().getAsJsonObject().get(PAYLOAD_EMAIL_KEY).getAsString();
			String thumbnail = context.getPayload().getAsJsonObject().get(PAYLOAD_THUMBNAIL_KEY).getAsString();
			String cover_photo = context.getPayload().getAsJsonObject().get(PAYLOAD_COVER_PHOTO_KEY).getAsString();
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
                StatementExecutor executor = new StatementExecutor(CHECK_USERNAME_OR_EMAIL_QUERY);
                final String finalUsername = username;
                final String finalEmail = email;
                final boolean[] returnCall = {false};
                executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {
                        ps.setString(1, finalUsername);
                        ps.setString(2, finalEmail);
                        ResultSet s = ps.executeQuery();
                        if (s.next()) {
                            if (s.getString("username").equalsIgnoreCase(finalUsername)) {
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.USERNAME_TAKEN);
                            } else if (s.getString("email").equalsIgnoreCase(finalEmail)) {
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.EMAIL_TAKEN);
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
			 * (todo cmcan)Handle password change
			 */


            try {
                StatementExecutor executor = new StatementExecutor(EDIT_USER);
                final int finalUser_id = user_id;
				final String finalUsername = username;
				final String finalEmail = email;
				final String finalThumbnail = thumbnail;
				final String finalCoverPhoto = cover_photo;
				final String finalSlogan = slogan;

				executor.execute(new ExecutionBlock() {
                    @Override
                    public void process(PreparedStatement ps) throws SQLException {

                        UserObject user = null;

                        ps.setString(1, finalUsername);
						ps.setString(2, finalEmail);
						ps.setString(3, finalThumbnail);
						ps.setString(4, finalCoverPhoto);
						ps.setString(5, finalSlogan);
						ps.setInt(6, finalUser_id);

						final int update = ps.executeUpdate();

						if(update > 0){
							user = new UserObject(finalUser_id, finalUsername, finalEmail, finalThumbnail,
									finalCoverPhoto, finalSlogan);
						}
						else {
								context.throwHttpError("EditUser", StaticRules.ErrorCodes.USER_NOT_EDITED);
								return;
						}

                        Gson g = new Gson();
                        String response = g.toJson(user);
                        context.getResponse().setStatus(200);
                        try {
                            context.getResponse().getWriter().write(response);
                        } catch (Exception e) {
                            Logging.log("High", e);
                        }
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
				return;
            }

        }
    }

