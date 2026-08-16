package com.soaesps.core.DataModels.executor;

public interface Payloader {
    boolean load(final Payload payload, final ExecutorNode en);
}