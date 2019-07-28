package com.soaesps.schedulerservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "SOA_ESPS.SCHEDULER_TASK")
public class SchedulerTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonProperty("job_name")
    @Column(name = "class_name")
    private String className;

    @JsonProperty("method_name")
    @Column(name = "handler_name")
    private String handlerName;

    @JsonProperty("timer")
    @Column(name = "timer")
    private String timer;

    protected SchedulerTask() {}

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }
}