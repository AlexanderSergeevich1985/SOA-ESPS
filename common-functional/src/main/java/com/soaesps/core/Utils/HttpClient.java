package com.soaesps.core.Utils;

public interface HttpClient {
    <T> T send(final String url, final String msg);
}
