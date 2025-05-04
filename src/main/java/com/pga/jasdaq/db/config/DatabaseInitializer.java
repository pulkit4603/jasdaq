package com.pga.jasdaq.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing database...");
        
        // Create trades table if it doesn't exist
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS trades (" +
            "  id BIGINT PRIMARY KEY AUTO_INCREMENT," +
            "  symbol VARCHAR(10) NOT NULL," +
            "  price DECIMAL(10, 2) NOT NULL," +
            "  volume INT NOT NULL," +
            "  timestamp DATETIME NOT NULL," +
            "  order_type VARCHAR(10) NOT NULL" +
            ")"
        );
        
        logger.info("Database initialized successfully");
    }
}