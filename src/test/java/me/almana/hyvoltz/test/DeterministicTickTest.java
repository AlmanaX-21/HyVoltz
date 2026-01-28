package me.almana.hyvoltz.test;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies deterministic tick advancement.
 */
public class DeterministicTickTest extends HeadlessTestHarness {

    @Test
    public void testManualAdvancement() {
        AtomicInteger ticksReceived = new AtomicInteger(0);

        tickSource.register(tick -> {
            ticksReceived.incrementAndGet();
            assertEquals(1L, tick, "Tick count should be main");
        });

        assertEquals(0L, tickSource.getCurrentTick(), "Start at tick 0");

        tickSource.advance();

        assertEquals(1L, tickSource.getCurrentTick(), "Should be at tick 1");
        assertEquals(1, ticksReceived.get(), "Listener invoked once");
    }

    @Test
    public void testNoAutoAdvancement() {
        assertEquals(0L, tickSource.getCurrentTick());
        // Do nothing
        assertEquals(0L, tickSource.getCurrentTick());
    }
}
