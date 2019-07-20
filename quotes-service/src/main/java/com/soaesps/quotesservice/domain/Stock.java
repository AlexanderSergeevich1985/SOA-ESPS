package com.soaesps.quotesservice.domain;

import com.fasterxml.jackson.annotation.*;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stock extends BaseStock implements Comparable<Stock> {
    private String symbol;

    private BigDecimal prediction;

    public Stock() {
        super();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrediction() {
        return prediction;
    }

    public void setPrediction(final BigDecimal prediction) {
        this.prediction = prediction;
    }

    @Override
    public int compareTo(final Stock other) {
        if (prediction == null) {
            if (other.prediction == null) {
                return 0;
            }
            return -1;
        }
        else if (other.prediction == null) {
            if (prediction == null) {
                return 0;
            }
            return 1;
        }

        return prediction.compareTo(other.prediction);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[Stock]: {").append('\n');
        builder.append("symbol: ").append(symbol).append('\n');
        builder.append(super.toString()).append('\n');
        builder.append("}");
        return builder.toString();
    }
}