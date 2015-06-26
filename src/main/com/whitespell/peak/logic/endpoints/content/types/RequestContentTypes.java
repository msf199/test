package main.com.whitespell.peak.logic.endpoints.content.types;

import com.google.gson.Gson;
import main.com.whitespell.peak.logic.EndpointInterface;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentTypeObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class RequestContentTypes extends EndpointInterface {


    private static final String GET_CONTENT_TYPES = "SELECT * FROM `content_type`";

    @Override
    public void call(final RequestObject context) throws IOException {
        /**
         * Get the content types
         */
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_TYPES);
            executor.execute(new ExecutionBlock() {
                @Override
                public void process(PreparedStatement ps) throws SQLException {

                    final ResultSet results = ps.executeQuery();
                    ArrayList<ContentTypeObject> contentTypes = new ArrayList<>();
                    while (results.next()) {

                        ContentTypeObject d = new ContentTypeObject(results.getInt("content_type_id"), results.getString("content_type_name"));

                        contentTypes.add(d);
                    }

                    // put the array list into a JSON array and write it as a response

                    Gson g = new Gson();
                    String response = g.toJson(contentTypes);
                    context.getResponse().setStatus(200);
                    try {
                        context.getResponse().getWriter().write(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }

}
