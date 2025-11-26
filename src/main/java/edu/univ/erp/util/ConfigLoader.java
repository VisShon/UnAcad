package edu.univ.erp.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to load configuration from config.properties file.
 */
public class ConfigLoader {
    private static Properties properties;
    private static final String CONFIG_FILE = "/config.properties";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        properties = new Properties();
        try (InputStream input = ConfigLoader.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Warning: config.properties not found. Using default values.");
                setDefaults();
                return;
            }
            properties.load(input);
        } catch (Exception e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
            System.err.println("Using default values.");
            setDefaults();
        }
    }

    private static void setDefaults() {
        properties.setProperty("db.auth.url", "jdbc:mysql://localhost:3306/auth_db");
        properties.setProperty("db.auth.username", "root");
        properties.setProperty("db.auth.password", "");
        properties.setProperty("db.erp.url", "jdbc:mysql://localhost:3306/erp_db");
        properties.setProperty("db.erp.username", "root");
        properties.setProperty("db.erp.password", "");
        properties.setProperty("db.pool.maxSize", "5");
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}