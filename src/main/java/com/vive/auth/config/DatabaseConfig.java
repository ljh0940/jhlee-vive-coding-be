package com.vive.auth.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Slf4j
@Configuration
@Profile("railway")
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        // Railway DATABASE_URL 형식: postgresql://user:password@host:port/database
        // JDBC 형식으로 변환: jdbc:postgresql://host:port/database

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }

        String jdbcUrl;
        String username = null;
        String password = null;

        try {
            // postgresql:// 를 jdbc:postgresql:// 로 변환
            if (databaseUrl.startsWith("postgresql://")) {
                jdbcUrl = "jdbc:" + databaseUrl;

                // URL에서 username과 password 추출
                // 형식: postgresql://username:password@host:port/database
                String withoutProtocol = databaseUrl.substring("postgresql://".length());

                if (withoutProtocol.contains("@")) {
                    String credentials = withoutProtocol.substring(0, withoutProtocol.indexOf("@"));

                    if (credentials.contains(":")) {
                        String[] parts = credentials.split(":", 2);
                        username = parts[0];
                        password = parts[1];

                        // JDBC URL에서 credentials 제거
                        String hostAndDb = withoutProtocol.substring(withoutProtocol.indexOf("@") + 1);
                        jdbcUrl = "jdbc:postgresql://" + hostAndDb;
                    }
                }
            } else {
                jdbcUrl = databaseUrl;
            }

            log.info("Connecting to database: {}", jdbcUrl.replaceAll(":[^:@]+@", ":***@"));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            if (username != null) {
                config.setUsername(username);
            }
            if (password != null) {
                config.setPassword(password);
            }
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            return new HikariDataSource(config);

        } catch (Exception e) {
            log.error("Failed to configure database connection", e);
            throw new RuntimeException("Database configuration failed", e);
        }
    }
}
