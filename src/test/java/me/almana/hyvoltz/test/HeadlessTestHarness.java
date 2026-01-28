package me.almana.hyvoltz.test;

import me.almana.hyvoltz.core.tick.ManualTickSource;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for headless tests.
 */
public abstract class HeadlessTestHarness {
    protected ManualTickSource tickSource;

    @BeforeEach
    public void setup() {
        tickSource = new ManualTickSource();
    }
}
