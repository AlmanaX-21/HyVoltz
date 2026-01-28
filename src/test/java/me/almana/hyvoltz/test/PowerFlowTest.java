package me.almana.hyvoltz.test;

import me.almana.hyvoltz.core.network.ElectricNetwork;
import me.almana.hyvoltz.core.node.ElectricConsumer;
import me.almana.hyvoltz.core.node.ElectricProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies power flow logic.
 */
public class PowerFlowTest extends HeadlessTestHarness {

    private ElectricNetwork network;

    @BeforeEach
    public void setupNetwork() {
        network = new ElectricNetwork();
        tickSource.register(network);
    }

    @Test
    public void testSufficientSupply() {
        // Supply: 100, Demand: 50 -> Received: 50
        network.addNode(new TestProducer(100));
        TestConsumer consumer = new TestConsumer(50);
        network.addNode(consumer);

        tickSource.advance();

        assertEquals(50, consumer.getLastReceived(), "Consumer should receive full request");
    }

    @Test
    public void testProportionalSplit() {
        // Supply: 100
        // Consumer 1: 100 (needs 50%)
        // Consumer 2: 100 (needs 50%)
        // Total Demand: 200. Supply/Demand = 0.5.
        // C1 gets 50, C2 gets 50.

        network.addNode(new TestProducer(100));
        TestConsumer c1 = new TestConsumer(100);
        TestConsumer c2 = new TestConsumer(100);
        network.addNode(c1);
        network.addNode(c2);

        tickSource.advance();

        assertEquals(50, c1.getLastReceived(), "C1 should receive 50%");
        assertEquals(50, c2.getLastReceived(), "C2 should receive 50%");
    }

    @Test
    public void testZeroSupply() {
        TestConsumer consumer = new TestConsumer(50);
        network.addNode(consumer);

        tickSource.advance();

        assertEquals(0, consumer.getLastReceived(), "Should receive 0 if no supply");
    }

    @Test
    public void testZeroDemand() {
        network.addNode(new TestProducer(100));
        // No consumers.
        // Just ensure no exception.
        tickSource.advance();
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
