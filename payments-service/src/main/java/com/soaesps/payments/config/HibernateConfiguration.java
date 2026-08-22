package com.soaesps.payments.config;

import com.soaesps.core.config.BaseHibernateConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

@Configuration
// Unified scanning package mapping for domain entities and modern data models
@EntityScan(basePackages = {"com.soaesps.payments"})
// Configures automated Spring Data JPA repository layer mapping orchestration
@EnableJpaRepositories(basePackages = {"com.soaesps.payments.repository"})
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
        em.setPackagesToScan("com.soaesps.payments");

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        // Reuses core inherited helper factory method mapping properties from @Value hooks
        em.setJpaProperties(baseHibernateProperties());

        return em;
    }
}