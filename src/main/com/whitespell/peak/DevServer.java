package main.com.whitespell.peak;

import main.com.whitespell.peak.logic.VideoProcessingManager;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.exec.ShellExecution;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         10/15/15
 *         main.com.whitespell.peak.security
 */
public class DevServer {

    public static void main(String[] args) throws Exception {

        Config.CONFIGURATION_FILE = "config.prop";
        Server.start();
    }

}
