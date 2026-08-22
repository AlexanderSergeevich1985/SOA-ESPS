package com.soaesps.payments.config;

import com.soaesps.core.service.archive.ArchiveServiceI;
import com.soaesps.core.service.archive.ArchiverService;
import com.soaesps.core.service.files.FileService;
import com.soaesps.core.service.files.FileServiceI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ArchiveServiceI archiveService() {
        return new ArchiverService();
    }

    @Bean
    public FileServiceI fileService() {
        return new FileService();
    }
}