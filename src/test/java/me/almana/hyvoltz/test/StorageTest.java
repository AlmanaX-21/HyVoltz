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
 * Tests for network storage what players will know as batteries.
 */
public class StorageTest extends HeadlessTestHarness {

    private ElectricNetwork network;

    @BeforeEach
    public void setupNetwork() {
        network = new ElectricNetwork();
        tickSource.register(network);
    }

    @Test
    public void testDischargeSupplementsSupply() {
        // Supply: 10. Demand: 50. Battery has 100.
        // Expect: Consumer gets 50 (10 prod + 40 batt). Battery has 60.

        network.addNode(new TestProducer(10));
        TestConsumer consumer = new TestConsumer(50);
        network.addNode(consumer);
        TestStorage battery = new TestStorage(100, 1000); // 100 stored, 1000 cap
        network.addNode(battery);

        tickSource.advance();

        assertEquals(50, consumer.getLastReceived(), "Consumer should be fully satisfied");
        assertEquals(60, battery.getStoredEnergy(), "Battery should have discharged 40");
    }

    @Test
    public void testChargeAbsorbsExcess() {
        // Supply: 100. Demand: 50. Battery has 0.
        // Expect: Consumer gets 50. Battery gets 50.

        network.addNode(new TestProducer(100));
        TestConsumer consumer = new TestConsumer(50);
        network.addNode(consumer);
        TestStorage battery = new TestStorage(0, 1000);
        network.addNode(battery);

        tickSource.advance();

        assertEquals(50, consumer.getLastReceived(), "Consumer should get requested");
        assertEquals(50, battery.getStoredEnergy(), "Battery should absorb excess");
    }

    @Test
    public void testCapacityLimit() {
        // Supply: 100. Demand: 0. Battery has 90, cap 100.
        // Expect: Battery gets 10, total 100. Excess 90 lost.

        network.addNode(new TestProducer(100));
        TestStorage battery = new TestStorage(90, 100);
        network.addNode(battery);

        tickSource.advance();

        assertEquals(100, battery.getStoredEnergy(), "Battery should be full");
    }

    @Test
    public void testEmptyLimit() {
        // Supply: 0. Demand: 100. Battery has 50.
        // Expect: Consumer gets 50. Battery has 0.

        TestConsumer consumer = new TestConsumer(100);
        network.addNode(consumer);
        TestStorage battery = new TestStorage(50, 1000);
        network.addNode(battery);

        tickSource.advance();

        assertEquals(50, consumer.getLastReceived(), "Consumer gets partial from battery");
        assertEquals(0, battery.getStoredEnergy(), "Battery empty");
    }

    // -- Test Stubs --
    // Reusing standard stubs or similar logic
    private static class TestStorage implements ElectricStorage {
        private final UUID id = UUID.randomUUID();
        private long stored;
        private final long capacity;

        public TestStorage(long stored, long capacity) {
            this.stored = stored;
            this.capacity = capacity;
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
        private final AtomicLong lastReceived = new AtomicLong(0);

        public TestConsumer(long request) {
            this.request = request;
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
    }
}
