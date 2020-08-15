package com.soaesps.quotesservice.endpoint;

import com.nukefintech.types.market.Quotes;
import com.nukefintech.types.market.Symbols;

import com.soaesps.core.Utils.TimeSynchronizer;
import com.soaesps.quotesservice.service.StocksServiceI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.logging.Level;
import java.util.logging.Logger;

@Endpoint
public class QuotesEndpoint {
    private static final Logger logger;

    static {
        logger = Logger.getLogger(TimeSynchronizer.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private StocksServiceI stocksService;

    @PayloadRoot(namespace = "http://nukefintech.com/types/market",
            localPart = "symbols")
    @ResponsePayload
    public Quotes getQuotes(@RequestPayload Symbols request) {
        logger.log(Level.INFO, "Requested symbols: {}", request.getSymbols());

        final Quotes response = stocksService.getQuotes(request);

        logger.log(Level.INFO, "Requested quotes sending: {}", response.getSymbol());

        return response;
    }
}