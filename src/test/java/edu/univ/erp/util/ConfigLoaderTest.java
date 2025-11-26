package edu.univ.erp.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("ConfigLoader Tests")
class ConfigLoaderTest {

    @Test
    @DisplayName("Should load database URL property")
    void testLoadDatabaseUrl() {
        String dbUrl = ConfigLoader.getProperty("db.erp.url", null);
        
        assertNotNull(dbUrl, "Database URL should be configured");
        assertThat(dbUrl, startsWith("jdbc:mysql://"));
    }

    @Test
    @DisplayName("Should load database username")
    void testLoadDatabaseUsername() {
        String dbUser = ConfigLoader.getProperty("db.erp.username", null);
        
        assertNotNull(dbUser, "Database username should be configured");
        assertThat(dbUser, not(emptyString()));
    }

    @Test
    @DisplayName("Should handle missing property with default value")
    void testMissingPropertyWithDefault() {
        String value = ConfigLoader.getProperty("nonexistent.property", "default_value");
        
        assertEquals("default_value", value);
    }

    @Test
    @DisplayName("Should load all required database properties")
    void testAllDatabaseProperties() {
        String erpUrl = ConfigLoader.getProperty("db.erp.url", null);
        String erpUser = ConfigLoader.getProperty("db.erp.username", null);
        String erpPassword = ConfigLoader.getProperty("db.erp.password", "");
        
        String authUrl = ConfigLoader.getProperty("db.auth.url", null);
        String authUser = ConfigLoader.getProperty("db.auth.username", null);
        String authPassword = ConfigLoader.getProperty("db.auth.password", "");
        
        assertNotNull(erpUrl, "ERP database URL should exist");
        assertNotNull(erpUser, "ERP database username should exist");
        // Password can be empty in test config
        assertNotNull(erpPassword);
        
        assertNotNull(authUrl, "Auth database URL should exist");
        assertNotNull(authUser, "Auth database username should exist");
        assertNotNull(authPassword);
    }

    @Test
    @DisplayName("Should validate database URLs format")
    void testDatabaseUrlFormat() {
        String erpUrl = ConfigLoader.getProperty("db.erp.url", null);
        String authUrl = ConfigLoader.getProperty("db.auth.url", null);
        
        if (erpUrl != null) {
            assertThat(erpUrl, matchesPattern("jdbc:mysql://.*"));
        }
        
        if (authUrl != null) {
            assertThat(authUrl, matchesPattern("jdbc:mysql://.*"));
        }
    }

    @Test
    @DisplayName("Should load integer property")
    void testLoadIntProperty() {
        int poolSize = ConfigLoader.getIntProperty("db.pool.size", 10);
        
        assertThat(poolSize, greaterThan(0));
    }

    @Test
    @DisplayName("Should return default for missing int property")
    void testMissingIntProperty() {
        int value = ConfigLoader.getIntProperty("nonexistent.int.property", 42);
        
        assertEquals(42, value);
    }
}
