package me.almana.hyvoltz.core.node;

/**
 * Node that consumes power.
 */
public interface ElectricConsumer extends ElectricNode {
    long getRequestedInput();

    void receivePower(long amount);

    @Override
    default ElectricNodeType getType() {
        return ElectricNodeType.CONSUMER;
    }

    /**
     * @return Priority of this consumer. Higher values are satisfied first.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * @return Maximum power this consumer can accept per tick.
     */
    default long getMaxInputPerTick() {
        return Long.MAX_VALUE;
    }
}
