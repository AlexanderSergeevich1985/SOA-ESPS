package com.soaesps.core.DataModels;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.ZonedDateTime;

import com.soaesps.core.Utils.convertor.hibernate.TimestampConverter;

@MappedSuperclass
public abstract class AuditableEntity implements Serializable {

    @Column(name = "creation_time", nullable = false, updatable = false)
    @Convert(converter = TimestampConverter.class) // Использует ваш существующий конвертер
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime creationTime;

    @Column(name = "modification_time")
    @Convert(converter = TimestampConverter.class) // Использует ваш существующий конвертер
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime modificationTime;

    /**
     * Automatically sets creation and modification timestamps before inserting into database.
     */
    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        this.creationTime = now;
        this.modificationTime = now;
    }

    /**
     * Automatically updates modification timestamp before updating database record.
     */
    @PreUpdate
    protected void onUpdate() {
        this.modificationTime = ZonedDateTime.now();
    }

    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(ZonedDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public ZonedDateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(ZonedDateTime modificationTime) {
        this.modificationTime = modificationTime;
    }
}