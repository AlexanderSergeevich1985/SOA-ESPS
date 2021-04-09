package com.soaesps.core.BaseOperation.Filter;

import java.util.Set;

public interface IModelSelector<T, T2> {
    Set<PredictionFilter<T, T2>> getModels();

    void addModel(PredictionFilter<T, T2> newModel);

    PredictionFilter<T, T2> generateNewModel(T inputSignal);

    PredictionFilter<T, T2> chooseModelByState(T inputSignal);
}