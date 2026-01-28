package me.almana.hyvoltz.hytale;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import me.almana.hyvoltz.core.network.ElectricNetwork;
import me.almana.hyvoltz.core.node.ElectricNode;
import me.almana.hyvoltz.core.tick.ManualTickSource;
import me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent;
import me.almana.hyvoltz.hytale.graph.NetworkGraphResolver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the electric networks for a specific Hytale world.
 * Handles adjacency-based discovery, merging, and splitting.
 */
public class WorldEnergyManager {
    private final UUID worldId;

    // Topology State
    private final Set<ElectricNetwork> networks = ConcurrentHashMap.newKeySet();
    private final Map<ElectricNetwork, ManualTickSource> tickSources = new ConcurrentHashMap<>();

    // Spatial State
    private final Map<BlockPosition, HyVoltzNodeComponent> nodesByPos = new ConcurrentHashMap<>();
    private final Map<ElectricNode, BlockPosition> posByNode = new ConcurrentHashMap<>();

    private final NetworkGraphResolver resolver = new NetworkGraphResolver();

    public WorldEnergyManager(UUID worldId) {
        this.worldId = worldId;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public void registerNode(HyVoltzNodeComponent component, BlockPosition pos) {
        if (nodesByPos.containsKey(pos)) {
            return;
        }
        nodesByPos.put(pos, component);
        posByNode.put(component.getNode(), pos);

        // Discovery
        Set<ElectricNetwork> neighborNetworks = new HashSet<>();
        for (BlockPosition neighbor : getNeighbors(pos)) {
            HyVoltzNodeComponent nComp = nodesByPos.get(neighbor);
            if (nComp != null) {
                if (areConnected(component, pos, nComp, neighbor)) {
                    ElectricNetwork net = findNetworkFor(nComp.getNode());
                    if (net != null) {
                        neighborNetworks.add(net);
                    }
                }
            }
        }

        if (neighborNetworks.isEmpty()) {
            createNetwork(component.getNode());
        } else if (neighborNetworks.size() == 1) {
            ElectricNetwork net = neighborNetworks.iterator().next();
            net.addNode(component.getNode());
        } else {
            mergeNetworks(neighborNetworks, component.getNode());
        }
    }

    public void unregisterNode(ElectricNode node) {
        BlockPosition pos = posByNode.remove(node);
        if (pos == null)
            return;

        HyVoltzNodeComponent comp = nodesByPos.remove(pos); // Actually removed

        ElectricNetwork net = findNetworkFor(node);
        if (net == null)
            return;

        net.removeNode(node);

        // Check for split
        Set<BlockPosition> neighbors = new HashSet<>();
        for (BlockPosition nPos : getNeighbors(pos)) {
            if (nodesByPos.containsKey(nPos)) {
                neighbors.add(nPos);
            }
        }

        if (neighbors.size() > 1) {
            // Predicate: Do neighbors have a valid electrical path between them?
            List<Set<BlockPosition>> islands = resolver.findDisjointGraphs(neighbors,
                    nodesByPos::containsKey,
                    this::checkConnectivityByPos);

            if (islands.size() > 1) {
                removeNetwork(net);
                for (Set<BlockPosition> island : islands) {
                    createNetworkFromIsland(island);
                }
            }
        }

        if (net.getNodes().isEmpty()) {
            removeNetwork(net);
        }
    }

    private boolean checkConnectivityByPos(BlockPosition posA, BlockPosition posB) {
        HyVoltzNodeComponent compA = nodesByPos.get(posA);
        HyVoltzNodeComponent compB = nodesByPos.get(posB);
        if (compA == null || compB == null)
            return false;

        return areConnected(compA, posA, compB, posB);
    }

    private boolean areConnected(HyVoltzNodeComponent a, BlockPosition pA, HyVoltzNodeComponent b, BlockPosition pB) {
        Vector3i dir = getRelativeDirection(pA, pB);
        if (dir == null)
            return false; // Not compatible or non-orthogonal?

        Vector3i opp = new Vector3i(-dir.x, -dir.y, -dir.z); // Simple inverse

        boolean aOut = a.getSideConfig().canOutput(dir);
        boolean bIn = b.getSideConfig().canInput(opp);

        boolean bOut = b.getSideConfig().canOutput(opp);
        boolean aIn = a.getSideConfig().canInput(dir);

        // Connected if A -> B OR B -> A is valid
        // Simple bidirectional networks often merge if ANY connection exists.
        return (aOut && bIn) || (bOut && aIn);
    }

    private Vector3i getRelativeDirection(BlockPosition from, BlockPosition to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        int dz = to.z - from.z;

        // Match constants
        if (dx == 0 && dy == 0) {
            if (dz == -1)
                return Vector3i.NORTH;
            if (dz == 1)
                return Vector3i.SOUTH;
        }
        if (dx == 0 && dz == 0) {
            if (dy == 1)
                return Vector3i.UP;
            if (dy == -1)
                return Vector3i.DOWN;
        }
        if (dy == 0 && dz == 0) {
            if (dx == 1)
                return Vector3i.EAST;
            if (dx == -1)
                return Vector3i.WEST;
        }
        return null; // Non-adjacent
    }

    private void createNetwork(ElectricNode initialNode) {
        ElectricNetwork net = new ElectricNetwork();
        ManualTickSource tick = new ManualTickSource();
        tick.register(net);
        networks.add(net);
        tickSources.put(net, tick);
        net.addNode(initialNode);
    }

    private void createNetworkFromIsland(Set<BlockPosition> island) {
        ElectricNetwork net = new ElectricNetwork();
        ManualTickSource tick = new ManualTickSource();
        tick.register(net);
        networks.add(net);
        tickSources.put(net, tick);

        for (BlockPosition p : island) {
            HyVoltzNodeComponent comp = nodesByPos.get(p);
            if (comp != null) {
                net.addNode(comp.getNode());
            }
        }
    }

    private void mergeNetworks(Set<ElectricNetwork> toMerge, ElectricNode connectingNode) {
        ElectricNetwork primary = toMerge.iterator().next();

        for (ElectricNetwork other : toMerge) {
            if (other == primary)
                continue;
            List<ElectricNode> otherNodes = new ArrayList<>(other.getNodes());
            for (ElectricNode n : otherNodes) {
                other.removeNode(n);
                primary.addNode(n);
            }
            removeNetwork(other);
        }
        primary.addNode(connectingNode);
    }

    private void removeNetwork(ElectricNetwork net) {
        networks.remove(net);
        tickSources.remove(net);
    }

    private ElectricNetwork findNetworkFor(ElectricNode node) {
        for (ElectricNetwork net : networks) {
            if (net.getNodes().contains(node)) {
                return net;
            }
        }
        return null; // Need to optimize this if I can figure it out in future
    }

    public void tick() {
        for (ManualTickSource src : tickSources.values()) {
            src.advance();
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
