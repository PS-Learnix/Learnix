package com.learnix.web.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Repara el historial de esquemas de Flyway antes de ejecutar las migraciones.
            // Esto resuelve los problemas de desajuste de checksums (checksum mismatch) en desarrollo.
            flyway.repair();
            flyway.migrate();
        };
    }
}
