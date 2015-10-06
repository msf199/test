package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         10/6/2015
 */
public class DeleteComment extends EndpointHandler {

    private static final String DELETE_CONTENT_COMMENT_QUERY = "DELETE FROM `content_comments` WHERE `comment_id` = ? AND `content_id` = ?";

    private static final String GET_COMMENT_USER_QUERY = "SELECT * FROM `content_comments` WHERE `comment_id` = ?";

    /**
     * Define user input variables
     */

    private static final String URL_CONTENT_COMMENT_ID = "commentId";

    private static final String COMMENT_USER_ID_KEY = "user_id";
    private static final String COMMENT_CONTENT_ID_KEY = "content_id";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_COMMENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {

        int commentId = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_COMMENT_ID));

        int[] commentUserId = {0};
        int[] contentId = {0};

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        if (!a.isAuthenticated()) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        try {
            StatementExecutor executor = new StatementExecutor(GET_COMMENT_USER_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, commentId);

                ResultSet results = ps.executeQuery();

                if(results.next()){
                    commentUserId[0] = results.getInt(COMMENT_USER_ID_KEY);
                    contentId[0] = results.getInt(COMMENT_CONTENT_ID_KEY);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        /**
         * Check comment poster and ensure comment poster matches authentication (only commenter can delete comment)
         */
        /**
         * If user is not the commenter they are unauthorized
         */
        if(commentUserId[0] != a.getUserId()){
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHORIZED);
            return;
        }

        /**
         * Delete the content from the DB after attempting to delete from hosting services
         */
        try {
            StatementExecutor executor = new StatementExecutor(DELETE_CONTENT_COMMENT_QUERY);
            executor.execute(ps -> {
                ps.setInt(1, commentId);
                ps.setInt(2, contentId[0]);

                int rows = ps.executeUpdate();
                if(rows > 0){
                    System.out.println("commentid " + commentId + " was deleted successfully");
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }

        context.getResponse().setStatus(HttpStatus.OK_200);
        DeleteCommentResponse object = new DeleteCommentResponse();
        object.setCommentDeleted(true);
        Gson g = new Gson();
        String json = g.toJson(object);
        try {
            context.getResponse().getWriter().write(json);
        }catch(Exception e) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
            return;
        }
    }

    public class DeleteCommentResponse {

        private boolean success;

        public boolean commentDeleted() {
            return this.success;
        }

        public void setCommentDeleted(boolean success) {
            this.success = success;
        }
    }
}