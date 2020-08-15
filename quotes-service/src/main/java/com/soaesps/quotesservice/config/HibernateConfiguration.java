package com.soaesps.quotesservice.config;

import com.soaesps.core.config.BaseHibernateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@Import({BaseHibernateConfiguration.class})
@EntityScan({"com.soaesps.core.DataModels"})
//@EnableJpaRepositories(basePackages = {"com.soaesps.auth.repository"})
@EnableTransactionManagement
public class HibernateConfiguration {
    private static Logger log = LoggerFactory.getLogger(HibernateConfiguration.class);

    @Autowired
    private Environment env;

    private Properties hibernateProperties() {
        final Properties props = new Properties();
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        props.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
        props.setProperty("hibernate.show_sql", "true");
        //props.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        props.setProperty("hibernate.globally_quoted_identifiers", "true");

        return props;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource restDataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(restDataSource);
        em.setPackagesToScan("com.soaesps.core.DataModels.security");

        final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }
}