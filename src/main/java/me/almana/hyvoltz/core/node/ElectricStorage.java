package me.almana.hyvoltz.core.node;

/**
 * Node that stores energy for the network.
 */
public interface ElectricStorage extends ElectricNode {
    long getStoredEnergy();

    long getCapacity();

    /**
     * Tries to add energy to storage.
     * 
     * @param amount Amount to add.
     * @return Amount actually added.
     */
    long insertEnergy(long amount);

    /**
     * Tries to remove energy from storage.
     * 
     * @param amount Amount to remove.
     * @return Amount actually removed.
     */
    long extractEnergy(long amount);

    @Override
    default ElectricNodeType getType() {
        return ElectricNodeType.STORAGE;
    }

    /**
     * @return Priority of this storage. Higher priority discharges first. Lower
     *         priority charges first.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * @return Maximum power this storage can accept per tick.
     */
    default long getMaxInputPerTick() {
        return Long.MAX_VALUE;
    }

    /**
     * @return Maximum power this storage can discharge per tick.
     */
    default long getMaxOutputPerTick() {
        return Long.MAX_VALUE;
    }
}
