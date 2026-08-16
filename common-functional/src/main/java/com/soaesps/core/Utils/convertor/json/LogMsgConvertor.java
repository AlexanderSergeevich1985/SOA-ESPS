package com.soaesps.core.Utils.convertor.json;

import jakarta.persistence.Converter;

@Converter
public class LogMsgConvertor extends JsonAbstractConverter<Object> {

    public LogMsgConvertor() {
        super(Object.class); // Or pass your specific message base class here
    }
}