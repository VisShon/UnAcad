package edu.univ.erp.access;

import edu.univ.erp.data.DataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Handles system-wide maintenance mode.
 * When ON → Students/Instructors cannot modify anything.
 *
 * The value is stored in: erp_db.settings (key = 'maintenance').
 */
public class MaintenanceManager {

    private static boolean cachedValue = false;
    private static boolean loaded = false;

    /**
     * Reads maintenance mode from DB (with cached result).
     */
    public static boolean isReadOnly() {
        if (loaded) return cachedValue;

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT value FROM settings WHERE `key` = 'maintenance'")) {

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String v = rs.getString("value");
                cachedValue = v != null && v.equalsIgnoreCase("ON");
            } else {
                // default OFF
                cachedValue = false;
            }

            loaded = true;
            return cachedValue;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // fail-safe
        }
    }

    /**
     * Toggles the maintenance flag in DB + cache.
     */
    public static void setReadOnly(boolean on) throws Exception {
        String val = on ? "ON" : "OFF";

        try (Connection c = DataSourceFactory.getErpDB().getConnection()) {

            // Ensure row exists
            String insert = """
                    INSERT INTO settings (`key`, `value`)
                    VALUES ('maintenance', ?)
                    ON DUPLICATE KEY UPDATE value = VALUES(value)
                    """;

            try (PreparedStatement ps = c.prepareStatement(insert)) {
                ps.setString(1, val);
                ps.executeUpdate();
            }

            // update cache
            cachedValue = on;
            loaded = true;
        }
    }
}