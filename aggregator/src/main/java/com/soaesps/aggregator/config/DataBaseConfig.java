package com.soaesps.aggregator.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Pure JDBC database configuration for the scheduler-service.
 * Fully stripped of Hibernate/JPA components to enable high-throughput, low-latency execution.
 */
@Configuration
@EnableTransactionManagement
public class DataBaseConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/soa_esps}")
    private String url;

    @Value("${spring.datasource.username:espssoa}")
    private String username;

    @Value("${spring.datasource.password:espssoa}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:4}")
    private int maxPoolSize;

    /**
     * Configures the native Hikari Data Source targeting the TimescaleDB infrastructure.
     */
    @Bean
    public DataSource restDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(maxPoolSize);

        // High-performance JDBC pre-compilation parameters optimization
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    /**
     * Pure JdbcTemplate abstraction replacing heavy JPA repositories.
     * Directly interacts with raw DB connections pool without any ORM parsing overhead.
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource restDataSource) {
        return new JdbcTemplate(restDataSource);
    }

    /**
     * Native JDBC transaction manager instead of JpaTransactionManager.
     * Provides rollback safety for database batch executions without requiring Hibernate Session context.
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource restDataSource) {
        return new DataSourceTransactionManager(restDataSource);
    }

    /**
     * Configures serialization settings for inbound/outbound payload parsing.
     */
    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }
}