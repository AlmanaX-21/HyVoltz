package me.almana.hyvoltz.test;

import me.almana.hyvoltz.core.network.ElectricNetwork;
import me.almana.hyvoltz.core.node.ElectricConsumer;
import me.almana.hyvoltz.core.node.ElectricProducer;
import me.almana.hyvoltz.core.node.ElectricStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for priority-based resolution.
 */
public class PriorityTest extends HeadlessTestHarness {

    private ElectricNetwork network;

    @BeforeEach
    public void setupNetwork() {
        network = new ElectricNetwork();
        tickSource.register(network);
    }

    @Test
    public void testConsumerPriority() {
        // Supply: 100
        // High Prio Demand: 100
        // Low Prio Demand: 100
        // Expect: High gets 100, Low gets 0.

        network.addNode(new TestProducer(100));
        TestConsumer high = new TestConsumer(100, 10);
        TestConsumer low = new TestConsumer(100, 1);
        network.addNode(high);
        network.addNode(low);

        tickSource.advance();

        assertEquals(100, high.getLastReceived(), "High priority should be satisfied first");
        assertEquals(0, low.getLastReceived(), "Low priority should get remainder (0)");
    }

    @Test
    public void testTieredDistribution() {
        // Supply: 150
        // High (10): 100
        // Mid (5): 100
        // Low (1): 100
        // Expect: High 100, Mid 50, Low 0.

        network.addNode(new TestProducer(150));
        TestConsumer high = new TestConsumer(100, 10);
        TestConsumer mid = new TestConsumer(100, 5);
        TestConsumer low = new TestConsumer(100, 1);
        network.addNode(high);
        network.addNode(mid);
        network.addNode(low);

        tickSource.advance();

        assertEquals(100, high.getLastReceived());
        assertEquals(50, mid.getLastReceived());
        assertEquals(0, low.getLastReceived());
    }

    @Test
    public void testEqualPriority() {
        // Supply: 100
        // C1 (5): 100
        // C2 (5): 100
        // Expect: 50 each (Proportional split within tier)

        network.addNode(new TestProducer(100));
        TestConsumer c1 = new TestConsumer(100, 5);
        TestConsumer c2 = new TestConsumer(100, 5);
        network.addNode(c1);
        network.addNode(c2);

        tickSource.advance();

        assertEquals(50, c1.getLastReceived());
        assertEquals(50, c2.getLastReceived());
    }

    @Test
    public void testStorageDischargeOrder() {
        // Demand: 100. Supply: 0.
        // Batt High (10): 100 stored
        // Batt Low (1): 100 stored
        // Expect: High drains 100. Low drains 0.

        network.addNode(new TestConsumer(100, 1));
        TestStorage high = new TestStorage(100, 1000, 10);
        TestStorage low = new TestStorage(100, 1000, 1);
        network.addNode(high);
        network.addNode(low);

        tickSource.advance();

        assertEquals(0, high.getStoredEnergy(), "High priority battery should drain first");
        assertEquals(100, low.getStoredEnergy(), "Low priority battery should be spared");
    }

    @Test
    public void testStorageChargeOrder() {
        // Supply: 100. Demand: 0.
        // Batt High (10): 0 stored
        // Batt Low (1): 0 stored
        // Expect: Low fills 100 (charges first). High fills 0.

        network.addNode(new TestProducer(100));
        TestStorage high = new TestStorage(0, 1000, 10);
        TestStorage low = new TestStorage(0, 1000, 1);
        network.addNode(high);
        network.addNode(low);

        tickSource.advance();

        assertEquals(100, low.getStoredEnergy(), "Low priority battery should charge first");
        assertEquals(0, high.getStoredEnergy(), "High priority battery should charge last");
    }

    // -- Test Stubs --
    private static class TestProducer implements ElectricProducer {
        private final UUID id = UUID.randomUUID();
        private final long output;

        public TestProducer(long output) {
            this.output = output;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public long getOutputPerTick() {
            return output;
        }
    }

    private static class TestConsumer implements ElectricConsumer {
        private final UUID id = UUID.randomUUID();
        private final long request;
        private final int priority;
        private final AtomicLong lastReceived = new AtomicLong(0);

        public TestConsumer(long request, int priority) {
            this.request = request;
            this.priority = priority;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public long getRequestedInput() {
            return request;
        }

        @Override
        public void receivePower(long amount) {
            lastReceived.set(amount);
        }

        public long getLastReceived() {
            return lastReceived.get();
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }

    private static class TestStorage implements ElectricStorage {
        private final UUID id = UUID.randomUUID();
        private long stored;
        private final long capacity;
        private final int priority;

        public TestStorage(long stored, long capacity, int priority) {
            this.stored = stored;
            this.capacity = capacity;
            this.priority = priority;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public long getStoredEnergy() {
            return stored;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public long insertEnergy(long amount) {
            long space = capacity - stored;
            long toAdd = Math.min(space, amount);
            stored += toAdd;
            return toAdd;
        }

        @Override
        public long extractEnergy(long amount) {
            long toExtract = Math.min(stored, amount);
            stored -= toExtract;
            return toExtract;
        }
    }
}
