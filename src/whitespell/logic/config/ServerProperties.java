package whitespell.logic.config;

import whitespell.logic.logging.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public final class ServerProperties {

    private static File configFile = null;

    public File getConfigFile() {
        return configFile;
    }
    public ServerProperties(String file) {
        configFile = new File(file);
    }

	public static final Properties properties = new Properties();

	public static void read() {
		if (configFile.exists()) {
			try {
				properties.load(new FileInputStream(configFile));
			} catch (FileNotFoundException e) {
				Logging.log("STOP(CONFIG FILE WAS NOT FOUND!)", e);
			} catch (IOException e) {
				Logging.log("STOP(CONFIG FILE THREW IO EXCEPTION)",e);
			}
			Config.SERVER_NAME =(properties.getProperty("SERVER_NAME"));
			Config.SERVER_VERSION = Integer.parseInt(properties.getProperty("SERVER_VERSION"));
            Config.SERVER_TIMEZONE = (properties.getProperty("SERVER_TIMEZONE"));
			Config.API_PORT = Integer.parseInt(properties.getProperty("API_PORT"));
			Config.MAX_ERROR_FOLDER_SIZE_MB = Integer.parseInt(properties.getProperty("MAX_ERROR_FOLDER_SIZE_MB"));
            Config.ERROR_PATH = (properties.getProperty("ERROR_PATH"));
		} else {
            Logging.log("WARNING","Configuration file was not found in project root. Now running from config.Config default properties");
        }
	}

}
