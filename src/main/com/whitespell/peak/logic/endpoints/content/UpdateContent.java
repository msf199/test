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

    // content urls
    private static final String CONTENT_URL_1080P = "contentUrl1080p";
    private static final String CONTENT_URL_720P = "contentUrl720p";
    private static final String CONTENT_URL_480P = "contentUrl480p";
    private static final String CONTENT_URL_360P = "contentUrl360p";
    private static final String CONTENT_URL_240P = "contentUrl240p";
    private static final String CONTENT_URL_144P = "contentUrl144p";

    // content previews
    private static final String CONTENT_PREVIEW_1080P = "contentPreview1080p";
    private static final String CONTENT_PREVIEW_720P = "contentPreview720p";
    private static final String CONTENT_PREVIEW_480P = "contentPreview480p";
    private static final String CONTENT_PREVIEW_360P = "contentPreview360p";
    private static final String CONTENT_PREVIEW_240P = "contentPreview240p";
    private static final String CONTENT_PREVIEW_144P = "contentPreview144p";

    // content thumbnails
    private static final String THUMBNAIL_1080P = "thumbnail1080p";
    private static final String THUMBNAIL_720P = "thumbnail720p";
    private static final String THUMBNAIL_480P = "thumbnail480p";
    private static final String THUMBNAIL_360P = "thumbnail360p";
    private static final String THUMBNAIL_240P = "thumbnail240p";
    private static final String THUMBNAIL_144P = "thumbnail144p";


    private static final String VIDEO_LENGTH_SECONDS = "videoLengthSeconds";
    private static final String SOCIAL_MEDIA_VIDEO = "socialMediaVideo";

    // content urls
    private static final String CONTENT_URL_1080P_DB = "content_url_1080p";
    private static final String CONTENT_URL_720P_DB  = "content_url_720p";
    private static final String CONTENT_URL_480P_DB  = "content_url_480p";
    private static final String CONTENT_URL_360P_DB  = "content_url_360p";
    private static final String CONTENT_URL_240P_DB  = "content_url_240p";
    private static final String CONTENT_URL_144P_DB  = "content_url_144p";

    // content previews
    private static final String CONTENT_PREVIEW_1080P_DB  = "content_preview_1080p";
    private static final String CONTENT_PREVIEW_720P_DB  = "content_preview_720p";
    private static final String CONTENT_PREVIEW_480P_DB  = "content_preview_480p";
    private static final String CONTENT_PREVIEW_360P_DB  = "content_preview_360p";
    private static final String CONTENT_PREVIEW_240P_DB  = "content_preview_240p";
    private static final String CONTENT_PREVIEW_144P_DB  = "content_preview_144p";

    // content thumbnails
    private static final String THUMBNAIL_1080P_DB  = "thumbnail_1080p";
    private static final String THUMBNAIL_720P_DB  = "thumbnail_720p";
    private static final String THUMBNAIL_480P_DB  = "thumbnail_480p";
    private static final String THUMBNAIL_360P_DB  = "thumbnail_360p";
    private static final String THUMBNAIL_240P_DB  = "thumbnail_240p";
    private static final String THUMBNAIL_144P_DB  = "thumbnail_144p";

    private static final String VIDEO_LENGTH_SECONDS_DB  = "video_length_seconds";
    private static final String SOCIAL_MEDIA_VIDEO_DB  = "social_media_video";

    private static final String CONTENT_TITLE_DB = "content_title";
    private static final String CONTENT_DESCRIPTION_DB = "content_description";
    private static final String CONTENT_PRICE_DB = "content_price";
    private static final String CATEGORY_ID_DB = "category_id";

    private static final String CONTENT_URL_DB = "content_url";


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
        payloadInput.put(CONTENT_URL_360P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_URL_240P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_URL_144P, StaticRules.InputTypes.REG_STRING_OPTIONAL);


        payloadInput.put(CONTENT_PREVIEW_1080P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PREVIEW_720P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PREVIEW_480P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PREVIEW_360P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PREVIEW_240P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(CONTENT_PREVIEW_144P, StaticRules.InputTypes.REG_STRING_OPTIONAL);

        payloadInput.put(THUMBNAIL_1080P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(THUMBNAIL_720P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(THUMBNAIL_480P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(THUMBNAIL_360P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(THUMBNAIL_240P, StaticRules.InputTypes.REG_STRING_OPTIONAL);
        payloadInput.put(THUMBNAIL_144P, StaticRules.InputTypes.REG_STRING_OPTIONAL);

        payloadInput.put(VIDEO_LENGTH_SECONDS, StaticRules.InputTypes.REG_INT_OPTIONAL_ZERO);
        payloadInput.put(SOCIAL_MEDIA_VIDEO, StaticRules.InputTypes.REG_STRING_OPTIONAL);

        payloadInput.put(PROCESSED, StaticRules.InputTypes.REG_STRING_OPTIONAL);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        JsonObject j = context.getPayload().getAsJsonObject();
        String temp_title="", temp_description="", temp_url="",
                temp_url_1080p="",
                temp_url_720p="",
                temp_url_480p="",
                temp_url_360p="",
                temp_url_240p="",
                temp_url_144p="",

                temp_preview_1080p="",
                temp_preview_720p="",
                temp_preview_480p="",
                temp_preview_360p="",
                temp_preview_240p="",
                temp_preview_144p="",

                temp_thumbnail_1080p="",
                temp_thumbnail_720p="",
                temp_thumbnail_480p="",
                temp_thumbnail_360p="",
                temp_thumbnail_240p="",
                temp_thumbnail_144p="",
                temp_social_media_video="";

                int temp_video_length_seconds=0;




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

        /**
         * CONTENT URLS
         */

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
        if (j.get(CONTENT_URL_360P) != null) {
            temp_url_360p = j.get(CONTENT_URL_360P).getAsString();
            updateKeys.add(CONTENT_URL_360P_DB);
        }
        if (j.get(CONTENT_URL_240P) != null) {
            temp_url_240p = j.get(CONTENT_URL_240P).getAsString();
            updateKeys.add(CONTENT_URL_240P_DB);
        }
        if (j.get(CONTENT_URL_144P) != null) {
            temp_url_144p = j.get(CONTENT_URL_144P).getAsString();
            updateKeys.add(CONTENT_URL_144P_DB);
        }
        /**
         * preview URLS
         */

        if (j.get(CONTENT_PREVIEW_1080P) != null) {
            temp_preview_1080p = j.get(CONTENT_PREVIEW_1080P).getAsString();
            updateKeys.add(CONTENT_PREVIEW_1080P_DB);
        }
        if (j.get(CONTENT_PREVIEW_720P) != null) {
            temp_preview_720p = j.get(CONTENT_PREVIEW_720P).getAsString();
            updateKeys.add(CONTENT_PREVIEW_720P_DB);
        }
        if (j.get(CONTENT_PREVIEW_480P) != null) {
            temp_preview_480p = j.get(CONTENT_PREVIEW_480P).getAsString();
            updateKeys.add(CONTENT_PREVIEW_480P_DB);
        }
        if (j.get(CONTENT_PREVIEW_360P) != null) {
            temp_preview_360p = j.get(CONTENT_PREVIEW_360P).getAsString();
            updateKeys.add(CONTENT_PREVIEW_360P_DB);
        }
        if (j.get(CONTENT_PREVIEW_240P) != null) {
            temp_preview_240p = j.get(CONTENT_PREVIEW_240P).getAsString();
            updateKeys.add(CONTENT_PREVIEW_240P_DB);
        }
        if (j.get(CONTENT_PREVIEW_144P) != null) {
            temp_preview_144p = j.get(CONTENT_PREVIEW_144P).getAsString();
            updateKeys.add(CONTENT_PREVIEW_144P_DB);
        }
        /**
         * thumbnail URLS
         */

        if (j.get(THUMBNAIL_1080P) != null) {
            temp_thumbnail_1080p = j.get(THUMBNAIL_1080P).getAsString();
            updateKeys.add(THUMBNAIL_1080P_DB);
        }
        if (j.get(THUMBNAIL_720P) != null) {
            temp_thumbnail_720p = j.get(THUMBNAIL_720P).getAsString();
            updateKeys.add(THUMBNAIL_720P_DB);
        }
        if (j.get(THUMBNAIL_480P) != null) {
            temp_thumbnail_480p = j.get(THUMBNAIL_480P).getAsString();
            updateKeys.add(THUMBNAIL_480P_DB);
        }
        if (j.get(THUMBNAIL_360P) != null) {
            temp_thumbnail_360p = j.get(THUMBNAIL_360P).getAsString();
            updateKeys.add(THUMBNAIL_360P_DB);
        }
        if (j.get(THUMBNAIL_240P) != null) {
            temp_thumbnail_240p = j.get(THUMBNAIL_240P).getAsString();
            updateKeys.add(THUMBNAIL_240P_DB);
        }
        if (j.get(THUMBNAIL_144P) != null) {
            temp_thumbnail_144p = j.get(THUMBNAIL_144P).getAsString();
            updateKeys.add(THUMBNAIL_144P_DB);
        }
        if (j.get(VIDEO_LENGTH_SECONDS) != null) {
            temp_video_length_seconds = j.get(VIDEO_LENGTH_SECONDS).getAsInt();
            updateKeys.add(VIDEO_LENGTH_SECONDS_DB);
        }

        if (j.get(SOCIAL_MEDIA_VIDEO) != null) {
            temp_social_media_video = j.get(SOCIAL_MEDIA_VIDEO).getAsString();
            updateKeys.add(SOCIAL_MEDIA_VIDEO_DB);
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
        final String final_url_360p = temp_url_360p;
        final String final_url_240p = temp_url_240p;
        final String final_url_144p = temp_url_144p;

        final String final_preview_1080p = temp_preview_1080p;
        final String final_preview_720p = temp_preview_720p;
        final String final_preview_480p = temp_preview_480p;
        final String final_preview_360p = temp_preview_360p;
        final String final_preview_240p = temp_preview_240p;
        final String final_preview_144p = temp_preview_144p;

        final String final_thumbnail_1080p = temp_thumbnail_1080p;
        final String final_thumbnail_720p = temp_thumbnail_720p;
        final String final_thumbnail_480p = temp_thumbnail_480p;
        final String final_thumbnail_360p = temp_thumbnail_360p;
        final String final_thumbnail_240p = temp_thumbnail_240p;
        final String final_thumbnail_144p = temp_thumbnail_144p;

        final int final_video_length_seconds = temp_video_length_seconds;

        final String final_social_media_video = temp_social_media_video;


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
                    if (updateKeys.contains(CONTENT_URL_360P_DB)) {
                        ps.setString(count, final_url_360p);
                        System.out.println("Set string " + count + " to " + final_url_360p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_URL_240P_DB)) {
                        ps.setString(count, final_url_240p);
                        System.out.println("Set string " + count + " to " + final_url_240p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_URL_144P_DB)) {
                        ps.setString(count, final_url_144p);
                        System.out.println("Set string " + count + " to " + final_url_144p);
                        count++;
                    }

                    //previews
                    if (updateKeys.contains(CONTENT_PREVIEW_1080P_DB)) {
                        ps.setString(count, final_preview_1080p);
                        System.out.println("Set string " + count + " to " + final_preview_1080p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_PREVIEW_720P_DB)) {
                        ps.setString(count, final_preview_720p);
                        System.out.println("Set string " + count + " to " + final_preview_720p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_PREVIEW_480P_DB)) {
                        ps.setString(count, final_preview_480p);
                        System.out.println("Set string " + count + " to " + final_preview_480p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_PREVIEW_360P_DB)) {
                        ps.setString(count, final_preview_360p);
                        System.out.println("Set string " + count + " to " + final_preview_360p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_PREVIEW_240P_DB)) {
                        ps.setString(count, final_preview_240p);
                        System.out.println("Set string " + count + " to " + final_preview_240p);
                        count++;
                    }
                    if (updateKeys.contains(CONTENT_PREVIEW_144P_DB)) {
                        ps.setString(count, final_preview_144p);
                        System.out.println("Set string " + count + " to " + final_preview_144p);
                        count++;
                    }

                    //thumbnails
                    if (updateKeys.contains(THUMBNAIL_1080P_DB)) {
                        ps.setString(count, final_thumbnail_1080p);
                        System.out.println("Set string " + count + " to " + final_thumbnail_1080p);
                        count++;
                    }
                    if (updateKeys.contains(THUMBNAIL_720P_DB)) {
                        ps.setString(count, final_thumbnail_720p);
                        System.out.println("Set string " + count + " to " + final_thumbnail_720p);
                        count++;
                    }
                    if (updateKeys.contains(THUMBNAIL_480P_DB)) {
                        ps.setString(count, final_thumbnail_480p);
                        System.out.println("Set string " + count + " to " + final_thumbnail_480p);
                        count++;
                    }
                    if (updateKeys.contains(THUMBNAIL_360P_DB)) {
                        ps.setString(count, final_thumbnail_360p);
                        System.out.println("Set string " + count + " to " + final_thumbnail_360p);
                        count++;
                    }
                    if (updateKeys.contains(THUMBNAIL_240P_DB)) {
                        ps.setString(count, final_thumbnail_240p);
                        System.out.println("Set string " + count + " to " + final_thumbnail_240p);
                        count++;
                    }
                    if (updateKeys.contains(THUMBNAIL_144P_DB)) {
                        ps.setString(count, final_thumbnail_144p);
                        System.out.println("Set string " + count + " to " + final_thumbnail_144p);
                        count++;
                    }
                    if (updateKeys.contains(VIDEO_LENGTH_SECONDS_DB)) {
                        ps.setInt(count, final_video_length_seconds);
                        System.out.println("Set string " + count + " to " + final_video_length_seconds);
                        count++;
                    }
                    if (updateKeys.contains(SOCIAL_MEDIA_VIDEO_DB)) {
                        ps.setString(count, final_social_media_video);
                        System.out.println("Set string " + count + " to " + final_social_media_video);
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
