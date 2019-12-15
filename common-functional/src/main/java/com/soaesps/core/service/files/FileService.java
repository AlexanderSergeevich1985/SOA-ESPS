package com.soaesps.core.service.files;

import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileService implements FileServiceI {
    static private final Logger logger;

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
    public boolean createFileIfNotExist(@Nonnull final String file, @Nonnull final InputStream in) throws IOException {
        final Path path = createFileIfNotExist(file);
        if (path != null) {
            try (final OutputStream out = Files.newOutputStream(path)) {
                IOUtils.copy(in, out);

                return true;
            }
        }

        return false;
    }

    public Path createFileIfNotExist(@Nonnull final String file) throws IOException {
        final Path path = Paths.get(file);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                return Files.createFile(path);
            }
        } catch (final IOException ioex) {
            logger.log(Level.WARNING, "[FileService/createDirIfNotExist]: {}", ioex);
        }

        return null;
    }
}