package main.java.org.abullard1;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigLoader class for loading the config.properties file.
 */
public class ConfigLoader {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE_PATH = "/config.properties";

    static {
        initializeProperties();
    }

    private static void initializeProperties() {
        try (InputStream input = ConfigLoader.class.getResourceAsStream(CONFIG_FILE_PATH)) {
            if (input == null) {
                System.out.println("Unable to find the properties file at " + CONFIG_FILE_PATH);
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}