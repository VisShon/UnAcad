package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import edu.univ.erp.util.ConfigLoader;

public class DataSourceFactory {

    private static HikariDataSource authDB;
    private static HikariDataSource erpDB;

    public static HikariDataSource getAuthDB() {
        if (authDB == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(ConfigLoader.getProperty("db.auth.url", "jdbc:mysql://localhost:3306/auth_db"));
            cfg.setUsername(ConfigLoader.getProperty("db.auth.username", "root"));
            cfg.setPassword(ConfigLoader.getProperty("db.auth.password", ""));
            cfg.setMaximumPoolSize(ConfigLoader.getIntProperty("db.pool.maxSize", 5));
            authDB = new HikariDataSource(cfg);
        }
        return authDB;
    }

    public static HikariDataSource getErpDB() {
        if (erpDB == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(ConfigLoader.getProperty("db.erp.url", "jdbc:mysql://localhost:3306/erp_db"));
            cfg.setUsername(ConfigLoader.getProperty("db.erp.username", "root"));
            cfg.setPassword(ConfigLoader.getProperty("db.erp.password", ""));
            cfg.setMaximumPoolSize(ConfigLoader.getIntProperty("db.pool.maxSize", 5));
            erpDB = new HikariDataSource(cfg);
        }
        return erpDB;
    }
}