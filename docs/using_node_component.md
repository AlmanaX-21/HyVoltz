# Using HyVoltzNodeComponent

The `HyVoltzNodeComponent` is the bridge between the Hytale World and the HyVoltz math and graph engine.

## What it Represents

This component represents the "socket" or "terminal" of your machine. It holds:
1.  The `ElectricNode` (the math object).
2.  The `NodeSideConfig` (which sides connect).
3.  The connection state.

## Lifecycle

### Placement (Attachment)
When your block is placed or the entity spawns:
1.  Check `world.isLoaded(pos)`.
2.  Call `component.attach(worldId, pos)`.

If the world is not fully loaded, HyVoltz will **defer** the attachment until the chunk is ready. You do not need to wait; just call `attach` immediately. This developer friendly decision was to save headaches and unnecessary checks.

### Destruction (Detachment)
When your block is broken or entity despawns:
1.  Call `component.detach()`. This is important.

Failure to detach leaves "ghost nodes" in the network, which causes power to flow into the void or prevents networks from splitting correctly.

## Side Configuration

By default, a node is **Omnidirectional** (connects on all 6 sides).
You can restrict this:

```java
// Connects only on UP and DOWN
component.setSideConfig(direction -> 
    direction.equals(Vector3i.UP) || direction.equals(Vector3i.DOWN)
);
```

This is dynamic. You can change it at runtime (e.g., rotating a machine with a wrench), but you must force a network update if you do (usually by re-attaching or triggering a neighbor update). In future, I will add an update network system as well.
