package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.logic.baseapi.WhitespellAPI;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.PeakAPI;
import main.com.whitespell.peak.logic.logging.Logging;

public class ApiThread extends Thread {
    boolean running = false;
    public void run() {
        running = true;
        do {
            try {
                /* start the API */
                WhitespellAPI api = new PeakAPI();
                System.out.println("Starting " + Config.SERVER_NAME + " on main thread.");
                api.startAPI(Config.API_PORT);

            } catch (Exception e) {
                Logging.log("LimitSelector problem", e);
            }
        } while (running);
    }


    public void performThreadActions() {
        //System.out.println("backing up  at " + System.currentTimeMillis());
    }

}