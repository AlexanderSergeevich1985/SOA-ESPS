package com.soaesps.notifications.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
// Isolate entity repositories to prevent Spring framework signature identification cross-talk
@EnableJpaRepositories(basePackages = "com.soaesps.notifications.repository.jpa")
@EnableR2dbcRepositories(basePackages = "com.soaesps.notifications.repository.reactive")
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:1}")
    private String password;

    // =========================================================================
    // 1. CLASSIC JDBC / JPA CONFIGURATION
    // =========================================================================

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    // =========================================================================
    // 2. REACTIVE R2DBC CONFIGURATION
    // =========================================================================

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        // Parse database host, port and name directly from the standard JDBC URL string structure
        // Clean URL example: jdbc:postgresql://host.docker.internal:5432/notifications_sql_db
        String cleanUrl = jdbcUrl.replace("jdbc:postgresql://", "");
        String[] parts = cleanUrl.split("/");
        String hostAndPort = parts[0];
        String databaseName = parts[1];

        String host = hostAndPort.split(":")[0];
        int port = Integer.parseInt(hostAndPort.split(":")[1]);

        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host)
                        .port(port)
                        .database(databaseName)
                        .username(username)
                        .password(password)
                        .build()
        );
    }
}
