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
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Pim de Witte(wwadewitte) and Cory McAn(cmcan), Whitespell LLC
 *         9/18/15
 *         main.com.whitespell.peak.logic.notifications
 */
public class HealthCheckThread extends Thread {


    private static final String GET_CONTENT_PROCESSED = "SELECT COUNT(1) as ct FROM `content` WHERE `processed` = 0";
    private static final String GET_AVAILABLE_PROCESSING_INSTANCES = "SELECT COUNT(1) as ct FROM `avcpvm_monitoring` WHERE `shutdown_reported` = 0 AND (`last_ping` IS NULL AND `creation_time` > ? OR `last_ping` > ?)";
    private static final String DELETE_NODES = "SELECT `instance_id` FROM `avcpvm_monitoring` WHERE `shutdown_reported` = 1 OR `last_ping` IS NULL OR (`creation_time` < ? AND `last_ping` < ?)";
    private static final String DELETE_OLD_NODE = "DELETE FROM `avcpvm_monitoring` WHERE `instance_id` = ?";
    private boolean running = false;

    public void run() {


        if(Config.isDev()) {
            return;
        }

        running = true;

        do {

              deleteOldNodes();


               int unprocessed = getUnprocessedContent();

               try {
                   StatementExecutor executor = new StatementExecutor(GET_AVAILABLE_PROCESSING_INSTANCES);
                   final Timestamp min_15_ago = new Timestamp(Server.getMilliTime() - (60 * 1000 * 15)); // 15 mins max
                   executor.execute(ps -> {

                       ps.setTimestamp(1,min_15_ago);
                       ps.setTimestamp(2,min_15_ago);
                       ResultSet r = ps.executeQuery();
                       if (r.next()){
                           // count of available instances
                           int instanceCount = r.getInt("ct");


                           if(unprocessed <= 0) {
                               Logging.log("INFO", "We dont need nodes");
                           } else {

                               Logging.log("INFO", "not enough video nodes, inserting one");

                               int nodesToCreate = (unprocessed - instanceCount); // we allow a queue of 3 per node


                               Logging.log("HIGH", "Creating " + unprocessed + ":" + nodesToCreate + " nodes");

                               for(int i = 0; i < nodesToCreate; i++) {
                                   ShellExecution.createAndInsertVideoConverter();
                               }

                               // giving it 3 minutes to start up and initialize
                               try {
                                   Thread.sleep(180000);
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }


                           }

                       } else {
                           Logging.log("INFO", "we have enough video nodes");
                       }
                   });
               } catch (SQLException e) {
                   Logging.log("High", e);
               }


            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (running);

    }

    private void deleteOldNodes() {
        try {
            StatementExecutor executor = new StatementExecutor(DELETE_NODES);
            final Timestamp min_15_ago = new Timestamp(Server.getMilliTime() - (60 * 1000 * 15)); // 15 mins max
            executor.execute(ps -> {

                ps.setTimestamp(1,min_15_ago);
                ps.setTimestamp(2,min_15_ago);
                ResultSet r = ps.executeQuery();
                while(r.next()){
                    // count of available instances
                    String instance_id = r.getString("instance_id");
                    int s = ShellExecution.deleteNode(instance_id);

                    if(s != 0 ) {
                        Logging.log("HIGH", "Delete node didn't exit as 0, not deleting");
                        return;
                    }

                    try {
                        StatementExecutor executor2 = new StatementExecutor(DELETE_OLD_NODE);
                        executor2.execute(ps2 -> {

                            ps2.setString(1,instance_id);
                            ps2.executeUpdate();
                        });
                    } catch (SQLException e) {
                        Logging.log("High", e);
                    }


                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    }

    int getUnprocessedContent() {
        final int[] unprocessedContent = {0};
        try {
            StatementExecutor executor = new StatementExecutor(GET_CONTENT_PROCESSED);
            executor.execute(ps -> {

                ResultSet r = ps.executeQuery();
                if (r.next()){
                    int count = r.getInt("ct");
                    if(count > 0 ) {
                        unprocessedContent[0] = count;
                    } else {
                        Logging.log("INFO", "we have enough video nodes");

                    }

                }
            });
        } catch (SQLException e) {
            Logging.log("High", e);
        }
    return unprocessedContent[0];
    }

}
