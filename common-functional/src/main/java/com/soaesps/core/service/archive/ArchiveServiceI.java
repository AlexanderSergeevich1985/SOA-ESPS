package com.soaesps.core.service.archive;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ArchiveServiceI {
    boolean archiveOne(@Nonnull final String archiveName, @Nonnull final InputStream data) throws IOException;

    OutputStream unzipOne(@Nonnull final String archiveName) throws IOException;

    boolean addToArchive(@Nonnull final String fileName, @Nonnull final InputStream data) throws IOException;
}