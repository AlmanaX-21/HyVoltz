package me.almana.hyvoltz.core.node;

/**
 * Node that generates power.
 */
public interface ElectricProducer extends ElectricNode {
    long getOutputPerTick();

    @Override
    default ElectricNodeType getType() {
        return ElectricNodeType.PRODUCER;
    }
}
