package team.cappcraft.immersivechemical.common.recipe;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectedConversionGraphTest {
    @Test
    void depthFirstTraversalUsesInsertionOrderAndExcludesStart() {
        DirectedConversionGraph<String, String> graph = new DirectedConversionGraph<>();
        graph.addEdge("a", "b", "a-b", 1);
        graph.addEdge("a", "c", "a-c", 1);
        graph.addEdge("b", "d", "b-d", 1);
        graph.addEdge("c", "e", "c-e", 1);

        assertEquals(List.of("b", "d", "c", "e"), graph.reachableDepthFirst("a"));
        assertEquals(List.of(), graph.reachableDepthFirst("missing"));
    }

    @Test
    void shortestPathUsesWeightsAndStableTieBreaking() {
        DirectedConversionGraph<String, String> graph = new DirectedConversionGraph<>();
        graph.addEdge("a", "b", "a-b", 1);
        graph.addEdge("b", "d", "b-d", 1);
        graph.addEdge("a", "c", "a-c", 1);
        graph.addEdge("c", "d", "c-d", 1);
        graph.addEdge("a", "d", "a-d", 3);

        DirectedConversionGraph.Path<String> path = graph.shortestPath("a", "d").orElseThrow();

        assertEquals(List.of("a-b", "b-d"), path.edges());
        assertEquals(2, path.totalWeight());
    }

    @Test
    void duplicateEdgeKeepsFirstRegisteredValue() {
        DirectedConversionGraph<String, String> graph = new DirectedConversionGraph<>();

        assertTrue(graph.addEdge("hot", "cold", "first", 1));
        assertFalse(graph.addEdge("hot", "cold", "second", 1));
        assertEquals("first", graph.getEdgeValue("hot", "cold").orElseThrow());
    }

    @Test
    void invalidEdgesAndUnreachableTargetsAreRejected() {
        DirectedConversionGraph<String, String> graph = new DirectedConversionGraph<>();

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("a", "a", "loop", 1));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("a", "b", "zero", 0));

        graph.addEdge("a", "b", "a-b", 1);
        graph.addVertex("isolated");
        assertTrue(graph.shortestPath("a", "isolated").isEmpty());
        assertTrue(graph.shortestPath("missing", "a").isEmpty());
    }

    @Test
    void clearRemovesVerticesAndCachedPaths() {
        DirectedConversionGraph<String, String> graph = new DirectedConversionGraph<>();
        graph.addEdge("hot", "cold", "conversion", 1);

        graph.clear();

        assertFalse(graph.containsVertex("hot"));
        assertTrue(graph.reachableDepthFirst("hot").isEmpty());
        assertTrue(graph.shortestPath("hot", "cold").isEmpty());
    }
}
