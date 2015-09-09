package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.*;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.CommentObject;
import main.com.whitespell.peak.model.DateObject;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         8/6/2015
 */
public class GetContentComments extends EndpointHandler {

    private static final String URL_CONTENT_ID = "contentId";

    private static final String CONTENT_ID = "content_id";
    private static final String COMMENT_USER_ID = "user_id";
    private static final String COMMENT_VALUE = "comment_value";
    private static final String COMMENT_TIMESTAMP = "comment_timestamp";

    private static final String USER_OBJECT_QUERY = "SELECT * FROM `user` WHERE `user_id` = ?";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        int contentId = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Construct the REQUEST COMMENTS query based on the the desired query output.
         */
        StringBuilder selectString = new StringBuilder();
        selectString.append("SELECT * FROM `content_comments` WHERE `content_id` = ? ");
        final String REQUEST_COMMENTS = selectString.toString();

        /**
         * Request the content based on query string
         */
        try {
            ArrayList<CommentObject> comments = new ArrayList<>();
            StatementExecutor executor = new StatementExecutor(REQUEST_COMMENTS);
            final int finalContentId = contentId;

            executor.execute(ps -> {
                ps.setInt(1, finalContentId);

                ResultSet results = ps.executeQuery();

                //display results
                while (results.next()) {
                    UserObject commentUser = new UserObject();
                    try {
                        StatementExecutor executor1 = new StatementExecutor(USER_OBJECT_QUERY);
                        executor1.execute(ps1 -> {
                            final int posterUserId = results.getInt(COMMENT_USER_ID);
                            ps1.setInt(1, posterUserId);

                            ResultSet results2 = ps1.executeQuery();
                            if (results2.next()) {
                                commentUser.setUserId(posterUserId);
                                commentUser.setUserName(results2.getString("username"));
                                commentUser.setThumbnail(results2.getString("thumbnail"));
                            } else {
                                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                                return;
                            }
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.ACCOUNT_NOT_FOUND);
                        return;
                    }

                    CommentObject comment = new CommentObject(results.getInt(CONTENT_ID), results.getString(COMMENT_VALUE), results.getTimestamp(COMMENT_TIMESTAMP));
                    comment.setPoster(commentUser);
                    comments.add(comment);
                }

                /**
                 * Sort comments by timestamp
                 */
                Collections.sort(comments);

                Gson g = new Gson();
                String response = g.toJson(comments);
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
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COMMENTS_NOT_FOUND);
            return;
        }
    }

    public class GetCommentsResponse {

        private int contentId;
        private int userId;
        private String comment;


        public GetCommentsResponse(){
            this.contentId = -1;
            this.userId = -1;
            this.comment = "";
        }

        public GetCommentsResponse(int contentId, int userId, String comment){
            this.contentId = contentId;
            this.userId = userId;
            this.comment = comment;
        }
    }
}
