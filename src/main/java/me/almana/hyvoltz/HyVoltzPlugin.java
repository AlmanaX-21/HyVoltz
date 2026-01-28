package me.almana.hyvoltz;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import me.almana.hyvoltz.HyVoltzEngine;
import me.almana.hyvoltz.api.HyVoltzAPI;
import me.almana.hyvoltz.hytale.adapter.HytaleTickAdapter;
import me.almana.hyvoltz.hytale.adapter.HytaleWorldAdapter;

public class HyVoltzPlugin extends JavaPlugin {

    private final HyVoltzEngine engine;
    private final HytaleTickAdapter tickAdapter;
    private final HytaleWorldAdapter worldAdapter;
    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public HyVoltzPlugin(JavaPluginInit init) {
        super(init);
        // Initialize the engine exactly once
        this.engine = new HyVoltzEngine();
        HyVoltzAPI.register(this.engine);

        this.tickAdapter = new HytaleTickAdapter();
        this.worldAdapter = new HytaleWorldAdapter();
    }

    @Override
    protected void start0() {
        super.start0();

        // Start server ticking
        tickAdapter.startTicking();

        // Wire up world lifecycle events
        EventRegistry eventRegistry = getEventRegistry();
        eventRegistry.registerGlobal(AddWorldEvent.class, worldAdapter::onWorldLoad);
        eventRegistry.registerGlobal(RemoveWorldEvent.class, worldAdapter::onWorldUnload);

        logger.atInfo().log("HyVoltz Platform Active - version 1.0.0");
    }
}
