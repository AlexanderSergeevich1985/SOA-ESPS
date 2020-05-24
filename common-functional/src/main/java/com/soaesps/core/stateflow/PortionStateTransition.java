package com.soaesps.core.stateflow;

import java.time.LocalDateTime;

public class PortionStateTransition { //describe the difference between two states the same data partion
    private LocalDateTime whenUpdatedLocal;

    private StateTransitionDesc stateTransitionDesc;

    public PortionStateTransition() {}

    public PortionStateTransition(final LocalDateTime whenUpdatedLocal,
                                  final StateTransitionDesc stateTransitionDesc) {
        this.whenUpdatedLocal = whenUpdatedLocal;
        this.stateTransitionDesc = stateTransitionDesc;
    }

    public LocalDateTime whenHaveBeenUpdatedLocal() {
        return whenUpdatedLocal;
    }

    interface StateTransitionDesc {
        StateTransitionDesc getDiffBetweenTwoStates();
    }
}