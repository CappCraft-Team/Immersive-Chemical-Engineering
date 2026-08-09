package team.cappcraft.immersivechemical.common.recipe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A small insertion-ordered directed graph used by the heat exchanger registry.
 *
 * <p>The implementation is deliberately dependency-free. Traversal and equal-cost
 * path selection follow edge insertion order, which makes recipe selection stable
 * across runs.</p>
 */
final class DirectedConversionGraph<N, E> {
    private final Map<N, LinkedHashMap<N, Edge<N, E>>> outgoing = new LinkedHashMap<>();

    boolean addVertex(N vertex) {
        Objects.requireNonNull(vertex, "vertex");
        if (outgoing.containsKey(vertex)) {
            return false;
        }
        outgoing.put(vertex, new LinkedHashMap<>());
        return true;
    }

    boolean containsVertex(N vertex) {
        return outgoing.containsKey(vertex);
    }

    void clear() {
        outgoing.clear();
    }

    boolean addEdge(N source, N target, E value, int weight) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(value, "value");
        if (source.equals(target)) {
            throw new IllegalArgumentException("Self-loop conversions are not supported");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Edge weight must be positive");
        }

        addVertex(source);
        addVertex(target);
        return outgoing.get(source).putIfAbsent(target, new Edge<>(source, target, value, weight)) == null;
    }

    Optional<E> getEdgeValue(N source, N target) {
        Map<N, Edge<N, E>> fromSource = outgoing.get(source);
        if (fromSource == null) {
            return Optional.empty();
        }
        Edge<N, E> edge = fromSource.get(target);
        return edge == null ? Optional.empty() : Optional.of(edge.value);
    }

    List<N> reachableDepthFirst(N start) {
        if (!containsVertex(start)) {
            return Collections.emptyList();
        }

        List<N> result = new ArrayList<>();
        Set<N> visited = new HashSet<>();
        Deque<N> pending = new ArrayDeque<>();
        pending.push(start);

        while (!pending.isEmpty()) {
            N current = pending.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (!current.equals(start)) {
                result.add(current);
            }

            List<N> neighbors = new ArrayList<>(outgoing.get(current).keySet());
            for (int i = neighbors.size() - 1; i >= 0; --i) {
                N neighbor = neighbors.get(i);
                if (!visited.contains(neighbor)) {
                    pending.push(neighbor);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    Optional<Path<E>> shortestPath(N start, N target) {
        if (!containsVertex(start) || !containsVertex(target)) {
            return Optional.empty();
        }
        if (start.equals(target)) {
            return Optional.of(new Path<>(Collections.emptyList(), 0));
        }

        Map<N, Long> distances = new HashMap<>();
        Map<N, Edge<N, E>> previous = new HashMap<>();
        Set<N> settled = new HashSet<>();
        PriorityQueue<NodeDistance<N>> pending = new PriorityQueue<>(
                Comparator.comparingLong(NodeDistance<N>::distance)
                        .thenComparingLong(NodeDistance<N>::order));

        long insertionOrder = 0;
        distances.put(start, 0L);
        pending.add(new NodeDistance<>(start, 0L, insertionOrder++));

        while (!pending.isEmpty()) {
            NodeDistance<N> current = pending.remove();
            if (!Objects.equals(distances.get(current.node), current.distance) || !settled.add(current.node)) {
                continue;
            }
            if (current.node.equals(target)) {
                break;
            }

            for (Edge<N, E> edge : outgoing.get(current.node).values()) {
                if (settled.contains(edge.target)) {
                    continue;
                }
                long candidate = Math.addExact(current.distance, edge.weight);
                Long known = distances.get(edge.target);
                if (known == null || candidate < known) {
                    distances.put(edge.target, candidate);
                    previous.put(edge.target, edge);
                    pending.add(new NodeDistance<>(edge.target, candidate, insertionOrder++));
                }
            }
        }

        Long totalWeight = distances.get(target);
        if (totalWeight == null || !previous.containsKey(target)) {
            return Optional.empty();
        }

        Deque<E> reversed = new ArrayDeque<>();
        N cursor = target;
        while (!cursor.equals(start)) {
            Edge<N, E> edge = previous.get(cursor);
            if (edge == null) {
                return Optional.empty();
            }
            reversed.push(edge.value);
            cursor = edge.source;
        }
        return Optional.of(new Path<>(new ArrayList<>(reversed), totalWeight));
    }

    private static final class Edge<N, E> {
        private final N source;
        private final N target;
        private final E value;
        private final int weight;

        private Edge(N source, N target, E value, int weight) {
            this.source = source;
            this.target = target;
            this.value = value;
            this.weight = weight;
        }
    }

    private static final class NodeDistance<N> {
        private final N node;
        private final long distance;
        private final long order;

        private NodeDistance(N node, long distance, long order) {
            this.node = node;
            this.distance = distance;
            this.order = order;
        }

        private long distance() {
            return distance;
        }

        private long order() {
            return order;
        }
    }

    static final class Path<E> {
        private final List<E> edges;
        private final long totalWeight;

        private Path(List<E> edges, long totalWeight) {
            this.edges = Collections.unmodifiableList(edges);
            this.totalWeight = totalWeight;
        }

        List<E> edges() {
            return edges;
        }

        long totalWeight() {
            return totalWeight;
        }
    }
}
