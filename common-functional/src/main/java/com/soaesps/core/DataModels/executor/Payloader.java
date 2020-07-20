package com.soaesps.core.DataModels.executor;

public interface Payloader {
    Boolean load(final Payload payload, final ExecutorNode en);
}