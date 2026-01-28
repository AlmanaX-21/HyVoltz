package me.almana.hyvoltz.core.tick;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic tick source for tests.
 */
public class ManualTickSource implements TickSource {
    private long currentTick = 0;
    private final List<TickListener> listeners = new ArrayList<>();

    @Override
    public void register(TickListener listener) {
        listeners.add(listener);
    }

    @Override
    public long getCurrentTick() {
        return currentTick;
    }

    public void advance() {
        currentTick++;
        for (TickListener listener : listeners) {
            listener.onTick(currentTick);
        }
    }
}
