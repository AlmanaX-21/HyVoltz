package me.almana.hyvoltz.test;

import com.hypixel.hytale.protocol.BlockPosition;
import me.almana.hyvoltz.core.node.ElectricConsumer;
import me.almana.hyvoltz.core.node.ElectricNode;
import me.almana.hyvoltz.hytale.HyVoltzEngine;
import me.almana.hyvoltz.hytale.WorldEnergyManager;
import me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationTest {

    @AfterEach
    public void tearDown() {
        // No easy way to reset singleton, but we can rely on new world IDs for
        // isolation
    }

    @Test
    public void testAttach() {
        UUID worldId = UUID.randomUUID();
        // Simulate world load
        HyVoltzEngine.getInstance().onWorldLoad(worldId);
        WorldEnergyManager manager = HyVoltzEngine.getInstance().getManager(worldId);

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

        component.attach(worldId, new BlockPosition(0, 0, 0));

        assertTrue(component.isAttached(), "Component should be attached");
    }

    @Test
    public void testDetach() {
        UUID worldId = UUID.randomUUID();
        HyVoltzEngine.getInstance().onWorldLoad(worldId);
        WorldEnergyManager manager = HyVoltzEngine.getInstance().getManager(worldId);

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

        component.attach(worldId, new BlockPosition(0, 0, 0));
        component.detach();

        assertFalse(component.isAttached(), "Component should be detached");
    }

    @Test
    public void testIdempotentAttach() {
        UUID worldId = UUID.randomUUID();
        HyVoltzEngine.getInstance().onWorldLoad(worldId);
        WorldEnergyManager manager = HyVoltzEngine.getInstance().getManager(worldId);

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

        component.attach(worldId, new BlockPosition(0, 0, 0));
        component.attach(worldId, new BlockPosition(0, 0, 0)); // Double attach

        assertTrue(component.isAttached(), "Component should be attached");
    }

    @Test
    public void testSafeDetachAfterWorldUnload() {
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

        component.attach(worldId, new BlockPosition(0, 0, 0));

        // Unload world
        HyVoltzEngine.getInstance().onWorldUnload(worldId);

        // Detach should be safe
        component.detach();

        assertFalse(component.isAttached(), "Component should be detached");
    }
}
