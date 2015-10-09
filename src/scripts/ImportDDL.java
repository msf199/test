package scripts;

import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.io.PrintWriter;
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
        tables.add("content_saved");
        tables.add("content_likes");
        tables.add("content_views");
        tables.add("feedback");
        tables.add("reporting");
        tables.add("notification");
        //requires backticks, `order` is reserved
        tables.add("`order`");

        // add reference tables
        tables.add("user_following");
        tables.add("category_following");
        tables.add("category_publishing");
        tables.add("device");
        tables.add("authentication");
        tables.add("bundle_match");
        tables.add("content_comments");
        tables.add("content_access");
        tables.add("fb_user");
        tables.add("reporting_type");
        tables.add("order_origin");
        tables.add("order_status");
        tables.add("order_type");

        //build the DDL

        for(final String table: tables) {
            try {
                StatementExecutor executor = new StatementExecutor("show create table " + table);
                executor.execute(ps -> {

                    System.out.println("table: "  + table);

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
