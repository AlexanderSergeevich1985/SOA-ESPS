package com.soaesps.core.stateflow;

import java.io.Serializable;
import java.util.zip.ZipOutputStream;

public interface StateFlowI<T extends Serializable> {
    T getProcessState();

    void setProcessState(final T state);

    String calcStateHash(final T state);

    ZipOutputStream arXivState(final byte[] previousStateArxiv, final T currentState, final String pshash);
}