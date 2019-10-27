package com.soaesps.core.graph;

import java.io.Serializable;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathFinder {
    private static final Logger logger;

    static {
        logger = Logger.getLogger(PathFinder.class.getName());
        logger.setLevel(Level.INFO);
    }

    private static final Integer MAX_DEFAULT_QUEUE_SIZE = 1000;

    private static AtomicBoolean doDFS = new AtomicBoolean(true);

    final static ForkJoinWorkerThreadFactory factory = pool -> {
        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("index_" + worker.getPoolIndex());

        return worker;
    };

    private ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), factory, new BFSExceptionHandler(), true);

    private PriorityBlockingQueue<NetVertex> queue;

    private AtomicBoolean isFound;

    public <T extends Serializable, T2 extends Number> NetVertex<T, T2> BFS_Cycle(final int size, final NetVertex<T, T2> vertex1, final NetVertex<T, T2> vertex2) {
        if (queue == null) {
            queue = new PriorityBlockingQueue<>(calcInitialSize(size), createComparator());
        }
        this.isFound = new AtomicBoolean(false);
        queue.add(vertex1);
        while (!queue.isEmpty() && !isFound.get()) {
            NetVertex<T, T2> vertex = this.queue.poll();
            if (vertex.equals(vertex2)) {
                return vertex;
            }
            if (vertex.getPathDescI().getState().equals(PathDescI.VertexState.UNVISITED)) {
                vertex.getPathDescI().setState(PathDescI.VertexState.VISITED);
            }
            else if (!doDFS.get() || vertex.getPathDescI().isChangedAfterAdded() || !vertex.getPathDescI().getState().equals(PathDescI.VertexState.PREPROCESSED)) {
                exploreVertex(vertex);
            }
            for (final NetVertex<T, T2> v: vertex.getVertices().keySet()) {
                this.queue.add(v);
            }
            vertex.getPathDescI().setState(PathDescI.VertexState.EVALUATED);
        }

        return null;
    }

    private int calcInitialSize(final int size) {
        return size > MAX_DEFAULT_QUEUE_SIZE ? MAX_DEFAULT_QUEUE_SIZE : size;
    }

    private void exploreVertex(final NetVertex vertex) {
        final Map<NetVertex, NetEdge.NetEdgeDesc> neighbours = vertex.getVertices();
        neighbours.entrySet().stream().forEach(entry -> {
            final NetVertex v = entry.getKey();
            final NetEdge.NetEdgeDesc e = entry.getValue();
            if (v.getPathDescI() == null) {
                vertex.setPathDescI(null, new PathDesc());
            }
            final PathDescI pd = v.getPathDescI();
            pd.setCost(e.getDistance().doubleValue()+vertex.getPathDescI().getCost());
        });
        if (doDFS.get()) {
            neighbours.keySet().stream().map(DFStask::new).forEach(t -> this.forkJoinPool.invoke(t));
        }
    }

    public class DFStask extends RecursiveAction {
        private NetVertex vertex;

        public DFStask(final NetVertex vertex) {
            this.vertex = vertex;
        }

        @Override
        protected void compute() {
            if (isFound.get()) {
                return;
            }
            exploreVertex(vertex);
            this.vertex.getPathDescI().setState(PathDescI.VertexState.PREPROCESSED);
        }
    }

    public static  <T extends Serializable, T2 extends Number> Comparator<NetVertex<T, T2>> createComparator() {
        return (o1, o2) -> {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                }
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            int result = o1.getPathDescI().getCost().compareTo(o2.getPathDescI().getCost());

            return ~(result - 1);
        };
    }

    public static class BFSExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            logger.log(Level.SEVERE, "[PathFinder/BFSExceptionHandler] thread name:".concat(t.getName()).concat(" runtime exception occurred:").concat(e.getMessage()));
        }
    }
}