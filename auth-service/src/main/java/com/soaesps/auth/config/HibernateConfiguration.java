package com.soaesps.auth.config;

import com.soaesps.core.config.BaseHibernateConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

/**
 * JPA/Hibernate configuration for the profile-service.
 * In Spring Boot 3, native Hibernate SessionFactory support was removed.
 * All database access should go through JPA EntityManager.
 */

@Configuration
// Unified scanning package mapping for domain entities and modern data models
@EntityScan(basePackages = {"com.soaesps.auth"})
// Configures automated Spring Data JPA repository layer mapping orchestration
@EnableJpaRepositories(basePackages = {"com.soaesps.auth.repository"})
public class HibernateConfiguration extends BaseHibernateConfiguration {

    /**
     * Configures the container-managed JPA EntityManagerFactory bean instance.
     * Inherits underlying database connection logic and populates vendors properties.
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource restDataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(restDataSource);

        // Dynamically scans all nested packages under payments module context boundary
        // Scan all required packages for JPA entities
        em.setPackagesToScan(
                "com.soaesps.core.DataModels.security",
                "com.soaesps.core.DataModels.device",
                "com.soaesps.core.DataModels.user"
                //"com.soaesps.payments.DataModels.Transactions"
        );

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        // Reuses core inherited helper factory method mapping properties from @Value hooks
        em.setJpaProperties(baseHibernateProperties());

        return em;
    }
}