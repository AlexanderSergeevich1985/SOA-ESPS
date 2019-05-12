package com.soaesps.core.BaseOperation.Filter;

public interface PredictionFilter<T, T1> {
    void setUpInSignal(final T in);

    void setUpOutSignal(final T1 out);

    T1 getPrediction();
}