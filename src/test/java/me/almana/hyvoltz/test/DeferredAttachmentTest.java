package me.almana.hyvoltz.test;

import me.almana.hyvoltz.core.node.ElectricConsumer;
import me.almana.hyvoltz.core.node.ElectricNode;
import me.almana.hyvoltz.hytale.HyVoltzEngine;
import me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DeferredAttachmentTest {

    @AfterEach
    public void tearDown() {
        // ideally reset engine
    }

    @Test
    public void testAttachBeforeWorldLoad() {
        UUID worldId = UUID.randomUUID();
        // World NOT loaded

        ElectricNode node = new ElectricConsumer() {
            private final UUID id = UUID.randomUUID();

            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public long getRequestedInput() {
                return 10;
            }

            @Override
            public void receivePower(long amount) {
            }
        };
        HyVoltzNodeComponent component = new HyVoltzNodeComponent(node);

        component.attach(worldId, new com.hypixel.hytale.protocol.BlockPosition(0, 0, 0));

        // Should not be physically attached yet
        assertFalse(component.isAttached(), "Component should NOT be actively attached before world load");

        // Now load world
        HyVoltzEngine.getInstance().onWorldLoad(worldId);

        // Should now be attached
        assertTrue(component.isAttached(), "Component SHOULD be attached after world load");
    }

    @Test
    public void testUnloadRequeues() {
        UUID worldId = UUID.randomUUID();
        HyVoltzEngine.getInstance().onWorldLoad(worldId);

        ElectricNode node = new ElectricConsumer() {
            private final UUID id = UUID.randomUUID();

            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public long getRequestedInput() {
                return 10;
            }

            @Override
            public void receivePower(long amount) {
            }
        };
        HyVoltzNodeComponent component = new HyVoltzNodeComponent(node);

        component.attach(worldId, new com.hypixel.hytale.protocol.BlockPosition(0, 0, 0));
        assertTrue(component.isAttached());

        // Unload world
        HyVoltzEngine.getInstance().onWorldUnload(worldId);

        // Should be physically detached
        assertFalse(component.isAttached(), "Component should safely detach on world unload");

        // Reload world
        HyVoltzEngine.getInstance().onWorldLoad(worldId);

        // Should re-attach
        assertTrue(component.isAttached(), "Component should re-attach on reload");
    }

    @Test
    public void testExplicitDetachCancelsPending() {
        UUID worldId = UUID.randomUUID();
        // World NOT loaded

        ElectricNode node = new ElectricConsumer() {
            private final UUID id = UUID.randomUUID();

            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public long getRequestedInput() {
                return 10;
            }

            @Override
            public void receivePower(long amount) {
            }
        };
        HyVoltzNodeComponent component = new HyVoltzNodeComponent(node);

        component.attach(worldId, new com.hypixel.hytale.protocol.BlockPosition(0, 0, 0));
        component.detach(); // Change mind

        // Load world
        HyVoltzEngine.getInstance().onWorldLoad(worldId);

        // Should NOT attach
        assertFalse(component.isAttached(), "Component should clearly not attach if explicitly detached while pending");
    }
}
