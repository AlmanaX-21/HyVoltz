package me.almana.hyvoltz.api;

import com.hypixel.hytale.protocol.BlockPosition;
import me.almana.hyvoltz.HyVoltzEngine;
import me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent;

import java.util.UUID;

public final class HyVoltzAPI {
    private static HyVoltzEngine engine;

    private HyVoltzAPI() {
    }

    public static HyVoltzEngine engine() {
        if (engine == null) {
            throw new IllegalStateException("HyVoltz is required but not loaded. Ensure 'hyvoltz' is installed.");
        }
        return engine;
    }

    public static void register(HyVoltzEngine instance) {
        if (engine != null) {
            throw new IllegalStateException("HyVoltz engine already registered");
        }
        engine = instance;
    }

    public static void attachNode(HyVoltzNodeComponent component, UUID worldId, BlockPosition position) {
        if (component == null)
            throw new IllegalArgumentException("Component cannot be null");
        if (worldId == null)
            throw new IllegalArgumentException("World UUID cannot be null");
        if (position == null)
            throw new IllegalArgumentException("Position cannot be null");

        engine().requestAttach(component, worldId);
    }

    public static void detachNode(HyVoltzNodeComponent component, UUID worldId) {
        if (component == null)
            throw new IllegalArgumentException("Component cannot be null");
        if (worldId == null)
            throw new IllegalArgumentException("World UUID cannot be null");

        engine().cancelAttach(component, worldId);
    }

    // WAIT. If I change HyVoltzNodeComponent to call this, it has the worldID.
    // If a mod calls this, they better know the worldID.
    // Let's stick to the signature `detachNode(HyVoltzNodeComponent component, UUID
    // worldId)`.
    // Wait, the Plan said "Add ... for detach". It didn't specify arguments.

    // Re-reading `HyVoltzNodeComponent.detach()`:
    // It gets `this.worldId.get()`.
    // So `HyVoltzNodeComponent` knows.

    // I will implement `detachNode(HyVoltzNodeComponent component, UUID worldId)`.
}
