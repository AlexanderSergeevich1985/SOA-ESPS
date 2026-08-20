package com.soaesps.schedulerservice.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for the scheduler-service.
 *
 * NOTE: in production this class is unnecessary — Spring Boot auto-configures all
 * these beans from application.yml. Keep it only if you need explicit control.
 */
@Configuration
@EnableTransactionManagement
@EntityScan("com.soaesps.schedulerservice.domain")
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackages = "com.soaesps.schedulerservice.repository"
)
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

    @Bean
    public DataSource restDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(maxPoolSize);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource restDataSource) {
        return new JdbcTemplate(restDataSource);
    }

    /**
     * REMOVED: the native Hibernate SessionFactory bean.
     * - org.springframework.orm.hibernate5.LocalSessionFactoryBuilder was deleted in Spring 6.
     * - Having both SessionFactory and EntityManagerFactory causes transaction conflicts.
     * - All business code should go through JPA EntityManager, not native Session.
     */

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource restDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(restDataSource);
        em.setPackagesToScan("com.soaesps.schedulerservice.domain");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();

        props.setProperty("hibernate.show_sql", "true");
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.globally_quoted_identifiers", "true");
        return props;
    }
}