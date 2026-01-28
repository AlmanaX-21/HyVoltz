# Network Behavior

## Network Formation

Networks are formed lazily. When you attach a node using `HyVoltzNodeComponent.attach()`:
1.  HyVoltz scans the 6 adjacent blocks.
2.  It checks for **compatible sides** (Output facing Input).
3.  If it finds neighbors, it merges into their network.
4.  If it finds no neighbors, it creates a new isolated network.

## Merging

If you place a wire that connects two separate networks, HyVoltz detects this and performs a **Merge**.
*   The smaller network is dissolved.
*   All nodes from the smaller network are added to the larger network.
*   This happens instantly in the same tick.

## Splitting

If you break a block in the middle of a network:
1.  HyVoltz removes the broken node.
2.  It traverses the remaining nodes to check for connectivity.
3.  If the graph is now disjoint (split into pieces), it creates new networks for each "island" or broken piece.

## Recomputation

HyVoltz does **not** re-calculate the graph every tick. It caches the list of nodes. The pathfinding only runs when:
*   A node is attached.
*   A node is detached.
*   A side configuration changes (if you force an update).

This is why it is important to correctly use the methods `attach()` and `detach()`.

## Side Compatibility

A connection is valid if and only if **BOTH** nodes agree.
*   Node A must be able to **Output** to direction D.
*   Node B must be able to **Input** from direction opposite(D).

If only one side agrees, they are **not** connected, and they will belong to separate networks even if physically touching.
