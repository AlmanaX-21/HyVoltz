package me.almana.hyvoltz.test;

import me.almana.hyvoltz.core.node.ElectricConsumer;
import me.almana.hyvoltz.core.node.ElectricNode;
import me.almana.hyvoltz.hytale.HyVoltzEngine;
import me.almana.hyvoltz.hytale.WorldEnergyManager;
import me.almana.hyvoltz.hytale.api.HyVoltzNodeComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.hypixel.hytale.protocol.BlockPosition;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DiscoveryTest {

    @AfterEach
    public void tearDown() {
        // Ideally reset engine
    }

    private ElectricNode createNode() {
        return new ElectricConsumer() {
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
    }

    @Test
    public void testMergeNetworks() {
        UUID worldId = UUID.randomUUID();
        HyVoltzEngine.getInstance().onWorldLoad(worldId);
        WorldEnergyManager manager = HyVoltzEngine.getInstance().getManager(worldId);

        HyVoltzNodeComponent nodeA = new HyVoltzNodeComponent(createNode());
        HyVoltzNodeComponent nodeB = new HyVoltzNodeComponent(createNode());

        nodeA.attach(worldId, new BlockPosition(0, 0, 0));
        nodeB.attach(worldId, new BlockPosition(2, 0, 0));

        assertTrue(nodeA.isAttached());
        assertTrue(nodeB.isAttached());

        HyVoltzNodeComponent connector = new HyVoltzNodeComponent(createNode());
        connector.attach(worldId, new BlockPosition(1, 0, 0));

        assertTrue(connector.isAttached());
    }

    @Test
    public void testSplitNetworks() {
        UUID worldId = UUID.randomUUID();
        HyVoltzEngine.getInstance().onWorldLoad(worldId);

        HyVoltzNodeComponent nodeA = new HyVoltzNodeComponent(createNode());
        HyVoltzNodeComponent nodeB = new HyVoltzNodeComponent(createNode());
        HyVoltzNodeComponent nodeC = new HyVoltzNodeComponent(createNode());

        nodeA.attach(worldId, new BlockPosition(0, 0, 0));
        nodeB.attach(worldId, new BlockPosition(1, 0, 0));
        nodeC.attach(worldId, new BlockPosition(2, 0, 0));

        nodeB.detach();

        assertTrue(nodeA.isAttached());
        assertTrue(nodeC.isAttached());
        assertFalse(nodeB.isAttached());
    }
}
