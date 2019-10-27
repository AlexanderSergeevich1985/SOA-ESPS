package com.soaesps.core.graph;

public interface PathDescI {
    Double getCost();

    void setCost(final Double cost);

    VertexState getState();

    void setState(final VertexState state);

    boolean isChangedAfterAdded();

    enum VertexState {
        UNVISITED(0),
        VISITED(1),
        EVALUATED(2),
        PREPROCESSED(3);

        private int value;

        VertexState(final int value) {
            this.value = value;
        }

        public static VertexState builder(final int value) {
            switch (value) {
                case 0:
                    return VertexState.UNVISITED;
                case 1:
                    return VertexState.VISITED;
                case 2:
                    return VertexState.EVALUATED;
                case 4:
                    return VertexState.PREPROCESSED;
                default:
                    return VertexState.UNVISITED;
            }
        }

        public void setState(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public VertexState next() {
            switch (this) {
                case UNVISITED:
                    this.setState(VertexState.VISITED.value);
                    break;
                case VISITED:
                    this.setState(VertexState.EVALUATED.value);
                    break;
                default:
                    break;
            }

            return this;
        }
    }
}