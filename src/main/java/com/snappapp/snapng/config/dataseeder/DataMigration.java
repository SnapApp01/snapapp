package com.snappapp.snapng.config.dataseeder;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataMigration {

    private static final Logger logger = LoggerFactory.getLogger(DataMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public DataMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrateData() {
        try {
            int deletedRows = jdbcTemplate.update(
                    "DELETE FROM user_roles WHERE user_id NOT IN (SELECT id FROM snap_user)"
            );
            logger.info("Cleaned up {} orphaned user_roles records", deletedRows);
        } catch (Exception e) {
            logger.error("Failed to clean up orphaned user_roles records: {}", e.getMessage(), e);
        }
    }
}