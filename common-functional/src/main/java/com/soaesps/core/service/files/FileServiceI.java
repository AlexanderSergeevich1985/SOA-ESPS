package com.soaesps.core.service.files;

import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public interface FileServiceI {
    boolean createDirIfNotExist(@Nonnull final String dir) throws IOException;

    boolean createFileIfNotExist(@Nonnull final String file, @Nonnull final InputStream in) throws IOException;

    ByteArrayOutputStream loadFileInMemory(@Nonnull final String file) throws Exception;
}