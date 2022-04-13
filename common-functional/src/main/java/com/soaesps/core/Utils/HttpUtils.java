package com.soaesps.core.Utils;

import com.soaesps.core.patterns.Patterns;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

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

    public enum ErrorMsg {
        ON_USER_NOT_FOUND("ERRC01", ""),
        ON_USER_ALREADY_EXIST("ERRC02", ""),
        ON_ILLEGAL_ARGUMENT_EXCEPTION("ERRC03", ""),
        ON_EXCEPTION("ERRC04", "");

        private String errCode;

        private String errMsg;

        ErrorMsg(String errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }

        public String getErrCode() {
            return this.errCode;
        }

        public void setErrCode(String errCode) {
            this.errCode = errCode;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }
    }

    public static <T> ResponseEntity<T> onOk(String... headers) {
        return statusAndHeaders(HttpStatus.OK, headers).build();
    }

    public static <T> ResponseEntity<T> onOk(T value) {
        ResponseEntity.BodyBuilder builder = statusAndHeaders(HttpStatus.OK);
        builder.body(value);

        return builder.build();
    }

    public static <T> ResponseEntity<T> onOk(T value, String... headers) {
        ResponseEntity.BodyBuilder builder = statusAndHeaders(HttpStatus.OK, headers);
        builder.body(value);

        return builder.build();
    }

    public static <T> ResponseEntity<T> onError(ErrorMsg error, String... headers) {
        ResponseEntity.BodyBuilder builder = statusAndHeaders(HttpStatus.INTERNAL_SERVER_ERROR, headers);
        builder.header("x-error-code", error.getErrCode());
        builder.header("X-Error-Info", error.getErrMsg());

        return builder.build();
    }

    public static <T> ResponseEntity<T> onError(Exception ex, String... headers) {
        ResponseEntity.BodyBuilder builder = statusAndHeaders(HttpStatus.INTERNAL_SERVER_ERROR, headers);
        builder.header("x-error-code", ex.getClass().getName());
        builder.body(ex.getLocalizedMessage());

        return builder.build();
    }

    public static <T> ResponseEntity<T> headersAndBody(HttpStatus status, T value, String... headers) {
        ResponseEntity.BodyBuilder builder = statusAndHeaders(status, headers);
        builder.body(value);

        return builder.build();
    }

    public static ResponseEntity.BodyBuilder statusAndHeaders(HttpStatus status, String... headers) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        for (int i = 0; i < headers.length - 1; i += 2) {
            builder.header(headers[i], headers[i+1]);
        }

        return builder;
    }
}