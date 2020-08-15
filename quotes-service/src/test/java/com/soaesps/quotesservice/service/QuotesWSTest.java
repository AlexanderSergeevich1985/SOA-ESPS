package com.soaesps.quotesservice.service;

import com.nukefintech.types.market.Quotes;
import com.nukefintech.types.market.Symbols;
import com.soaesps.quotesservice.config.QuotesWSTestConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ws.client.core.WebServiceTemplate;


@RunWith(SpringRunner.class)
@Import(QuotesWSTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class QuotesWSTest {
    @Autowired
    private WebServiceTemplate webServiceTemplate;

    @Test
    public void loadQuotes() {
        Assert.assertNotNull(webServiceTemplate);
        final Quotes greeting = (Quotes) webServiceTemplate
                .marshalSendAndReceive(getSymbols());
    }

    private Symbols getSymbols() {
        final Symbols symbols = new Symbols();
        symbols.setSymbols("test");

        return symbols;
    }
}