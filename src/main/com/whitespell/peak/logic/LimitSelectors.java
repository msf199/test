package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.logic.logging.Logging;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/20/15
 *         whitespell.logic
 */
public class LimitSelectors {

    /**
     * The LimitSelectors class is a class that stores the limits to queries to optimize them.
     * For example the amount of users somebody follows can never be more than the amount of users
     * The amount of category one follows can never be more than the amount of category
     * etc.
     */

    // we initialize them at max value so that no errors occur if server starts and no results are in yet.

    public static int CATEGORY_AMOUNT = Integer.MAX_VALUE;
    public static String CATEGORY_AMOUNT_QUERY = "SELECT COUNT(1) FROM `category`";
    public static int CONTENT_TYPE_AMOUNT = Integer.MAX_VALUE;
    public static String CONTENT_TYPE_QUERY = "SELECT COUNT(1) FROM `category`";
    public static int USER_AMOUNT = Integer.MAX_VALUE;
    public static String USER_AMOUNT_QUERY = "SELECT COUNT(1) FROM `category`";
    public static int CONTENT_AMOUNT = Integer.MAX_VALUE;

    public class LimitSelectorThread extends Thread {
        private boolean running = false;
        private long now;
        private long SECOND_NS = 1000000000; // 1 second
        private long DELAY_NS = 60 * SECOND_NS;
        private long lastFrameNs;

        public void run() {
            running = true;
            do {
                try {
                    tick();
                } catch (Exception e) {
                    Logging.log("LimitSelector problem", e);
                }
            } while (running);
        }

        public void tick() {
            while (now - lastFrameNs < DELAY_NS) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                now = System.nanoTime();
            }

            lastFrameNs = System.nanoTime();

            performThreadActions();
        }

        public void performThreadActions() {
            //System.out.println("backing up  at " + Server.getCalendar().getTimeInMillis());
        }

    }
}
