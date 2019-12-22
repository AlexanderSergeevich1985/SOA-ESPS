package com.soaesps.core.service.archive;

import com.soaesps.core.Utils.DateTimeHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public interface ArchiveServiceI {
    default String generateName(final String ownerName) {
        return ownerName.concat(".").concat(DateTimeHelper.zdtToString(ZonedDateTime.now(), null));
    }

    boolean archiveOne(@Nonnull final String archiveName, @Nonnull final InputStream data) throws IOException;

    boolean unzipOne(@Nonnull final String unzipDir, @Nonnull final ZipInputStream zis) throws IOException;

    void addToArchive(@Nonnull final String fileName, @Nonnull final InputStream in, @Nonnull final ZipOutputStream zos) throws IOException;

    boolean archiveFiles(@Nonnull final String archiveName, @Nonnull final Map<String, InputStream> files) throws IOException;
}