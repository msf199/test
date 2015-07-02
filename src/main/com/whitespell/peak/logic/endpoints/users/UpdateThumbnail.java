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
 * Created by Cory on 7/2/2015.
 */
public class UpdateThumbnail extends EndpointHandler{
	private static final String EDIT_THUMBNAIL = "UPDATE `user` SET `thumbnail` = ? WHERE `user_id` = ?";
	private static final String GET_USER = "SELECT `user_id`, `username`, `displayname`, `email`, `thumbnail`, `cover_photo`, `slogan` FROM `user` WHERE `user_id` = ?";

	private static final String URL_USER_ID = "user_id";

	private static final String PAYLOAD_THUMBNAIL_KEY = "thumbnail";

	private static final String USERNAME_KEY = "username";
	private static final String DISPLAYNAME_KEY = "displayname";
	private static final String EMAIL_KEY = "email";
	private static final String COVER_PHOTO_KEY = "cover_photo";
	private static final String SLOGAN_KEY = "slogan";


	@Override
	protected void setUserInputs() {
		urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
		payloadInput.put(PAYLOAD_THUMBNAIL_KEY, StaticRules.InputTypes.REG_STRING_REQUIRED);
	}

	@Override
	public void safeCall(final RequestObject context) throws IOException {

		final int user_id = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));
		final String thumbnail = context.getPayload().getAsJsonObject().get(PAYLOAD_THUMBNAIL_KEY).getAsString();

		/**
		 * Ensure that the user is authenticated properly
		 */

		final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

		if (!a.isAuthenticated()) {
			context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
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

						final String username = results.getString(USERNAME_KEY);
						final String displayname = results.getString(DISPLAYNAME_KEY);
						final String email = results.getString(EMAIL_KEY);
						final String old_thumbnail = results.getString(PAYLOAD_THUMBNAIL_KEY);
						final String cover_photo = results.getString(COVER_PHOTO_KEY);
						final String slogan = results.getString(SLOGAN_KEY);

					/**
					 * Check if Thumbnail changed, if yes, Update the Thumbnail
					 */
					//if(!old_thumbnail.equals(thumbnail)) {
						try {
							StatementExecutor executor = new StatementExecutor(EDIT_THUMBNAIL);
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
