package com.soaesps.core.service.files;

import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public interface FileServiceI {
    Integer MAX_FILE_NAME_LENGTH = 128;

    static String validateFileName(@Nonnull final String origStr) {
        final String clearedStr = origStr.replaceAll("[\\\"#@,;:<>*^|?\\\\/]", "_");
        if (clearedStr.length() > MAX_FILE_NAME_LENGTH) {
            final String csStr = clearedStr.substring(0, MAX_FILE_NAME_LENGTH);
            final int liOfDot = clearedStr.lastIndexOf('.');
            if (liOfDot == MAX_FILE_NAME_LENGTH - 1) {
                return Arrays.asList(csStr.split(".")).stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining("."));
            }

            return csStr;
        }

        return clearedStr;
    }

    boolean createDirIfNotExist(@Nonnull final String dir) throws IOException;

    boolean createFileIfNotExist(@Nonnull final String file, @Nonnull final InputStream in) throws IOException;

    ByteArrayOutputStream loadFileInMemory(@Nonnull final String file) throws Exception;
}