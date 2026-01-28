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
 * Tests for throughput limits.
 */
public class ThroughputTest extends HeadlessTestHarness {

    private ElectricNetwork network;

    @BeforeEach
    public void setupNetwork() {
        network = new ElectricNetwork();
        tickSource.register(network);
    }

    @Test
    public void testConsumerInputLimit() {
        // Supply: 100
        // Consumer Requests: 100, Limit: 10
        // Expect: Consumer gets 10.

        network.addNode(new TestProducer(100));
        TestConsumer consumer = new TestConsumer(100, 10);
        network.addNode(consumer);

        tickSource.advance();

        assertEquals(10, consumer.getLastReceived(), "Consumer input should be clamped to limit");
    }

    @Test
    public void testStorageChargeLimit() {
        // Supply: 100
        // Storage: 0 stored, 1000 cap, 10 limit
        // Expect: Storage charges 10 (not 100).

        network.addNode(new TestProducer(100));
        TestStorage storage = new TestStorage(0, 1000, 10, 1000); // 10 charge limit
        network.addNode(storage);

        tickSource.advance();

        assertEquals(10, storage.getStoredEnergy(), "Storage charge should be clamped");
    }

    @Test
    public void testStorageDischargeLimit() {
        // Demand: 100
        // Storage: 100 stored, 1000 cap, 1000 charge limit, 10 discharge limit
        // Expect: Consumer gets 10.

        TestConsumer consumer = new TestConsumer(100, 100);
        network.addNode(consumer);
        TestStorage storage = new TestStorage(100, 1000, 1000, 10); // 10 discharge limit
        network.addNode(storage);

        tickSource.advance();

        assertEquals(10, consumer.getLastReceived(), "Consumer should only get 10 from battery");
        assertEquals(90, storage.getStoredEnergy());
    }

    @Test
    public void testPriorityWithLimits() {
        // Supply: 100
        // High Prio Consumer: Request 100, Limit 10
        // Low Prio Consumer: Request 100, Limit 100
        // Expect: High gets 10. Available 90. Low gets 90.

        network.addNode(new TestProducer(100));
        TestConsumer high = new TestConsumer(100, 10, 10); // requests 100, limit 10, prio 10
        TestConsumer low = new TestConsumer(100, 100, 1); // requests 100, limit 100, prio 1
        network.addNode(high);
        network.addNode(low);

        tickSource.advance();

        assertEquals(10, high.getLastReceived(), "High prio clamped");
        assertEquals(90, low.getLastReceived(), "Low prio gets remainder");
    }

    // -- Stubs --
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
        private final long maxInput;
        private final int priority;
        private final AtomicLong lastReceived = new AtomicLong(0);

        public TestConsumer(long request, long maxInput) {
            this(request, maxInput, 0);
        }

        public TestConsumer(long request, long maxInput, int priority) {
            this.request = request;
            this.maxInput = maxInput;
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
        public long getMaxInputPerTick() {
            return maxInput;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void receivePower(long amount) {
            lastReceived.set(amount);
        }

        public long getLastReceived() {
            return lastReceived.get();
        }
    }

    private static class TestStorage implements ElectricStorage {
        private final UUID id = UUID.randomUUID();
        private long stored;
        private final long capacity;
        private final long maxInput;
        private final long maxOutput;

        public TestStorage(long stored, long capacity, long maxInput, long maxOutput) {
            this.stored = stored;
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
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
        public long getMaxInputPerTick() {
            return maxInput;
        }

        @Override
        public long getMaxOutputPerTick() {
            return maxOutput;
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
