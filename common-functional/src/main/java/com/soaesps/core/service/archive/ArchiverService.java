package com.soaesps.core.service.archive;

import com.soaesps.core.service.files.FileService;
import com.soaesps.core.service.files.FileServiceI;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiverService implements ArchiveServiceI {
    @Autowired
    private FileService fileService;

    public boolean archiveOne(@Nonnull final String archiveName, @Nonnull InputStream in) throws IOException {
        final Path path = Paths.get(archiveName);
        if (Files.exists(path)) {
            return false;
        }
        try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
            addToZipOutStream(archiveName, in, zos);
        }

        return false;
    }

    protected void addToZipOutStream(@Nonnull final String fileName, @Nonnull final InputStream in, @Nonnull final ZipOutputStream zos) throws IOException {
        final ZipEntry entry = new ZipEntry(FileServiceI.validateFileName(fileName));
        zos.putNextEntry(entry);
        IOUtils.copy(in, zos);
    }

    @Override
    public boolean archiveFiles(@Nonnull final String archiveName, @Nonnull final Map<String, InputStream> files) throws IOException {
        final Path path = Paths.get(archiveName);
        if (Files.exists(path)) {
            return false;
        }

        try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
            for (final Map.Entry<String, InputStream> entry: files.entrySet()) {
                addToZipOutStream(entry.getKey(), entry.getValue(), zos);
            }
        }

        return false;
    }

    @Override
    public boolean unzipOne(@Nonnull final String unzipDir, @Nonnull final ZipInputStream zis) throws IOException {
        final Path path = Paths.get(unzipDir);
        if (!Files.exists(path) || Files.createDirectories(path) == null) {
            return false;
        }
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            final Path entryPath = path.resolve(entry.getName());
            if (entry.isDirectory()) {
                Files.createDirectories(entryPath);
            } else {
                if (!Files.exists(entryPath.getParent()) || Files.createDirectories(path.getParent()) == null) {
                    continue;
                }
                FileServiceI.createFileIfNotExist(entryPath.toString(), zis);
            }
            zis.closeEntry();
        }

        return true;
    }

    @Override
    public void addToArchive(@Nonnull final String fileName, @Nonnull final InputStream in,
                                @Nonnull final ZipOutputStream zos) throws IOException {
        final String validName = FileServiceI.validateFileName(fileName);
        final ZipEntry entry = new ZipEntry(fileName);
        zos.putNextEntry(entry);
        IOUtils.copy(in, zos);
    }

    @Override
    public boolean addToArchive(@Nonnull final String fileName, @Nonnull final String archiveName, @Nonnull final InputStream in) throws IOException {
        final Path path = Paths.get(archiveName);
        if (!Files.exists(path)) {
            return false;
        }

        try (final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
            addToArchive(fileName, archiveName, in);
        }

        return false;
    }
}