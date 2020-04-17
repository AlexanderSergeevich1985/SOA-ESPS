package com.soaesps.core.Utils.audit;

public class AuditHelper {
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