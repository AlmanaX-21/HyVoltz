package me.almana.hyvoltz.test;

import me.almana.hyvoltz.api.HyVoltzAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyEnforcementTest {

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        TestUtils.resetEngine();
    }

    @AfterEach
    public void tearDown() {
        TestUtils.resetEngine();
    }

    @Test
    public void testApiThrowsWithoutEngine() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            HyVoltzAPI.engine();
        });

        assertEquals("HyVoltz is required but not loaded. Ensure 'hyvoltz' is installed.", exception.getMessage());
    }

    @Test
    public void testAttachFailsWithMissingEngine() {
        // Valid inputs
        me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent component = new me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent(
                new me.almana.hyvoltz.core.node.ElectricConsumer() {
                    public java.util.UUID getId() {
                        return java.util.UUID.randomUUID();
                    }

                    public long getRequestedInput() {
                        return 10;
                    }

                    public void receivePower(long a) {
                    }
                });
        java.util.UUID worldId = java.util.UUID.randomUUID();
        com.hypixel.hytale.protocol.BlockPosition pos = new com.hypixel.hytale.protocol.BlockPosition(0, 0, 0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            HyVoltzAPI.attachNode(component, worldId, pos);
        });

        assertEquals("HyVoltz is required but not loaded. Ensure 'hyvoltz' is installed.", exception.getMessage());
    }
}
