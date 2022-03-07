package com.soaesps.core.DataModels.market;

import java.math.BigDecimal;

public class MarketPosition {
    private BigDecimal entry; //вход

    private BigDecimal stopLoss; //стоп-лосс

    private BigDecimal risk; //риск

    private BigDecimal target; //таргет

    private BigDecimal takeProfit; //прибыль

    private BigDecimal ratio; //отношение риска к прибыли
}