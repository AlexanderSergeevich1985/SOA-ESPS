package com.soaesps.core.Utils.fs;

import com.soaesps.core.service.files.FileService;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.soaesps.core.service.files.FileService.MAX_CHANGE_BUFFER_SIZE;
import static com.soaesps.core.service.files.FileService.checkDir;
import static com.soaesps.core.service.files.FileService.checkFile;

public class ZipUtils {
    public static void zipToFile(String zipName, String fileName, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipName); ZipOutputStream zos = new ZipOutputStream(fos)) {
            ZipEntry ze = new ZipEntry(fileName);
            zos.putNextEntry(ze);
            zos.write(data);
            zos.closeEntry();
        }
    }

    public static void dirToZipFile(String zipName, String dirName) throws IOException {
        if (dirName == null || dirName.isEmpty()) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(zipName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            File file = new File(dirName);
            dirToZipFile(file, dirName, zos);
            zos.finish();
            fos.flush();
        }
    }

    public static void dirToZipFile(File file, String fileName, ZipOutputStream zos) throws IOException {
        if (file.isHidden()) {
            return;
        }
        if (file.isDirectory()) {
            if (fileName.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(fileName));
                zos.closeEntry();
            } else {
                zos.putNextEntry(new ZipEntry(fileName + "/"));
                zos.closeEntry();
            }
            File[] children = file.listFiles();
            for (File childFile : children) {
                dirToZipFile(childFile, fileName + "/" + childFile.getName(), zos);
            }

            return;
        }
        addZipEntry(zos, file, fileName);
    }

    public static void filesToZipFile(String zipName, String... fileNames) throws IOException {
        if (fileNames == null || fileNames.length == 0) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(zipName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (String fileName: fileNames) {
                File file = new File(fileName);
                addZipEntry(zos, file);
            }
        }
    }

    public static void addZipEntry(ZipOutputStream zos, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            addZipEntry(fis, zos, file.getName());
        }
    }

    public static void addZipEntry(ZipOutputStream zos, File file, String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            addZipEntry(fis, zos, fileName);
        }
    }

    public static void addZipEntry(FileInputStream fis, ZipOutputStream zos,
                                   String fileName) throws IOException {
        ZipEntry ze = new ZipEntry(fileName);
        zos.putNextEntry(ze);
        byte[] bytes = new byte[MAX_CHANGE_BUFFER_SIZE];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
    }

    public static void unzip(String zipName, String destDirName) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipName))) {
            ZipEntry zipEntry = null;
            byte[] buffer = new byte[MAX_CHANGE_BUFFER_SIZE];
            File destDir = checkDir(destDirName);
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = checkFile(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    checkDir(newFile);
                } else {
                    checkDir(newFile.getParentFile());
                    FileService.saveFile(newFile, zis, buffer);
                }
            }
            zis.closeEntry();
        }
    }
}