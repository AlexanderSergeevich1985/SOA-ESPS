package com.soaesps.config;

import com.soaesps.Utils.DataStructure.InMemoryCacheTest;
import com.soaesps.core.Utils.DataStructure.CacheI;
import com.soaesps.core.Utils.DataStructure.LFUInMemoryCache;
import com.soaesps.core.Utils.DataStructure.LRUInMemoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryCacheConfiguration {
    @Bean(name = "lruCache")
    public CacheI<Long, InMemoryCacheTest.TestObject> lruCache() {
        return new LRUInMemoryCache<>();
    }

    @Bean(name = "lfuCache")
    public CacheI<Long, InMemoryCacheTest.TestObject> lfuCache() {
        return new LFUInMemoryCache<>();
    }
}