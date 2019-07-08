package com.soaesps.core.BaseOperation.Filter;

import org.jblas.DoubleMatrix;
import org.springframework.stereotype.Component;

@Component(value = "predictionFilterImpl")
public class PredictionFilterImpl<T extends String, T1> implements PredictionFilter<T, T1> {
    private DoubleMatrix matrix;

    public void setUpInSignal(final T in) {
    }

    public void setUpOutSignal(final T1 out) {
    }

    public T1 getPrediction() {
        return null;
    }
}
