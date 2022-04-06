package com.soaesps.core.Utils;

import com.soaesps.core.patterns.Patterns;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class HttpUtils {
    private HttpUtils() {
        throw new UnsupportedOperationException();
    }

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
            "REMOTE_ADDR"
    };

    public static String getClientIpAddress(final HttpServletRequest request) {
        for (final String header: IP_HEADER_CANDIDATES) {
            final String ipList = request.getHeader(header);
            if (ipList == null || ipList.isEmpty() || "unknown".equalsIgnoreCase(ipList)) {
                continue;
            }
            String ip = ipList.split(",")[0];
            if (ip.length() != 15) {
                continue;
            }
            if (Patterns.IP_PATTERN.isMatches(ip)) {
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