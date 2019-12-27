package com.soaesps.core.service.archive;

import com.soaesps.core.Utils.DateTimeHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public interface ArchiveServiceI {
    default String generateName(final String ownerName) {
        return ownerName.concat(".").concat(DateTimeHelper.zdtToString(ZonedDateTime.now(), null));
    }

    static boolean mergeFileWithArchive(@Nonnull final String fileName, @Nonnull final String archiveName,
                                           @Nonnull final String data) throws IOException {
        final Path archivePath = Paths.get(archiveName);
        if (!Files.exists(archivePath)) {
            return false;
        }
        final Path path = Paths.get(fileName);
        if (!Files.exists(path.getParent())) {
            return false;
        }
        try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
            try (InputStream stris = IOUtils.toInputStream(data, "UTF-8");
                 final InputStream ais = FileUtils.openInputStream(archivePath.toFile())) {

            }
        }

        return true;
    }

    boolean archiveOne(@Nonnull final String archiveName, @Nonnull final InputStream data) throws IOException;

    boolean unzipOne(@Nonnull final String unzipDir, @Nonnull final ZipInputStream zis) throws IOException;

    void addToArchive(@Nonnull final String fileName, @Nonnull final InputStream in, @Nonnull final ZipOutputStream zos) throws IOException;

    boolean addToArchive(@Nonnull final String fileName, @Nonnull final String archiveName, @Nonnull final InputStream in) throws IOException;

    boolean archiveFiles(@Nonnull final String archiveName, @Nonnull final Map<String, InputStream> files) throws IOException;
}