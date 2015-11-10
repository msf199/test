package main.com.whitespell.peak.logic.health;

import javapns.Push;
import javapns.notification.PushNotificationPayload;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.exec.ShellExecution;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import org.apache.log4j.BasicConfigurator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Pim de Witte(wwadewitte) and Cory McAn(cmcan), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class HealthCheckThread extends Thread {


    private static final String GET_CONTENT_PROCESSED = "SELECT COUNT(1) FROM `content` WHERE `processed` = 0";
    private static final String GET_AVAILABLE_PROCESSING_INSTANCES = "SELECT 1 FROM `avcpvm_monitoring` WHERE `queue_size` < 3 AND `shutdown_reported` = 0 AND (`last_ping` IS NULL AND `creation_time` > ? OR `last_ping` > ?)";
    private boolean running = false;

    final int[] unprocessedVideos = {0};

    public void run() {
        running = true;

        do {

           if(hasUnprocessedContent()) {

               int unprocessed = unprocessedVideos[0];

               try {
                   StatementExecutor executor = new StatementExecutor(GET_AVAILABLE_PROCESSING_INSTANCES);
                   final Timestamp min_15_ago = new Timestamp(Server.getCalendar().getTimeInMillis() - (60 * 1000 * 15)); // 15 mins max
                   executor.execute(ps -> {

                       ps.setTimestamp(1,min_15_ago);
                       ps.setTimestamp(2,min_15_ago);
                       ResultSet r = ps.executeQuery();
                       if (!r.next() && !Config.TESTING){
                           Logging.log("INFO", "not enough video nodes, inserting one");

                           int nodesToCreate = unprocessed < 3 ? 1 : (unprocessed / 3); // we allow a queue of 3 per node

                           for(int i = 0; i < nodesToCreate; i++) {
                               ShellExecution.createAndInsertVideoConverter();
                           }

                       } else {
                           Logging.log("INFO", "we have enough video nodes");
                       }
                   });
               } catch (SQLException e) {
                   Logging.log("High", e);
               }
           }


            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (running);

    }

    boolean hasUnprocessedContent() {
        final boolean[] unprocessedContent = {false};
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_PROCESSED);
            executor.execute(ps -> {

                ResultSet r = ps.executeQuery();
                if (r.next()){
                    int count = r.getInt("ct");
                    if(count > 0 ) {
                        unprocessedVideos[0] = count;
                        unprocessedContent[0] = true;
                    } else {
                        Logging.log("INFO", "we have enough video nodes");
                        unprocessedContent[0] = false;
                    }


                } 
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    return unprocessedContent[0];
    }

}
