package me.almana.hyvoltz.core.tick;

/**
 * Interface for observing ticks.
 */
public interface TickSource {
    void register(TickListener listener);

    long getCurrentTick();
}
