package com.soaesps.core.graph;

import java.io.Serializable;

public class NetEdge<T extends Serializable, T2 extends Number> {
    private NetVertex<T, T2> vertex1;

    private NetVertex<T, T2> vertex2;

    private NetEdgeDesc<T2> desc;

    private int hash;

    public NetEdge(final NetVertex<T, T2> vertex1, final NetVertex<T, T2> vertex2, final NetEdgeDesc<T2> desc) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.desc = desc;
    }

    public NetVertex<T, T2> getVertex1() {
        return vertex1;
    }

    public void setVertex1(final NetVertex<T, T2> vertex1) {
        this.vertex1 = vertex1;
    }

    public NetVertex<T, T2> getVertex2() {
        return vertex2;
    }

    public void setVertex2(final NetVertex<T, T2> vertex2) {
        this.vertex2 = vertex2;
    }

    public NetEdgeDesc<T2> getDesc() {
        return desc;
    }

    public void setDesc(final NetEdgeDesc<T2> desc) {
        this.desc = desc;
    }

    public int getHash() {
        return hash;
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
        final NetEdge other = (NetEdge) obj;
        if (vertex1 == null || other.getVertex1() == null || vertex2 == null || other.getVertex2() == null) {
            return false;
        }
        else if (!vertex1.equals(other.getVertex1()) && !vertex2.equals(other.getVertex2())) {
            return false;
        }
        if (desc == null || other.getDesc() == null) {
            return false;
        }
        else if (!desc.equals(other.getDesc())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Math.abs(vertex1.hashCode() + vertex2.hashCode());
            int counter = 10;
            int module = hash+1000;
            //Linear congruential generator
            while (counter > 0) {
                hash = (125*hash + 345)%module;
                --counter;
            }
        }

        return hash;
    }

    static public class NetEdgeDesc<T extends Number> {
        private T distance;

        private EdgeType type;

        public T getDistance() {
            return distance;
        }

        public void setDistance(final T distance) {
            this.distance = distance;
        }

        public EdgeType getType() {
            return type;
        }

        public void setType(final EdgeType type) {
            this.type = type;
        }
    }
}