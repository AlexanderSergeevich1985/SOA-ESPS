package com.soaesps.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class BaseHibernateConfiguration {
    private static Logger log = LoggerFactory.getLogger(BaseHibernateConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource restDataSource() {
        final PropertiesExtractor.HibernateExtractor extractor = new PropertiesExtractor.HibernateExtractor(env);
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(extractor.url());
        config.setUsername(extractor.username());
        config.setPassword(extractor.password());
        config.setDriverClassName(extractor.driverClassName());
        config.setMaximumPoolSize(extractor.maxPoolSize());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);

        return transactionManager;
    }
}