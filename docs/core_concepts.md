# Core Concepts

## Nodes vs Networks

A **Node** is a single block (or entity) that wants to participate in the power system.
A **Network** is a temporary runtime graph of connected nodes.

*   You create **Nodes** (via `HyVoltzNodeComponent`).
*   HyVoltz automatically manages **Networks**.

You never interact with the "Network" object directly. You just place nodes next to each other. HyVoltz automatically discovers neighbors, merges networks when connected, and splits them when cables are broken.

## Node Types

There are four functional roles a node can play:

1.  **Producer**: Generates new energy (e.g., Solar Panel).
2.  **Consumer**: Uses energy (e.g., Electric Furnace).
3.  **Storage**: Buffers energy (e.g., Battery Box).
4.  **Connector**: Just passes power through (e.g., Wire).

## Throughput vs Capacity

> Configurable values for you
*   **Capacity**: How much energy a node can hold. Only applies to Storage and buffered Consumers.
*   **Throughput**: How much energy can flow *in* or *out* of a node per tick. 

HyVoltz solves the entire network at once. If a battery has a 50/tick output limit, it will never contribute more than 50 energy to the network in a single tick, even if 1000 consumers are asking for it.

## Priorities

Every consumer and storage node has a `Priority`. You can mostly leave it at default(0) unless you have some machine that can explode or cause damage
*   **High Priority (Positive)**: Served first. Critical machines.
*   **Low Priority (Negative)**: Served last. Chargers or less important systems.

By default, everything is Priority 0.

## Zero Voltage

HyVoltz is a **voltage-less** system. It models energy as a fluid that instantly equalizes across the network.
*   There is no "High Voltage" or "Low Voltage".
*   You cannot blow up machines by giving them "too much" power (they just take what they ask for).
*   Distance does not reduce power (no attenuation).
