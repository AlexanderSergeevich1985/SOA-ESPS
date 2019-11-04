package com.soaesps.core.Utils.fs;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

import net.lingala.zip4j.io.outputstream.ZipOutputStream;

public class ZipCreator {
    private ZipParameters zp;

    public ZipCreator() {}

    public OutputStream cipher(final Map<String, Object> entries, final String password, final ZipParameters zp) throws IOException {
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