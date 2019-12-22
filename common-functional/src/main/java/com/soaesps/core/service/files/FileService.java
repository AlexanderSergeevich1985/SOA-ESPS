package com.soaesps.core.service.files;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileService implements FileServiceI {
    static private final Logger logger;

    static public final Integer MAX_LOADED_FILE_SIZE = 1024;

    static {
        logger = Logger.getLogger(FileService.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Override
    public boolean createDirIfNotExist(@Nonnull final String dir) {
        final Path path = Paths.get(dir);
        if (!Files.exists(path)) {
            try {
                if (Files.createDirectories(path) != null) {
                    return true;
                }
            } catch (final IOException ioex) {
                logger.log(Level.WARNING, "[FileService/createDirIfNotExist]: {}", ioex);
            }
        }

        return false;
    }

    @Override
    public ByteArrayOutputStream loadFileInMemory(@Nonnull final String file) throws Exception {
        final Path path = Paths.get(file);
        if (!Files.exists(path)) {
            throw new FileNotFoundException();
        }
        Long fileSize = Files.size(path);
        if (fileSize > MAX_LOADED_FILE_SIZE) {
            throw new FileUploadBase.FileSizeLimitExceededException("File: ".concat(file), fileSize, MAX_LOADED_FILE_SIZE);
        }
        ByteArrayOutputStream baos = null;
        try (final InputStream in = Files.newInputStream(path)) {
            baos = new ByteArrayOutputStream(fileSize.intValue());
            baos.write(IOUtils.toByteArray(in));
        }

        return baos;
    }
}