package com.soaesps.core.graph;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NetGraph<T extends Serializable, T2 extends Number> {
    private ConcurrentHashMap<T, NetVertex<T, T2>> vertices = new ConcurrentHashMap<>();

    public NetGraph() {}

    public ConcurrentHashMap<T, NetVertex<T, T2>> getVertices() {
        return vertices;
    }

    public void setVertices(final Map<T, NetVertex<T, T2>> vertices) {
        this.vertices.putAll(vertices);
    }

    public void addVertex(final NetVertex<T, T2> vertex) {
        this.vertices.put(vertex.getVertexId(), vertex);
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
    }
}