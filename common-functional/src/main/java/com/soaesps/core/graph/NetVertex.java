package com.soaesps.core.graph;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetVertex<T extends Serializable, T2 extends Number> implements Serializable {
    private T vertexId;

    //adjacent vertexes
    private Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc> vertices = new ConcurrentHashMap<>();

    public NetVertex(final T vertexId, final Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc<T2>> vertexes) {
        this.vertexId = vertexId;
        this.vertices.putAll(vertexes);
    }

    public void setVertexId(final T vertexId) {
        this.vertexId = vertexId;
    }

    public T getVertexId() {
        return this.vertexId;
    }

    public Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc> getVertices() {
        return vertices;
    }

    public void setVertices(final Map<NetVertex<T, T2>, NetEdge.NetEdgeDesc<T2>> vertexes) {
        this.vertices.putAll(vertexes);
    }

    public void addAdjVertex(final NetVertex<T, T2> vertex, final NetEdge.NetEdgeDesc<T2> desc) {
        this.vertices.put(vertex, desc);
    }

    @Override
    public int hashCode() {
        return vertexId.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null || !(obj instanceof NetEdge)) {
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