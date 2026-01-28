# Best Practices

## 1. Respect the Simulation Limitations
HyVoltz is fast, but it is not magic.
*   **Avoid creating "Megastructures"**: Don't encourage players to build 10,000-block contiguous cables. It hurts graph traversal.
*   **Limit Network Churn**: Don't rapidly attach/detach nodes every tick. This forces full graph re-computations.

## 2. Safe Detachment
Always use `try-finally` blocks or `onDetached` callbacks in your Block Component to ensure `hyVoltzNode.detach()` is called.
If you crash before detaching, the network map will leak memory and logic until the server restarts.

## 3. Inter-Mod Compatibility
Since HyVoltz is a **Platform Mod**, all mods on the server share the same energy network.
*   **Recommendation**: Use standard HyVoltz interfaces (`ElectricProducer`, etc.) directly. You can interface with any block from any mod that uses HyVoltz.

## 4. Versioning
Depend on a specific version of HyVoltz in your plugin configuration. If HyVoltz breaks API compatibility (major version bump), your mod should fail to load cleanly rather than causing runtime errors.

