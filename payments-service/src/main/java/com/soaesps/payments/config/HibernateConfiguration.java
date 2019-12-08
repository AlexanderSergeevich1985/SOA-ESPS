package com.soaesps.payments.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EntityScan({"com.soaesps.payments"})
@EnableJpaRepositories(basePackages = {"com.soaesps.payments.repository"})
@EnableTransactionManagement
public class HibernateConfiguration {
    private static Logger log = LoggerFactory.getLogger(HibernateConfiguration.class);

    private static final int DEFAULT_CONNECTION_POOL_SIZE = 4;

    @Autowired
    private Environment env;

    @Bean
    public SessionFactory getSessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(restDataSource());
        sessionFactory.setPackagesToScan("com.soaesps.payments.DataModels");
        sessionFactory.setHibernateProperties(hibernateProperties());

        return sessionFactory.getObject();
    }

    @Bean
    public DataSource restDataSource() {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getProperty("spring.datasource.url"));
        config.setUsername(env.getProperty("spring.datasource.username"));
        config.setPassword(env.getProperty("spring.datasource.password"));
        config.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        final String mps = env.getProperty("spring.datasource.hikari.maximum-pool-size");
        config.setMaximumPoolSize(mps != null ? Integer.valueOf(mps) : DEFAULT_CONNECTION_POOL_SIZE);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    @Bean
    @Autowired
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager(sessionFactory);

        return txManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect", env.getProperty("spring.datasource.jpa.database-platform"));
        props.setProperty("hibernate.show_sql", env.getProperty("spring.datasource.jpa.show_sql"));
        props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        props.setProperty("hibernate.globally_quoted_identifiers", "true");

        return props;
    }
}