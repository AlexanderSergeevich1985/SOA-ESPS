package com.soaesps.core.DataModels;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.soaesps.core.Utils.convertor.hibernate.TimestampConverter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.ZonedDateTime;

/**
 * Common 'id' part of all entities.
 */
@MappedSuperclass
public abstract class BaseEntity {
    public static final String ID_PROPERTY = "id";
    public static final String CREATION_TIME = "creationTime";
    public static final String MODIFICATION_TIME = "modificationTime";

    /*@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;*/
    @Id
    @GenericGenerator(name="kaugen", strategy="increment")
    @GeneratedValue(generator="kaugen")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "creation_time", nullable = false, updatable = false)
    @Convert(converter = TimestampConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
    private ZonedDateTime creationTime;

    @JsonIgnore
    @Column(name = "modification_time")
    @Convert(converter = TimestampConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
    private ZonedDateTime modificationTime;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final ZonedDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public ZonedDateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(final ZonedDateTime modificationTime) {
        this.modificationTime = modificationTime;
    }
}
