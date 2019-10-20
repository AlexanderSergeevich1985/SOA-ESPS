package com.soaesps.core.graph;

public enum EdgeType {
    UNDIRECTED("undirected"),
    DIRECTED("directed");

    private String name;

    EdgeType(final String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}