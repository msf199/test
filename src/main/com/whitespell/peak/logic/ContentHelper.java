package main.com.whitespell.peak.logic;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         10/2/15
 *         main.com.whitespell.peak.logic
 */
public class ContentHelper {

    private static final String GET_CONTENT = "SELECT * FROM `content` as ct INNER JOIN `user` as ut ON ct.`user_id` = ut.`user_id` WHERE `content_id` = ? ";
    private static final String GET_POPULAR_BUNDLE = "SELECT * FROM `content` as ct INNER JOIN `user` as ut ON ct.`user_id` = ut.`user_id` WHERE `category_id` = ? AND `processed` = 1 ORDER BY `content_id` DESC LIMIT 1";


    public ContentObject getContentById(RequestObject context,int contentId, int requesterUserId) {
        final ContentObject[] content = {null};

        ContentWrapper contentWrapper = new ContentWrapper(context, requesterUserId);
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT);

            executor.execute(ps -> {
                ps.setInt(1, contentId);

                final ResultSet results = ps.executeQuery();

                if (results.next()) {
                    content[0] = contentWrapper.wrapContent(results);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
        }

        return content[0];
    }


    public ContentObject getPopularBundleByCategoryId(RequestObject context,int categoryId, int requesterUserId) {
        final ContentObject[] content = {null};

        ContentWrapper contentWrapper = new ContentWrapper(context, requesterUserId);
        try {
            StatementExecutor executor = new StatementExecutor(GET_POPULAR_BUNDLE);

            executor.execute(ps -> {
                ps.setInt(1, categoryId);

                final ResultSet results = ps.executeQuery();

                if (results.next()) {
                    content[0] = contentWrapper.wrapContent(results);
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
            context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.UNKNOWN_SERVER_ISSUE);
        }

        return content[0];
    }
}
