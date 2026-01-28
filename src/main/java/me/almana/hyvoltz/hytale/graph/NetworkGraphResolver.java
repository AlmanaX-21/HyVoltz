package me.almana.hyvoltz.hytale.graph;

import com.hypixel.hytale.protocol.BlockPosition;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.BiPredicate;

public class NetworkGraphResolver {

    /**
     * Finds disjoint sub-graphs (islands) from a set of starting nodes.
     *
     * @param nodes             The set of nodes to analyze.
     * @param hasNode           A lookup to check if a position has a registered
     *                          node.
     * @param connectivityCheck A predicate that returns true if two adjacent nodes
     *                          are electrically connected.
     * @return A list of disjoint sets of nodes.
     */
    public List<Set<BlockPosition>> findDisjointGraphs(Set<BlockPosition> nodes,
            Predicate<BlockPosition> hasNode,
            BiPredicate<BlockPosition, BlockPosition> connectivityCheck) {
        List<Set<BlockPosition>> disjointSets = new ArrayList<>();
        Set<BlockPosition> visited = new HashSet<>();

        for (BlockPosition node : nodes) {
            if (!visited.contains(node)) {
                Set<BlockPosition> island = new HashSet<>();
                performBFS(node, hasNode, visited, island, connectivityCheck);
                if (!island.isEmpty()) {
                    disjointSets.add(island);
                }
            }
        }
        return disjointSets;
    }

    private void performBFS(BlockPosition start,
            Predicate<BlockPosition> hasNode,
            Set<BlockPosition> visited,
            Set<BlockPosition> island,
            BiPredicate<BlockPosition, BlockPosition> connectivityCheck) {
        Queue<BlockPosition> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        island.add(start);

        while (!queue.isEmpty()) {
            BlockPosition current = queue.poll();

            for (BlockPosition neighbor : getNeighbors(current)) {
                if (hasNode.test(neighbor) && !visited.contains(neighbor)) {
                    // Check if they are actually connected (side compatibility)
                    if (connectivityCheck.test(current, neighbor)) {
                        visited.add(neighbor);
                        island.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private List<BlockPosition> getNeighbors(BlockPosition pos) {
        List<BlockPosition> neighbors = new ArrayList<>(6);
        neighbors.add(new BlockPosition(pos.x + 1, pos.y, pos.z));
        neighbors.add(new BlockPosition(pos.x - 1, pos.y, pos.z));
        neighbors.add(new BlockPosition(pos.x, pos.y + 1, pos.z));
        neighbors.add(new BlockPosition(pos.x, pos.y - 1, pos.z));
        neighbors.add(new BlockPosition(pos.x, pos.y, pos.z + 1));
        neighbors.add(new BlockPosition(pos.x, pos.y, pos.z - 1));
        return neighbors;
    }
}
