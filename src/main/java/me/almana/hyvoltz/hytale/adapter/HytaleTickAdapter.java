package me.almana.hyvoltz.hytale.adapter;

import com.hypixel.hytale.server.core.HytaleServer;

import java.util.concurrent.TimeUnit;

import me.almana.hyvoltz.api.HyVoltzAPI;

/**
 * Adapter to hook Hytale server ticks.
 * Uses the server's scheduled executor to run at 20 TPS (50ms).
 */
public class HytaleTickAdapter {

    public void startTicking() {
        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::onServerTick, 0, 50, TimeUnit.MILLISECONDS);
    }

    private void onServerTick() {
        try {
            HyVoltzAPI.engine().onServerTick();
        } catch (Exception e) {
            // Log error but don't crash scheduler
            e.printStackTrace();
        }
    }
}
