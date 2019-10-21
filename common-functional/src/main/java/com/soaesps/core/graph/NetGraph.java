package com.soaesps.core.graph;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NetGraph<T extends Serializable, T2 extends Number> implements NetGraphI<T, T2> {
    private ConcurrentHashMap<T, NetVertex<T, T2>> vertices = new ConcurrentHashMap<>();

    public NetGraph() {}

    @Override
    public void addVertex(final NetVertex<T, T2> vertex) {
        this.vertices.put(vertex.getVertexId(), vertex);
    }

    @Override
    public void addEdge(final NetEdge<T, T2> edge) {
        final NetVertex<T, T2> vertex1 = this.vertices.containsKey(edge.getVertex1().getVertexId()) ? this.vertices.get(edge.getVertex1().getVertexId()) : edge.getVertex1();
        final NetVertex<T, T2> vertex2 = this.vertices.containsKey(edge.getVertex2().getVertexId()) ? this.vertices.get(edge.getVertex2().getVertexId()) : edge.getVertex2();
        if (!vertex1.getVertices().containsKey(vertex2)) {
            vertex1.getVertices().put(vertex2, edge.getDesc());
        }
        if (edge.getDesc().getType().equals(EdgeType.UNDIRECTED)) {
            if (!vertex2.getVertices().containsKey(vertex1)) {
                vertex2.getVertices().put(vertex1, edge.getDesc());
            }
        }
    }

    @Override
    public LinkedHashSet<NetEdge<T, T2>> getPath(final NetVertex<T, T2> vertex1, final NetVertex<T, T2> vertex2) {
        return null;
    }

    public ConcurrentHashMap<T, NetVertex<T, T2>> getVertices() {
        return vertices;
    }

    public void setVertices(final Map<T, NetVertex<T, T2>> vertices) {
        this.vertices.putAll(vertices);
    }

    public void removeVertex(final T vertexId) {
        this.vertices.remove(vertexId);
    }

    public Map<EdgeKey<T>, NetEdge<T, T2>> сonvertToEdges() {
        return vertices.values().stream()
                .flatMap(v -> v.getVertices().entrySet().stream().map(e -> new NetEdge<T, T2>(v, e.getKey(), e.getValue())))
                .collect(Collectors.toConcurrentMap(ed -> new EdgeKey<T>(ed.getVertex1().getVertexId(), ed.getVertex2().getVertexId()), Function.identity()));
    }

    public Callable<Double> getWalker() {
        return new Callable<Double>() {
            @Override
            public Double call() throws Exception {

                return null;
            }
        };
    }

    static public class EdgeKey<T extends Serializable> {
        private T key1;

        private T key2;

        public EdgeKey(final T key1, final T key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        public T getKey1() {
            return key1;
        }

        public void setKey1(final T key1) {
            this.key1 = key1;
        }

        public T getKey2() {
            return key2;
        }

        public void setKey2(final T key2) {
            this.key2 = key2;
        }

        @Override
        public int hashCode() {
            int hash1 = key1.hashCode();
            int hash2 = key2.hashCode();

            return (hash1 * hash2)/(hash1 + hash2);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || !this.getClass().isInstance(obj)) {
                return false;
            }
            final EdgeKey<T> other = (EdgeKey<T>) obj;
            if (key1 == null || key2 == null || other.getKey1() == null || other.getKey2() == null) {
                return false;
            }
            if (!key1.equals(other.getKey1()) || !key2.equals(other.getKey2())) {
                return false;
            }

            return true;
        }
    }
}