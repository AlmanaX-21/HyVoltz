# HyVoltz

**HyVoltz** is a high-performance energy network library for Hytale that delivers a robust and efficient power system infrastructure.

## Key Features
- **Graph-Based Networks**: Networks process updates only when required, improving efficiency.
- **Strict Directionality**: `NodeSideConfig` enforces input/output rules for blocks.
- **Performance First**: My goal was to ensure performance and hence no polling, optimized adjacency lookups, and manual tick sources.
- **Headless Testing**: Fully testable API without requiring a running Hytale server.

## Modules
- **`hyvoltz-core`**: The platform-agnostic energy logic (Networks, Nodes, Consumers, Producers).
- **`hyvoltz-hytale`**: The Hytale integration layer (Block Components, World Management, Adjacency).

## Usage
Implement `ElectricNode` (or `ElectricConsumer`/`ElectricProducer`) and wrap it in a `HyVoltzNodeComponent`. Attach it to the world using `component.attach(worldId, position)`. Documentation to come soon.