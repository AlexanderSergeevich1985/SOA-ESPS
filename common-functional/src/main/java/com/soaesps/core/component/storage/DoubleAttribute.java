package com.soaesps.core.component.storage;

/**
 * Created by sniper on 20.11.22.
 */
public class DoubleAttribute extends Attribute {
    @Override
    public Integer getNumericValue() {
        return Integer.valueOf(value);
    }
}