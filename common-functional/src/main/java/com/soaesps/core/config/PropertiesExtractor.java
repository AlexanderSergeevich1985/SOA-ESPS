package com.soaesps.core.config;

import org.springframework.core.env.Environment;

public interface PropertiesExtractor {
    int DEFAULT_CONNECTION_POOL_SIZE = 4;

    static HibernateExtractor getHibernateExtractor(final Environment env) {
        return new HibernateExtractor(env);
    }

    class HibernateExtractor implements PropertiesExtractor {
        public final String HIBERNATE_PREFIX = "spring.hibernate.";

        public final String CONNECTION_PREFIX = "connection.";

        private final String DRIVER_CLASS_NAME = "driverClassName";

        private final String URL = "url";

        private final String USER_NAME = "username";

        private final String PASSWORD = "password";

        private final String MAX_POOL_SIZE = "maxPoolSize";

        private final String DIALECT = "dialect";

        private Environment env;

        private StringBuilder builder;

        public HibernateExtractor(final Environment env) {
            this.env = env;
        }

        public void connection() {
            this.builder = new StringBuilder(HIBERNATE_PREFIX);
            this.builder.append(CONNECTION_PREFIX);
        }

        public String driverClassName() {
            return env.getProperty(HIBERNATE_PREFIX.concat(CONNECTION_PREFIX).concat(DRIVER_CLASS_NAME));
        }

        public String url() {
            return env.getProperty(HIBERNATE_PREFIX.concat(CONNECTION_PREFIX).concat(URL));
        }

        public String username() {
            return env.getProperty(HIBERNATE_PREFIX.concat(CONNECTION_PREFIX).concat(USER_NAME));
        }

        public String password() {
            return env.getProperty(HIBERNATE_PREFIX.concat(CONNECTION_PREFIX).concat(PASSWORD));
        }

        public int maxPoolSize() {
            final Integer maxPoolSize = Integer.valueOf(env.getProperty(HIBERNATE_PREFIX.concat(CONNECTION_PREFIX).concat(MAX_POOL_SIZE)));

            return (maxPoolSize != null && maxPoolSize > 0) ? maxPoolSize : DEFAULT_CONNECTION_POOL_SIZE;
        }

        public String dialect() {
            return env.getProperty(HIBERNATE_PREFIX.concat(CONNECTION_PREFIX).concat(DIALECT));
        }
    }
}