package com.soaesps.core.component.router;

public interface TransportI {
    <T> T sendAndGet(final String url, final Object msg);
}