package me.almana.hyvoltz.test;

import me.almana.hyvoltz.core.network.ElectricNetwork;
import me.almana.hyvoltz.core.node.BufferedElectricConsumer;
import me.almana.hyvoltz.core.node.ElectricProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for internal consumer buffers.
 */
public class BufferedConsumerTest extends HeadlessTestHarness {

    private ElectricNetwork network;

    @BeforeEach
    public void setupNetwork() {
        network = new ElectricNetwork();
        tickSource.register(network);
    }

    @Test
    public void testBuffering() {
        // Supply: 10/tick. Capacity: 100.
        // Should take 10 ticks to fill.

        network.addNode(new TestProducer(10));
        TestBufferedConsumer consumer = new TestBufferedConsumer(100);
        network.addNode(consumer);

        // Tick 1
        tickSource.advance();
        assertEquals(10, consumer.getStoredEnergy());
        assertEquals(90, consumer.getRequestedInput());

        // Tick 5
        for (int i = 0; i < 4; i++)
            tickSource.advance();
        assertEquals(50, consumer.getStoredEnergy());

        // Tick 10 (Full)
        for (int i = 0; i < 5; i++)
            tickSource.advance();
        assertEquals(100, consumer.getStoredEnergy());
        assertEquals(0, consumer.getRequestedInput(), "Demand should be 0 when full");
    }

    @Test
    public void testPowerLoss() {
        // Fill buffer then remove power source
        TestProducer producer = new TestProducer(50);
        network.addNode(producer);
        TestBufferedConsumer consumer = new TestBufferedConsumer(100);
        network.addNode(consumer);

        // Fill it (2 ticks)
        tickSource.advance();
        tickSource.advance();
        assertEquals(100, consumer.getStoredEnergy());

        // Remove producer (equivalent to power cut)
        network.removeNode(producer);

        tickSource.advance();
        // No new power, but buffer is full
        assertEquals(100, consumer.getStoredEnergy());

        // Use energy
        boolean success = consumer.useEnergy(25);
        assertTrue(success);
        assertEquals(75, consumer.getStoredEnergy());

        // Demand should go up
        assertEquals(25, consumer.getRequestedInput());
    }

    private static class TestBufferedConsumer extends BufferedElectricConsumer {
        public TestBufferedConsumer(long capacity) {
            super(capacity);
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
}
