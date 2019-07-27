package com.soaesps.core.Utils.DataStructure;

import com.soaesps.core.Utils.DAO.Persister;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LimitedQueue<T extends Serializable> extends BaseQueue<T> {
    public Integer DEFAULT_LIMIT = 100;

    private Persister persister;

    public LimitedQueue() {
        super();
    }

    @Override
    public boolean push(final T message) {
        if (getSize() >= DEFAULT_LIMIT) {
            if (persister != null) {
                persister.persistMsg(message, ZonedDateTime.now(ZoneId.of("UTC")));

                return true;
            }

            return false;
        }

        return super.push(message);
    }
}