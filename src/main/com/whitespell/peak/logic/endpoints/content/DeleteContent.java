package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         10/5/2015
 */
public class DeleteContent extends EndpointHandler {

    private static final String DELETE_CONTENT_QUERY = "DELETE FROM `content` WHERE `content_id` = ?";

    /**
     * Define user input variables
     */

    private static final String URL_CONTENT_ID = "contentId";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        int contentId = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        ContentHelper h = new ContentHelper();
        DeleteHelper d = new DeleteHelper();

        ContentObject toDelete;
        String contentUrl = null;
        String contentThumbnail = null;

        /**
         * Check content poster and ensure content poster matches authentication (only publisher can delete content)
         */
        try {
            toDelete = h.getContentById(contentId);

            if(toDelete == null){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
                return;
            }

            /**
             * If user is not the publisher of this content they are unauthorized
             */

            if(a.getUserId() == -1 && a.isMasterKey(a.getKey())){
                System.out.println("master is authorized");
            }else if(toDelete != null && toDelete.getPoster().getUserId() != a.getUserId()){
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHORIZED);
                return;
            }

            /**
             * Get the contentUrl and thumbnailUrl to delete from AWS or Cloudinary
             */
            contentUrl = toDelete.getContentUrl();
            contentThumbnail = toDelete.getThumbnailUrl();

        }
        catch(UnirestException e){
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.CONTENT_NOT_FOUND);
            return;
        }


        /**
         * Delete the video from AWS
         */
        if(contentUrl != null
                && contentUrl.contains(Config.AWS_API_VID_BUCKET)
                && contentUrl.contains(Config.AWS_API_HOSTNAME)){
            System.out.println("aws video delete");

            /**
             * Parse url to get video name (get everything after last "/")
             */
            String filename = contentUrl.substring(contentUrl.lastIndexOf("/") + 1);

            /**
             * Attempt the delete from AWS
             */
            d.deleteAWSContent(Config.AWS_API_VID_BUCKET, filename);

        }

        /**
         * Delete the picture from AWS
         */
        if(contentThumbnail != null
                && contentThumbnail.contains(Config.AWS_API_IMG_BUCKET)
                && contentThumbnail.contains(Config.AWS_API_HOSTNAME)) {
            System.out.println("aws thumbnail delete");

            /**
             * Parse url to get image name (get everything after last "/")
             */
            String filename = contentThumbnail.substring(contentThumbnail.lastIndexOf("/") + 1);

            /**
             * Attempt the delete from AWS
             */
            d.deleteAWSContent(Config.AWS_API_IMG_BUCKET, filename);
        }

        /**
         * Delete the picture from cloudinary
         */
        if(contentThumbnail != null
                && contentThumbnail.contains(Config.CL_API_FOLDER)
                && contentThumbnail.contains(Config.CL_API_HOSTNAME)){
            System.out.println("cloud thumbnail delete");

            /**
             * Parse url to get "public_id" for cloudinary (get everything after last "/", exclude file type)
             */
            String filename = contentThumbnail.substring(contentThumbnail.lastIndexOf("/") + 1);
            String split[] = filename.split("\\.");
            String publicId = split[0];

            d.deleteCloudinaryImage(publicId);
        }

        /**
         * Delete the content from the DB after attempting to delete from hosting services
         */
        try {
            StatementExecutor executor = new StatementExecutor(DELETE_CONTENT_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, contentId);

                ps.executeUpdate();
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        context.getResponse().setStatus(HttpStatus.OK_200);
        DeleteContentResponse object = new DeleteContentResponse();
        object.setContentDeleted(true);
        Gson g = new Gson();
        String json = g.toJson(object);
        try {
            context.getResponse().getWriter().write(json);
        }catch(Exception e) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class DeleteContentResponse {

        private boolean success;

        public boolean contentDeleted() {
            return this.success;
        }

        public void setContentDeleted(boolean success) {
            this.success = success;
        }
    }


}

