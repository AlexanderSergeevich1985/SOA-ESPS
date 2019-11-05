package com.soaesps.core.Utils.transaction;

public interface TransPersister<T extends Number> {
    void save(final TransDesc<T> transaction);
}