package com.soaesps.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class BaseHibernateConfiguration {

    // HikariCP connection pool infrastructure properties
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriver;

    @Value("${spring.datasource.hikari.maximum-pool-size:4}")
    private int maxPoolSize;

    // Hibernate engine configuration properties with flexible key fallbacks
    @Value("${spring.jpa.database-platform:${spring.datasource.jpa.database-platform:org.hibernate.dialect.PostgreSQLDialect}}")
    private String dialect;

    @Value("${spring.jpa.show-sql:${spring.datasource.jpa.show_sql:false}}")
    private String showSql;

    @Value("${spring.jpa.hibernate.ddl-auto:${hibernate.hbm2ddl.auto:validate}}")
    private String ddlAuto;

    @Value("${hibernate.globally_quoted_identifiers:true}")
    private String globallyQuotedIdentifiers;

    @Bean
    public DataSource restDataSource() {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(dbDriver);
        config.setMaximumPoolSize(maxPoolSize);

        // Performance optimizations for prepared statements caching
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    /**
     * Unwraps traditional Hibernate SessionFactory directly from the JPA context.
     * Guarantees compatibility for both modern repository layers and legacy DAOs.
     */
    @Bean
    public SessionFactory sessionFactory(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory.unwrap(SessionFactory.class) == null) {
            throw new IllegalStateException("The configured EntityManagerFactory is not a Hibernate factory!");
        }
        return entityManagerFactory.unwrap(SessionFactory.class);
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Aggregates extracted property fields into a structured Properties object for Hibernate vendor configurations.
     */
    protected Properties baseHibernateProperties() {
        final Properties props = new Properties();
        props.setProperty("hibernate.dialect", dialect);
        props.setProperty("hibernate.show_sql", showSql);
        props.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        props.setProperty("hibernate.globally_quoted_identifiers", globallyQuotedIdentifiers);
        return props;
    }
}