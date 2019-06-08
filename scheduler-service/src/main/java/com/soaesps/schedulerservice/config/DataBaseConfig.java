package com.soaesps.schedulerservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableAutoConfiguration
@PropertySource("classpath:config/application.yaml")
@ConfigurationProperties(prefix = "spring.hibernate.connection")
@Validated
@EnableTransactionManagement
/*@EntityScan({"com.soaesps.schedulerservice.domain"})
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackages = {"com.soaesps.schedulerservice.repository"})*/
public class DataBaseConfig {
    private String url;

    private String username;

    private String password;

    private String driverClassName;

    private Integer maxPoolSize;

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
    public JdbcTemplate jdbcTemplate(final DataSource restDataSource) {
        return new JdbcTemplate(restDataSource);
    }

    @Bean
    public SessionFactory getSessionFactory(final DataSource restDataSource) {
        LocalSessionFactoryBuilder sessionFactory = new LocalSessionFactoryBuilder(restDataSource);

        sessionFactory.scanPackages("com.soaesps.schedulerservice.domain");
        sessionFactory.setProperties(hibernateProperties());

        return sessionFactory.buildSessionFactory();
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();

        props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.setProperty("hibernate.show_sql", "true");
        //props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        props.setProperty("hibernate.globally_quoted_identifiers", "true");

        return props;
    }
}