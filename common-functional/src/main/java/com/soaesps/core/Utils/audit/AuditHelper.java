package com.soaesps.core.Utils.audit;

import com.soaesps.core.Utils.HttpUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class AuditHelper {
    public String getClientUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "";
        }

        return authentication.getName();
    }

    public static String getClientIpAddressIfServletRequestExist() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return "";
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return HttpUtils.getClientIpAddress(request);
    }

    public static String getStringIP(final Long numericIP) {
        if (numericIP == null) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        builder.append((numericIP >> 24) & 0xFF).append(".");
        builder.append((numericIP >> 16) & 0xFF).append(".");
        builder.append((numericIP >> 8) & 0xFF).append(".");
        builder.append(numericIP & 0xFF);

        return builder.toString();
    }

    public static Long getNumericIP(final String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return null;
        }
        final String[] ipParts = ipAddress.split("\\.");
        long numericIP = 0;
        numericIP |= Long.parseLong(ipParts[0]) << 24;
        numericIP |= Long.parseLong(ipParts[1]) << 16;
        numericIP |= Long.parseLong(ipParts[2]) << 8;
        numericIP |= Long.parseLong(ipParts[3]);

        return numericIP;
    }
}