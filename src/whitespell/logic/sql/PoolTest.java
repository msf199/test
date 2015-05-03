package whitespell.logic.sql;

import whitespell.logic.Safety;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         5/3/15
 *         whitespell.logic.sql
 */
public class PoolTest {

    public static void start(int iterations) throws SQLException {

        Pool.initializePool();

        // dataset that holds the performance time of each result
        int[] tests = new int[iterations];

        for (int i = 0; i < iterations; i++) {
            Connection con = null;
            try {

                long start = System.currentTimeMillis();

                con = Pool.getConnection();

                PreparedStatement p = con.prepareStatement("SELECT * from `users` WHERE `user_id` = ?;");

                p.setInt(1, 1);

                ResultSet s = p.executeQuery();

                while (s.next()) {
                    System.out.println(s.getString("username"));
                }

                long end = System.currentTimeMillis();

                tests[i] = Safety.safeLongToInt(end - start);

            } finally {
                if (con != null)
                    con.close();
            }
         }

        // process the created dataset and create useful metrics

        int total = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;
        int median = 0;

        for(int i = 0; i < iterations; i++) {
            total += tests[i];

            if(tests[i] > max) {
                max = tests[i];
            } else if(tests[i] < min) {
                min = tests[i];
            }

            if(i == iterations / 2) {
                median = tests[i];
            }
        }

        int avg = total / iterations;

        System.out.println("Averaged " + avg + " ms in " + iterations + " queries with a max of "+max+" and a min of "+min+" with median "+median+".");
    }
}
