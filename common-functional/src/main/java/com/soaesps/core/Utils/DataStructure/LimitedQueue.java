package com.soaesps.core.Utils.DataStructure;

import com.soaesps.core.Utils.DAO.Persister;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LimitedQueue<T extends Serializable> extends BaseQueue<T> {
    static public long DEFAULT_LIMIT = 100l;

    private Persister persister;

    public LimitedQueue() {
        super();
    }

    public Persister getPersister() {
        return persister;
    }

    public void setPersister(final Persister persister) {
        this.persister = persister;
    }

    @Override
    public boolean push(final T message) {
        if (getSize() >= DEFAULT_LIMIT) {
            if (persister != null) {
                return persister.persistMsg(message, ZonedDateTime.now(ZoneId.of("UTC")));
            }

            return false;
        }

        return super.push(message);
    }
}