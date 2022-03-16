package com.soaesps.core.Utils.DataStructure;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DateRange {
    private LocalDateTime from;

    private LocalDateTime to;

    private ZoneId zoneId;

    public DateRange() {}

    public DateRange(LocalDateTime from, LocalDateTime to) {
        if (!check(from, to)) {
            return;
        }
        this.from = from;
        this.to = to;
    }

    public DateRange(LocalDateTime from, LocalDateTime to, ZoneId zoneId) {
        if (!check(from, to)) {
            return;
        }
        this.from = from;
        this.to = to;
        this.zoneId = zoneId;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        if (!check(from, to)) {
            return;
        }
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        if (!check(from, to)) {
            return;
        }
        this.to = to;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public static Long daysBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }

        return ChronoUnit.DAYS.between(from, to);
    }

    public static boolean check(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return true;
        }

        return from.isBefore(to);
    }
}