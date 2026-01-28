package me.almana.hyvoltz.hytale.adapter;

import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;

import me.almana.hyvoltz.api.HyVoltzAPI;

/**
 * Adapter to hook Hytale world lifecycle events.
 * Intended to be registered to the EventRegistry.
 */
public class HytaleWorldAdapter {

    public void onWorldLoad(AddWorldEvent event) {
        HyVoltzAPI.engine().onWorldLoad(event.getWorld().getWorldConfig().getUuid());
    }

    public void onWorldUnload(RemoveWorldEvent event) {
        HyVoltzAPI.engine().onWorldUnload(event.getWorld().getWorldConfig().getUuid());
    }
}
