package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         10/6/2015
 */
public class DeleteUser extends EndpointHandler {

    private static final String DELETE_USER_QUERY = "DELETE FROM `user` WHERE `user_id` = ?";
    private static final String DELETE_NOTIFICATION_QUERY = "DELETE FROM `notification` where `notification_action` = ?";

    /**
     * Define user input variables
     */

    private static final String URL_USER_ID = "userId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_USER_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        int userId = Integer.parseInt(context.getUrlVariables().get(URL_USER_ID));

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = userId == a.getUserId();

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        UserHelper h = new UserHelper();
        DeleteHelper d = new DeleteHelper();

        UserObject toDelete;
        String userThumbnail = null;
        String coverPhotoUrl = null;

        /**
         * Get userObject and images associated with it
         */
            toDelete = h.getUserById(userId, true, true, true , true);

            if(toDelete == null){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                return;
            }

            /**
             * Get the thumbnailUrl and coverPhotoUrl to delete from AWS or Cloudinary
             */
            userThumbnail = toDelete.getThumbnail();
            coverPhotoUrl = toDelete.getCoverPhoto();





        /**
         * Delete the userThumbnail from AWS (if exists)
         */
        if(userThumbnail != null
                && userThumbnail.contains(Config.AWS_API_IMG_BUCKET)
                && userThumbnail.contains(Config.AWS_API_HOSTNAME)){
            System.out.println("aws thumbnail delete");

            /**
             * Parse url to get thumbnail name (get everything after last "/")
             */
            String filename = userThumbnail.substring(userThumbnail.lastIndexOf("/") + 1);

            /**
             * Attempt the delete from AWS
             */
            d.deleteAWSContent(Config.AWS_API_IMG_BUCKET, filename);

        }

        /**
         * Delete the picture from AWS
         */
        if(coverPhotoUrl != null
                && coverPhotoUrl.contains(Config.AWS_API_IMG_BUCKET)
                && coverPhotoUrl.contains(Config.AWS_API_HOSTNAME)) {
            System.out.println("aws coverPhoto delete");

            /**
             * Parse url to get image name (get everything after last "/")
             */
            String filename = coverPhotoUrl.substring(coverPhotoUrl.lastIndexOf("/") + 1);

            /**
             * Attempt the delete from AWS
             */
            d.deleteAWSContent(Config.AWS_API_IMG_BUCKET, filename);
        }

        /**
         * Delete the userThumbnail from cloudinary
         */
        if(userThumbnail != null
                && userThumbnail.contains(Config.CL_API_FOLDER)
                && userThumbnail.contains(Config.CL_API_HOSTNAME)){
            System.out.println("cloud thumbnail delete");

            /**
             * Parse url to get "public_id" for cloudinary (get everything after last "/", exclude file type)
             */
            String filename = userThumbnail.substring(userThumbnail.lastIndexOf("/") + 1);
            String split[] = filename.split("\\.");
            String publicId = split[0];

            d.deleteCloudinaryImage(publicId);
        }

        /**
         * Delete the coverPhoto from cloudinary
         */
        if(coverPhotoUrl != null
                && coverPhotoUrl.contains(Config.CL_API_FOLDER)
                && coverPhotoUrl.contains(Config.CL_API_HOSTNAME)){
            System.out.println("cloud coverPhoto delete");

            /**
             * Parse url to get "public_id" for cloudinary (get everything after last "/", exclude file type)
             */
            String filename = coverPhotoUrl.substring(coverPhotoUrl.lastIndexOf("/") + 1);
            String split[] = filename.split("\\.");
            String publicId = split[0];

            d.deleteCloudinaryImage(publicId);
        }

        /**
         * Delete the user from the DB after attempting to delete from hosting services
         */
        try {
            StatementExecutor executor = new StatementExecutor(DELETE_USER_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, userId);

                ps.executeUpdate();
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Delete notifications related to this user
         */
        try {
            StatementExecutor executor = new StatementExecutor(DELETE_NOTIFICATION_QUERY);
            executor.execute(ps -> {
                ps.setString(1, "open-user:" + userId);

                int rows = ps.executeUpdate();
                if(rows > 0){
                    System.out.println("success deleting notifications for this user");
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        context.getResponse().setStatus(HttpStatus.OK_200);
        DeleteUserResponse object = new DeleteUserResponse();
        object.setUserDeleted(true);
        Gson g = new Gson();
        String json = g.toJson(object);
        try {
            context.getResponse().getWriter().write(json);
        }catch(Exception e) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class DeleteUserResponse {

        private boolean success;

        public boolean userDeleted() {
            return this.success;
        }

        public void setUserDeleted(boolean success) {
            this.success = success;
        }
    }


}
