# Creating Power Nodes

You define your machine's properties by implementing the `ElectricNode` sub-interfaces.

## 1. The Producer
Your block or node generates power.
```java
public class SolarGenerator extends HyVoltzNodeComponent implements ElectricProducer {
    
    public SolarGenerator() {
         // Pass UUID and any other dependencies to your internal node logic if needed
         super(new SimpleNode(UUID.randomUUID())); 
         // Or implement ElectricProducer directly on the component, 
         // but usually you wrap an internal model.
    }

    // Implementing the interface on the internal node:
    @Override
    public long getOutputPerTick() {
        return isDaytime() ? 10 : 0;
    }
}
```
*Note: In practice, `HyVoltzNodeComponent` wraps an `ElectricNode`. You typically pass an object implementing `ElectricProducer` to the constructor.*

## 2. The Consumer
Your block or node uses power.
```java
public class ElectricFurnace extends BufferedElectricConsumer {
    public ElectricFurnace() {
        super(1000); // Internal buffer size
    }

    @Override
    public long getMaxInputPerTick() {
        return 20; // Charge rate
    }

    public void smelt() {
        if (this.useEnergy(50)) {
             // Smelt item
        }
    }
}
```

## 3. The Storage
Your block or node stores power for others to use.
```java
public class BasicBatteryBox implements ElectricStorage {
    private long stored = 0;
    private final long capacity = 10000;

    @Override
    public long insertEnergy(long amount) {
        // ... add to stored, return accepted amount
    }

    @Override
    public long extractEnergy(long amount) {
        // ... remove from stored, return given amount
    }

    @Override
    public int getPriority() {
        // Storage usually has lower priority than consumers
        return 0; 
    }
}
```

## Buffered Machines

Most machines should be `BufferedElectricConsumer`. This gives them a small internal battery. Some edge case machines might use `ElectricConsumer` directly.
*   **Why?** If the network fluctuates, the machine can keep running for a few ticks.
*   **How?** The base class handles `receivePower` for you. You just call `useEnergy()` in your logic.
