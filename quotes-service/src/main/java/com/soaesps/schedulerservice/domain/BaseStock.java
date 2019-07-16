package com.soaesps.schedulerservice.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.soaesps.core.DataModels.serializer.ZonedDateTimeDeserializer;
import com.soaesps.core.DataModels.serializer.ZonedDateTimeSerializer;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class BaseStock {
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime timeStamp;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    private BigDecimal adjClose;

    public BaseStock() {}

    public ZonedDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final ZonedDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(final BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(final BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(final BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(final BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(final BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getAdjClose() {
        return adjClose;
    }

    public void setAdjClose(final BigDecimal adjClose) {
        this.adjClose = adjClose;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.toString();
    }
}