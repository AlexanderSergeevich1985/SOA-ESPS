package com.soaesps.core.component.transaction;

import com.soaesps.core.DataModels.BaseEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public class TransactionDesc<T> extends BaseEntity {
    @Column(name = "value", nullable = false)
    private T value;

    @Transient
    private boolean canBeModified;

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public boolean isCanBeModified() {
        return canBeModified;
    }

    public void setCanBeModified(final boolean canBeModified) {
        this.canBeModified = canBeModified;
    }
}