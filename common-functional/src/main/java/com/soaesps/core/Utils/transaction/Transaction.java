package com.soaesps.core.Utils.transaction;


import java.time.ZonedDateTime;

public class Transaction<T extends Number> {
    private TransDesc.Type type;

    private T value;

    private ZonedDateTime zdt;

    private TransDesc desc;

    public TransDesc.Type getType() {
        return type;
    }

    public void setType(final TransDesc.Type type) {
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public ZonedDateTime getZdt() {
        return zdt;
    }

    public void setZdt(final ZonedDateTime zdt) {
        this.zdt = zdt;
    }

    public TransDesc getDesc() {
        return desc;
    }

    public void setDesc(final TransDesc desc) {
        this.desc = desc;
    }
}