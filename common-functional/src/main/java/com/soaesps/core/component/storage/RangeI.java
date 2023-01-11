package com.soaesps.core.component.storage;

/**
 * Created by sniper on 19.11.22.
 */
public interface RangeI<T> {
    boolean valueInRange(T value, Boolean startIn, Boolean endIn);

    <T1 extends Number> long getIndexByValue(T1 value);
}