package com.soaesps.profile.config;

import com.soaesps.core.config.BaseHibernateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * JPA/Hibernate configuration for the profile-service.
 * In Spring Boot 3, native Hibernate SessionFactory support was removed.
 * All database access should go through JPA EntityManager.
 */
@Configuration
@Import({BaseHibernateConfiguration.class})
@EntityScan({"com.soaesps.core.DataModels"})
@EnableJpaRepositories(basePackages = {"com.soaesps.profile.repository"})
@EnableTransactionManagement
public class HibernateConfiguration {

    private static final Logger log = LoggerFactory.getLogger(HibernateConfiguration.class);

    private final Environment env;

    public HibernateConfiguration(Environment env) {
        this.env = env;
    }

    /**
     * JPA EntityManagerFactory configuration.
     * This is the standard way to configure Hibernate in Spring Boot 3.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        // Scan all required packages for JPA entities
        em.setPackagesToScan(
                "com.soaesps.core.DataModels",
                "com.soaesps.core.DataModels.device",
                "com.soaesps.core.DataModels.user",
                "com.soaesps.payments.DataModels.Transactions"
        );

        final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    private Properties hibernateProperties() {
        final Properties props = new Properties();

        props.setProperty("hibernate.show_sql", "true");
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.globally_quoted_identifiers", "true");

        // DDL auto-generation (optional, for development only)
        // props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto", "validate"));

        return props;
    }
}