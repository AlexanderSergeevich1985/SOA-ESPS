package com.soaesps.core.Utils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtils {
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    public static String getClientIpAddress(final HttpServletRequest request) {
        for (final String header: IP_HEADER_CANDIDATES) {
            final String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    public static String getUserAgent(final HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public enum ClientOS {
        WINDOWS,
        MAC,
        LINUX,
        ANDROID,
        IPHONE,
        UNKNOWN;

        public static ClientOS valueOf(final HttpServletRequest request) {
            final String userAgent = getUserAgent(request);
            final ClientOS result;

            if (userAgent == null || userAgent.isEmpty()) {
                result = UNKNOWN;
            } else if (userAgent.toLowerCase().contains("windows")) {
                result = WINDOWS;
            } else if (userAgent.toLowerCase().contains("mac")) {
                result = MAC;
            } else if (userAgent.toLowerCase().contains("x11")) {
                result = LINUX;
            } else if (userAgent.toLowerCase().contains("android")) {
                result = ANDROID;
            } else if (userAgent.toLowerCase().contains("iphone")) {
                result = IPHONE;
            } else {
                result = UNKNOWN;
            }

            return result;
        }
    }
}