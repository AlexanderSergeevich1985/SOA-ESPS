package com.soaesps.core.Utils.fs;

import com.soaesps.core.Utils.CryptoHelper;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

import net.lingala.zip4j.io.outputstream.ZipOutputStream;

public class ZipCreator {
    public static Integer DEFAULT_READ_BUF_SIZE = 1000;

    private ZipParameters zp;

    public ZipCreator() {}

    public OutputStream zipCipher(final Map<String, Object> entries, final String password, final ZipParameters zp) throws IOException {
        OutputStream os = new ByteArrayOutputStream();
        try (final ZipOutputStream zos = new ZipOutputStream(os, password.toCharArray())) {
            entries.entrySet().stream().forEach(e -> {
                try {
                    zp.setFileNameInZip(e.getKey());
                    zos.putNextEntry(zp);
                    zos.write(serialize(e.getValue()));
                    zos.closeEntry();
                } catch (final IOException ex) {
                }
            });
        }

        return os;
    }

    public Map<String, Object> unzipDecipher(final InputStream is, final String password, final ZipParameters zp) throws Exception {
        final Map<String, Object> result = new IdentityHashMap<>();
        final byte[] buffer = new byte[DEFAULT_READ_BUF_SIZE];
        try (final ZipInputStream zis = new ZipInputStream(is, password.toCharArray())) {
            LocalFileHeader lfh = null;
            while ((lfh = zis.getNextEntry()) != null) {
                final String fileName = lfh.getFileName();
                final ByteBuffer bbuf = ByteBuffer.allocate(zis.available());
                while (zis.read(buffer) != -1) {
                    bbuf.put(bbuf);
                }
                result.put(fileName, CryptoHelper.deserializeObject(bbuf.array()));
            }
        }

        return result;
    }

    public static byte[] serialize(final Object obj) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);

        return out.toByteArray();
    }

    protected ZipCreator(final ZipParameters zp) {
        this.zp = zp;
    }

    protected static void setDefaults(final ZipParameters zipParams) {
        zipParams.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParams.setCompressionLevel(CompressionLevel.MAXIMUM);
        zipParams.setEncryptFiles(true);
        zipParams.setEncryptionMethod(EncryptionMethod.AES);
        zipParams.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
    }

    public ZipParameters getZipParameters() {
        return zp;
    }

    public void setZipParameters(final ZipParameters zp) {
        this.zp = zp;
    }

    public static class Builder {
        private ZipParameters zipParams = new ZipParameters();

        Builder() {}

        public static Builder builder() {
            return new Builder();
        }

        public Builder defaults() {
            setDefaults(zipParams);

            return this;
        }

        public Builder compressions(final CompressionMethod method, final CompressionLevel level) {
            zipParams.setCompressionMethod(method);
            zipParams.setCompressionLevel(level);

            return this;
        }

        public Builder encryption(final EncryptionMethod method) {
            zipParams.setEncryptionMethod(method);
            zipParams.setEncryptFiles(true);

            return this;
        }

        public Builder aesKeyStrength(final AesKeyStrength strength) {
            zipParams.setAesKeyStrength(strength);

            return this;
        }

        public Builder setFileNameZip(final String fileNameZip) {
            zipParams.setFileNameInZip(fileNameZip);

            return this;
        }

        public Builder aesVersion(final AesVersion aesVersion) {
            zipParams.setAesVersion(aesVersion);

            return this;
        }

        public ZipCreator build() {
            return new ZipCreator(zipParams);
        }
    }
}