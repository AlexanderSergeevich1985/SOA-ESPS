package com.soaesps.schedulerservice.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
public abstract class BaseReport {
    @Id
    @GenericGenerator(name="kaugen" , strategy="increment")
    @GeneratedValue(generator="kaugen")
    @Column(name = "id", nullable = false)
    private Integer id;

    /*@OneToMany
    @JoinColumn(name = "event_id", nullable = false)
    private Set<BaseEvent> events = new HashSet<>();*/

    public BaseReport() {}

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    /*public Set<BaseEvent> getEvents() {
        return events;
    }

    public void setEvents(final Set<BaseEvent> events) {
        this.events = events;
    }*/
}