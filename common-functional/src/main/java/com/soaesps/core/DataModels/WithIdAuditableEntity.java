package com.soaesps.core.DataModels;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class WithIdAuditableEntity extends AuditableEntity {
    @Id
    private Long id;

    // Getters and Setters remain unchanged
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}