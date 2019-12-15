package com.soaesps.core.service.files;

import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Service
public interface FileServiceI {
    boolean createDirIfNotExist(@Nonnull final String dir) throws IOException;

    boolean createFileIfNotExist(@Nonnull final String file, @Nonnull final InputStream in) throws IOException;
}