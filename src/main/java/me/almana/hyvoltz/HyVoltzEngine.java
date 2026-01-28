package me.almana.hyvoltz;

import me.almana.hyvoltz.hytale.WorldEnergyManager;
import me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core engine for HyVoltz integration. Manages lifecycle of world energy
 * managers.
 */
public class HyVoltzEngine {
    private static boolean instantiated = false;
    private final Map<UUID, WorldEnergyManager> worldManagers = new ConcurrentHashMap<>();

    private final Map<UUID, Set<HyVoltzNodeComponent>> pendingAttachments = new ConcurrentHashMap<>();
    private final Map<UUID, Set<HyVoltzNodeComponent>> attachedComponents = new ConcurrentHashMap<>();

    HyVoltzEngine() {
        if (instantiated) {
            throw new IllegalStateException("HyVoltzEngine initialized twice!");
        }
        instantiated = true;
    }

    public void onWorldLoad(UUID worldId) {
        worldManagers.computeIfAbsent(worldId, WorldEnergyManager::new);

        Set<HyVoltzNodeComponent> pending = pendingAttachments.remove(worldId);
        if (pending != null) {
            for (HyVoltzNodeComponent component : pending) {
                requestAttach(component, worldId);
            }
        }
    }

    public void onWorldUnload(UUID worldId) {
        worldManagers.remove(worldId);

        // Move all currently attached components to pending
        Set<HyVoltzNodeComponent> attached = attachedComponents.remove(worldId);
        if (attached != null) {
            Set<HyVoltzNodeComponent> pending = pendingAttachments
                    .computeIfAbsent(worldId, k -> ConcurrentHashMap.newKeySet());
            for (HyVoltzNodeComponent component : attached) {
                component.internalOnDetach(); // Force physical detach
                pending.add(component); // Re-queue
            }
        }
    }

    public void requestAttach(HyVoltzNodeComponent component, UUID worldId) {
        WorldEnergyManager manager = getManager(worldId);
        if (manager != null) {
            // World is loaded, attach immediately
            manager.registerNode(component, component.getPosition());
            component.internalOnAttach();
            attachedComponents.computeIfAbsent(worldId, k -> ConcurrentHashMap.newKeySet()).add(component);
        } else {
            // World not loaded, defer
            pendingAttachments.computeIfAbsent(worldId, k -> ConcurrentHashMap.newKeySet()).add(component);
        }
    }

    public void cancelAttach(HyVoltzNodeComponent component, UUID worldId) {
        // Remove from pending
        Set<HyVoltzNodeComponent> pending = pendingAttachments.get(worldId);
        if (pending != null) {
            pending.remove(component);
        }

        // Remove from attached
        Set<HyVoltzNodeComponent> attached = attachedComponents.get(worldId);
        if (attached != null && attached.remove(component)) {
            WorldEnergyManager manager = getManager(worldId);
            if (manager != null) {
                manager.unregisterNode(component.getNode());
            }
            component.internalOnDetach();
        }
    }

    public void onServerTick() {
        for (WorldEnergyManager manager : worldManagers.values()) {
            manager.tick();
        }
    }

    public WorldEnergyManager getManager(UUID worldId) {
        return worldManagers.get(worldId);
    }
}
