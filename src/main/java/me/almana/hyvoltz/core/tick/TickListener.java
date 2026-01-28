package me.almana.hyvoltz.core.tick;

/**
 * Functional interface for tick events.
 */
@FunctionalInterface
public interface TickListener {
    void onTick(long tick);
}
