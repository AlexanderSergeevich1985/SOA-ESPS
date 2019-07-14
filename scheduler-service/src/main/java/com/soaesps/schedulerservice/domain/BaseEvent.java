package com.soaesps.schedulerservice.domain;

import javax.persistence.*;
//import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timeStamp;

    @Column(name = "description")
    private String description;

    public BaseEvent() {}

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @NotNull
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(@NotNull final LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(/*@NotBlank*/ final String description) {
        this.description = description;
    }
}