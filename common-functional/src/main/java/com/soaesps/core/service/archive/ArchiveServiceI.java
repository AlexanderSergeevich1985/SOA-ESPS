package com.soaesps.core.service.archive;

import com.soaesps.core.Utils.DateTimeHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;

public interface ArchiveServiceI {
    default String generateName(final String ownerName) {
        return ownerName.concat(".").concat(DateTimeHelper.zdtToString(ZonedDateTime.now(), null));
    }

    boolean archiveOne(@Nonnull final String archiveName, @Nonnull final InputStream data) throws IOException;

    OutputStream unzipOne(@Nonnull final String archiveName) throws IOException;

    boolean addToArchive(@Nonnull final String fileName, @Nonnull final InputStream data) throws IOException;

    boolean archiveFiles(@Nonnull final String fileName, @Nonnull final InputStream... data) throws IOException;
}