package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.logic.baseapi.WhitespellAPI;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.endpoints.PeakAPI;
import main.com.whitespell.peak.logic.logging.Logging;

public class ApiThread extends Thread {
    boolean running = false;
    public void run() {
        running = true;
        int attempts = 0;
        do {
            try {
                /* start the API */
                WhitespellAPI api = new PeakAPI();
                System.out.println("Starting " + Config.SERVER_NAME + " on main thread.");
                api.startAPI(Config.API_PORT);

            } catch (Exception e) {
                attempts++;
                Logging.log("API problem", e);

                if(attempts > 5) {
                    System.out.println("Couldn't start API on API Thread. Exiting the program.");
                    System.exit(0);
                    return;
                }
            }
        } while (running);
    }


    public void performThreadActions() {
        //System.out.println("backing up  at " + Server.getCalendar().getTimeInMillis());
    }

}