package com.soaesps.core.graph;

import java.io.Serializable;
import java.util.LinkedHashSet;

public interface NetGraphI<T extends Serializable, T2 extends Number> {
    void addVertex(final NetVertex<T, T2> vertex);

    void addEdge(final NetEdge<T, T2> edge);

    LinkedHashSet<NetVertex<T,T2>> getPath(final int size, final NetVertex<T, T2> vertex1, final NetVertex<T, T2> vertex2);
}