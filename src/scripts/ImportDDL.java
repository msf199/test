package scripts;

import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/21/15
 *         scripts
 */

/**
 * ImportDDL Imports the database schema from the database in config.prop and dumps it in the /ddl directory
 * You can then run the tests with this script very easily.
 */
public class ImportDDL {

    private static StringBuilder s = new StringBuilder();
    public static ArrayList<String> tables = new ArrayList<>();


    public static void main(String[] args) throws Exception {

        Server.readConfigs();

        System.out.println("Getting tables");

        // add model tables
        tables.add("user");
        tables.add("category");
        tables.add("content_type");
        tables.add("content");
        tables.add("content_curation");
        tables.add("newsfeed");
        tables.add("saved_content");
        tables.add("bundles");
        tables.add("content_likes");

        // add reference tables
        tables.add("user_following");
        tables.add("category_following");
        tables.add("category_publishing");
        tables.add("authentication");
        tables.add("bundle_content");
        tables.add("content_comments");

        //build the DDL

        for(final String table: tables) {
            try {
                StatementExecutor executor = new StatementExecutor("show create table " + table);
                executor.execute(ps -> {

                    final ResultSet results = ps.executeQuery();
                    while (results.next()) {

                        String table_ddl = results.getString("Create Table");
                        s.append(table_ddl + ";");
                    }
                });
            } catch (SQLException e) {
                Logging.log("High", e);
            }
        }
        PrintWriter writer = new PrintWriter("ddl/"+Config.DB+".sql", "UTF-8");
        writer.println(s.toString());
        writer.close();
    }
}
