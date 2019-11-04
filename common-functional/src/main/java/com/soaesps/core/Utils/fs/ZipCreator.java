package com.soaesps.core.Utils.fs;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.*;

public class ZipCreator {
    private ZipParameters zp;

    public ZipCreator() {}

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