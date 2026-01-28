package me.almana.hyvoltz.core.node;

import java.util.UUID;

/**
 * Consumer with internal energy buffer.
 */
public abstract class BufferedElectricConsumer implements ElectricConsumer {
    private final UUID id = UUID.randomUUID();
    private final long capacity;
    private long storedEnergy;

    protected BufferedElectricConsumer(long capacity) {
        this.capacity = capacity;
        this.storedEnergy = 0;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public long getRequestedInput() {
        return Math.max(0, capacity - storedEnergy);
    }

    @Override
    public void receivePower(long amount) {
        storedEnergy = Math.min(capacity, storedEnergy + amount);
    }

    public boolean useEnergy(long amount) {
        if (storedEnergy >= amount) {
            storedEnergy -= amount;
            return true;
        }
        return false;
    }

    public long getStoredEnergy() {
        return storedEnergy;
    }
}
