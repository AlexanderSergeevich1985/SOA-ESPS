package com.soaesps.core.Utils.transaction;

public interface TransPersister<ID extends Comparable<ID>, T> {
    Transaction<T> load(final ID transId); //load record from database

    Transaction<T> save(final Transaction<T> transaction); //save record to database
}