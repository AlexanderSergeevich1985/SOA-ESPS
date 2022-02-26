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

    static public final Integer MAX_CHANGE_BUFFER_SIZE = 1024;

    static public final Integer MAX_LOADED_FILE_SIZE = 1024 * 1024;

    static {
        logger = Logger.getLogger(FileService.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Override
    public boolean createDirIfNotExist(@Nonnull final String dir) {
        File file = null;
        try {
            file = checkDir(dir);
        } catch (final IOException ioex) {
            logger.log(Level.WARNING, "[FileService/createDirIfNotExist]: {}", ioex);
        }

        return file != null;
    }

    public static File checkDir(String dirName) throws IOException {
        final Path path = Paths.get(dirName);
        if (!Files.exists(path)) {
            return Files.createDirectory(path).toFile();
        }

        return path.toFile();
    }

    public static void checkDir(File dir) throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
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

    public void saveFile(String fileName, byte[] value) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(value);
        }
    }

    public void saveFile(String fileName, String value) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             DataOutputStream os = new DataOutputStream(new BufferedOutputStream(fos))) {
            os.writeUTF(value);
        }
    }

    public static void checkFile(File file, boolean isNeedNew) throws IOException {
        if (file.exists()) {
            if (isNeedNew) {
                throw new IOException("File already with name = " + file.getName() + " exist.");
            }
            if (file.isDirectory()) {
                throw new IOException("File with name = " + file.getName() + " is directory.");
            }
        }
    }

    public static File checkFile(String fileName, boolean isNeedNew) throws IOException {
        File file = new File(fileName);
        checkFile(file, isNeedNew);

        return file;
    }

    public static File checkFile(File destDir, String fileName) throws IOException {
        File destFile = new File(destDir, fileName);
        checkFile(destFile, true);

        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + fileName);
        }

        return destFile;
    }

    public void saveFile(String fileName, InputStream is) throws IOException {
        byte[] buffer = new byte[MAX_CHANGE_BUFFER_SIZE];
        File file = checkFile(fileName, true);
        saveFile(file, is, buffer);
    }

    public void saveFile(String fileName, InputStream is, byte[] buffer) throws IOException {
        File file = checkFile(fileName, true);
        saveFile(file, is, buffer);
    }

    public static void saveFile(File file, InputStream is, byte[] buffer) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }
}