package com.soaesps.core.Utils.operation;

public interface HandlerI<T> {
    void process(ExchangerI<T> exchanger);
}