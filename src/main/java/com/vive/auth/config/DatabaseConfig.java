package com.vive.auth.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("railway")
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            // Railway 형식 변환: postgres:// -> jdbc:postgresql://
            databaseUrl = databaseUrl.replace("postgres://", "jdbc:postgresql://");
        } else if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            // postgresql:// -> jdbc:postgresql://
            databaseUrl = "jdbc:" + databaseUrl;
        }

        if (databaseUrl != null) {
            return DataSourceBuilder
                    .create()
                    .url(databaseUrl)
                    .build();
        }

        // Fallback to default configuration
        return DataSourceBuilder.create().build();
    }
}
