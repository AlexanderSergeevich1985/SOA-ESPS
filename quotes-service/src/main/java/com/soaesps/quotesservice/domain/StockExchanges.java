package com.soaesps.quotesservice.domain;

import com.soaesps.core.DataModels.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "SOA_ESPS.MARKET_CATALOG")
public class StockExchanges extends BaseEntity {
    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "marketCode", nullable = false)
    private String marketCode;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "delay", nullable = false)
    private Double delay;

    public StockExchanges() {
        super();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getMarketCode() {
        return marketCode;
    }

    public void setMarketCode(final String marketCode) {
        this.marketCode = marketCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Double getDelay() {
        return delay;
    }

    public void setDelay(final Double delay) {
        this.delay = delay;
    }
}