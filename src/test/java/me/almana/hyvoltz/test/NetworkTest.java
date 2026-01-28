package me.almana.hyvoltz.test;

import me.almana.hyvoltz.core.network.ElectricNetwork;
import me.almana.hyvoltz.core.node.ElectricNode;
import me.almana.hyvoltz.core.node.ElectricNodeType;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ElectricNetwork container.
 */
public class NetworkTest extends HeadlessTestHarness {

    @Test
    public void testAddRemoveNode() {
        ElectricNetwork network = new ElectricNetwork();
        ElectricNode node = new TestNode(ElectricNodeType.PRODUCER);

        network.addNode(node);
        assertTrue(network.getNodes().contains(node), "Network should contain added node");

        network.removeNode(node);
        assertFalse(network.getNodes().contains(node), "Network should not contain removed node");
    }

    @Test
    public void testImmutableAccess() {
        ElectricNetwork network = new ElectricNetwork();
        Collection<ElectricNode> nodes = network.getNodes();

        assertThrows(UnsupportedOperationException.class, () -> {
            nodes.add(new TestNode(ElectricNodeType.CONSUMER));
        }, "Nodes collection should be immutable");
    }

    @Test
    public void testNetworkIsolation() {
        ElectricNetwork net1 = new ElectricNetwork();
        ElectricNetwork net2 = new ElectricNetwork();
        ElectricNode node = new TestNode(ElectricNodeType.CONNECTOR);

        net1.addNode(node);

        assertTrue(net1.getNodes().contains(node));
        assertFalse(net2.getNodes().contains(node));
    }

    // Simple implementation for testing
    private static class TestNode implements ElectricNode {
        private final UUID id = UUID.randomUUID();
        private final ElectricNodeType type;

        public TestNode(ElectricNodeType type) {
            this.type = type;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public ElectricNodeType getType() {
            return type;
        }
    }
}
