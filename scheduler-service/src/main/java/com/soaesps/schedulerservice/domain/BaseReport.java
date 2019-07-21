package com.soaesps.schedulerservice.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseReport {
    @Id
    @GenericGenerator(name="kaugen" , strategy="increment")
    @GeneratedValue(generator="kaugen")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "start_period", nullable = false)
    private ZonedDateTime startPeriod;

    @Column(name = "end_period", nullable = false)
    private ZonedDateTime endPeriod;

    public BaseReport() {}

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public ZonedDateTime getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(final ZonedDateTime startPeriod) {
        this.startPeriod = startPeriod;
    }

    public ZonedDateTime getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(final ZonedDateTime endPeriod) {
        this.endPeriod = endPeriod;
    }
}