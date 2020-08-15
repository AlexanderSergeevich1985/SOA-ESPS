package com.soaesps.quotesservice.service;

import com.nukefintech.types.market.Quotes;
import com.nukefintech.types.market.Symbols;

public interface StocksServiceI {
    Quotes getQuotes(Symbols request);
}