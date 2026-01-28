# Persistence & Lifecycle

## What HyVoltz Persists

**NOTHING.**

HyVoltz is a runtime-only simulation layer. It assumes that the Hytale world (blocks, entities) is the source of truth.

*   It does **not** save the list of networks to disk.
*   It does **not** save which blocks are connected.
*   It does **not** save the energy in your cables (wires are instant-transfer). This could be subject to change but for now it seems to be the best option.

## What YOU Must Persist

You are responsible for saving the state of your individual nodes.

### 1. Stored Energy
If you have a battery with 500 energy, you must save that `500` to your Block Component or any other way you store data.
*   **On World Save**: Write `storedEnergy` to disk.
*   **On World Load**: Read `storedEnergy` back into your `ElectricNode`.

### 2. Side Configuration
If your machine can be rotated or configured with a wrench, you must save its `NodeSideConfig`.

## World Reloading

When the world loads:
1.  Your block components initialize.
2.  You load your energy values from NBT.
3.  You call `attach()` in your startup logic.
4.  HyVoltz treats these as "new" connections and rebuilds the network graph from scratch.

This ensures that if a block was removed via WorldEdit or an external tool while the server was offline, the network won't try to connect to a phantom block. 
