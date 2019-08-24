package com.soaesps.core.DataModels.executor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ExecutorNodeDesc {
    @Column(name = "url")
    private String url;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "descriptor")
    private String descriptor;

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(final String descriptor) {
        this.descriptor = descriptor;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public int hashCode() {
        return (descriptor != null || ipAddress != null) ? descriptor.hashCode()/(1 + ipAddress.hashCode()) + ipAddress.hashCode()/(1 + descriptor.hashCode()) : this.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof ExecutorNodeDesc)) {
            return false;
        }
        final ExecutorNodeDesc en = (ExecutorNodeDesc) other;
        if (ipAddress == null || ipAddress.isEmpty() || descriptor == null || descriptor.isEmpty()) {
            return false;
        }
        if (en.getIpAddress() == null || en.getIpAddress().isEmpty() || en.getDescriptor() == null || en.getDescriptor().isEmpty()) {
            return false;
        }
        if (!ipAddress.equals(en.getIpAddress()) || !descriptor.equals(en.getDescriptor())) {
            return false;
        }

        return true;
    }
}