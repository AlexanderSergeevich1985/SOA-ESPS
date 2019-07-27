package com.soaesps.core.Utils.DAO;

import java.time.ZonedDateTime;

public interface Persister<T> {
    boolean persistMsg(final T message, final ZonedDateTime timestamp);
}