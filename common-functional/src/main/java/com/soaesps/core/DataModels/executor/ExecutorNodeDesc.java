package com.soaesps.core.DataModels.executor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ExecutorNodeDesc {
    @Column(name = "url")
    private String url;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public int hashCode() {
        return (description != null || ipAddress != null) ? description.hashCode()/(1 + ipAddress.hashCode()) + ipAddress.hashCode()/(1 + description.hashCode()) : this.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof ExecutorNodeDesc)) {
            return false;
        }
        final ExecutorNodeDesc en = (ExecutorNodeDesc) other;
        if (ipAddress == null || ipAddress.isEmpty() || description == null || description.isEmpty()) {
            return false;
        }
        if (en.getIpAddress() == null || en.getIpAddress().isEmpty() || en.getDescription() == null || en.getDescription().isEmpty()) {
            return false;
        }
        if (!ipAddress.equals(en.getIpAddress()) || !description.equals(en.getDescription())) {
            return false;
        }

        return true;
    }
}