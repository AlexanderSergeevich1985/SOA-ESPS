package com.soaesps.core.stateflow;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.zip.ZipOutputStream;

abstract public class StateFlow<T extends Serializable> {
    public ByteArrayOutputStream arXivState(final byte[] prevStateArXiv, final State<T> current) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try(final ZipOutputStream zipStream = new ZipOutputStream(outputStream)) {
            zipStream.write(prevStateArXiv);
            addToZipStream(current, zipStream);
        }
        catch(final Exception ex) {
        }

        return outputStream;
    }

    abstract void addToZipStream(final State<T> current, final ZipOutputStream zipStream);
}