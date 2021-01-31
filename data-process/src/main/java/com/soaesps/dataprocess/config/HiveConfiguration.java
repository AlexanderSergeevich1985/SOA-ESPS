package com.soaesps.dataprocess.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.fs.HdfsResourceLoader;
import org.springframework.data.hadoop.hive.HiveClient;
import org.springframework.data.hadoop.hive.HiveTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
public class HiveConfiguration {
    @Value("${hdfs.uri}")
    private URI hdfsUri;

    @Value("${hadoop.user}")
    private String user;

    @Value("${hive.uri}")
    private String hiveUri;

    @Bean
    public org.apache.hadoop.conf.Configuration hadoopConfiguration() {
        return new org.apache.hadoop.conf.Configuration();
    }

    @Bean
    public HdfsResourceLoader hdfsResourceLoader(org.apache.hadoop.conf.Configuration hadoopConfiguration) {
        return new HdfsResourceLoader(hadoopConfiguration, hdfsUri, user);
    }

    @Bean
    public HiveTemplate hiveTemplate() {
        return new HiveTemplate(() -> {
            final SimpleDriverDataSource ds = new SimpleDriverDataSource(new HiveDriver(), hiveUri);

            return new HiveClient(ds);
        });
    }
}