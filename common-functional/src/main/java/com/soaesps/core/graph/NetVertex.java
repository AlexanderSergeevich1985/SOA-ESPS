package com.soaesps.core.graph;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NetVertex<T extends Serializable, T2 extends Number> implements Serializable {
    private T vertexId;

    private Double probability;

    private Map<T, Double> probabilities = new ConcurrentHashMap<>();

    //adjacent vertices
    private Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc<T2>> vertices = new ConcurrentHashMap<>();

    private AtomicReference<PathDescI> pathDescI;

    public NetVertex(final T vertexId) {
        this.vertexId = vertexId;
    }

    public NetVertex(final T vertexId, final Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc<T2>> vertices) {
        this.vertexId = vertexId;
        this.vertices.putAll(vertices);
        probability = 1.0 / vertices.size();
    }

    public PathDescI getPathDescI() {
        return pathDescI.get();
    }

    public void setPathDescI(final PathDescI expected, final PathDescI pathDescI) {
        this.pathDescI.compareAndSet(expected, pathDescI);
    }

    public void setVertexId(final T vertexId) {
        this.vertexId = vertexId;
    }

    public T getVertexId() {
        return this.vertexId;
    }

    public Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc<T2>> getVertices() {
        return vertices;
    }

    public void setVertices(final Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc<T2>> vertices) {
        this.vertices.putAll(vertices);
        probability = 1.0 / vertices.size();
    }

    public void addAdjVertex(final NetVertex<T, T2> vertex, final NetEdge.NetEdgeDesc<T2> desc) {
        this.vertices.put(vertex, desc);
        probability = 1.0 / vertices.size();
    }

    public Double getProbability() {
        if (probability == null) {
            probability = 1.0 / vertices.size();
        }
        return probability;
    }

    public void calculateProbs() {
        Optional<Double> max = vertices.values().stream().map(d -> (Double) d.getDistance()).max(Double::compare);
        if (!max.isPresent()) {
            return;
        }
        final Map<T, Double> coeff = new HashMap<>();
        vertices.entrySet().stream().forEach(e -> {
            coeff.put(e.getKey().getVertexId(), max.get()/(Double) e.getValue().getDistance());
        });
        final Double sum = coeff.values().stream().collect(Collectors.summingDouble(Double::intValue));
        final Double minProb = 1/sum;
        coeff.entrySet().stream().forEach(e -> {
            probabilities.put(e.getKey(), minProb * e.getValue());
        });
    }

    public void setProbability(final Double probability) {
        this.probability = probability;
    }

    @Override
    public int hashCode() {
        return vertexId.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetEdge)) {
            return false;
        }

        final NetVertex other = (NetVertex) obj;

        if (vertexId == null || other.getVertexId() == null) {
            return false;
        }
        else if (!(vertexId.equals(other.getVertexId()))) {
            return false;
        }

        return true;
    }
}