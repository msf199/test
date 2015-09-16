package main.com.whitespell.peak.logic.endpoints.content;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         8/5/2015
 */
public class AddContentComment extends EndpointHandler {

    private static final String INSERT_CONTENT_COMMENT_QUERY = "INSERT INTO `content_comments`(`user_id`, `content_id`, `comment_value`) VALUES (?,?,?)";

    private static final String PAYLOAD_USER_ID_KEY = "userId";
    private static final String URL_CONTENT_ID = "contentId";
    private static final String PAYLOAD_COMMENT = "comment";

    @Override
    protected void setUserInputs() {
        urlInput.put(URL_CONTENT_ID, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_USER_ID_KEY, StaticRules.InputTypes.REG_INT_REQUIRED);
        payloadInput.put(PAYLOAD_COMMENT, StaticRules.InputTypes.REG_STRING_REQUIRED);
    }

    @Override
    public void safeCall(RequestObject context) throws IOException {
        JsonObject payload = context.getPayload().getAsJsonObject();

        final int content_id = Integer.parseInt(context.getUrlVariables().get(URL_CONTENT_ID));

        final int user_id = payload.get(PAYLOAD_USER_ID_KEY).getAsInt();
        final String comment = payload.get(PAYLOAD_COMMENT).getAsString();

        /**
         * Ensure that the user is authenticated properly
         */

        final Authentication a = new Authentication(context.getRequest().getHeader("X-Authentication"));

        boolean isMe = (user_id == a.getUserId());

        if (!a.isAuthenticated() || !isMe) {
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_AUTHENTICATED);
            return;
        }

        /**
         * Add comment to the content
         */
        AddContentCommentObject commentAdded = new AddContentCommentObject();
        try {
            StatementExecutor executor = new StatementExecutor(INSERT_CONTENT_COMMENT_QUERY);
            executor.execute(ps -> {
                ps.setString(1, String.valueOf(user_id));
                ps.setInt(2, content_id);
                ps.setString(3, comment);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    commentAdded.setCommentAdded(true);
                    Gson g = new Gson();
                    String response = g.toJson(commentAdded);
                    context.getResponse().setStatus(200);
                    try {
                        context.getResponse().getWriter().write(response);
                    } catch (Exception e) {
                        Logging.log("High", e);
                        context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
                        return;
                    }
                } else {
                    context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COMMENT_NOT_POSTED);
                    return;
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.COMMENT_NOT_POSTED);
            return;
        }
    }

    public class AddContentCommentObject {

        public AddContentCommentObject(){
            commentAdded = false;
        }

        private boolean commentAdded;

        public boolean isCommentAdded() {
            return commentAdded;
        }

        public void setCommentAdded(boolean commentAdded) {
            this.commentAdded = commentAdded;
        }
    }
}
