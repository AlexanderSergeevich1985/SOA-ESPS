package com.soaesps.core.graph;

import java.io.Serializable;
import java.util.LinkedHashSet;

public interface NetGraphI<T extends Serializable, T2 extends Number> {
    void addVertex(final NetVertex<T, T2> vertex);

    void addEdge(final NetEdge<T, T2> edge);

    LinkedHashSet<NetEdge<T, T2>> getPath(final NetVertex<T, T2> vertex1, final NetVertex<T, T2> vertex2);
}