package me.almana.hyvoltz.core.network;

import me.almana.hyvoltz.core.node.ElectricConsumer;
import me.almana.hyvoltz.core.node.ElectricNode;
import me.almana.hyvoltz.core.node.ElectricProducer;
import me.almana.hyvoltz.core.node.ElectricStorage;
import me.almana.hyvoltz.core.tick.TickListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Container for electric nodes.
 */
public class ElectricNetwork implements TickListener {
    private final Set<ElectricNode> nodes = new HashSet<>();

    public void addNode(ElectricNode node) {
        nodes.add(node);
    }

    public void removeNode(ElectricNode node) {
        nodes.remove(node);
    }

    public Collection<ElectricNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public void onTick(long tick) {
        // Collect nodes first
        long producerSupply = 0;
        List<ElectricStorage> storageNodes = new ArrayList<>();
        List<ElectricConsumer> consumerNodes = new ArrayList<>();

        for (ElectricNode node : nodes) {
            if (node instanceof ElectricProducer producer) {
                producerSupply += producer.getOutputPerTick();
            } else if (node instanceof ElectricStorage storage) {
                storageNodes.add(storage);
            } else if (node instanceof ElectricConsumer consumer) {
                consumerNodes.add(consumer);
            }
        }

        long availablePower = producerSupply;

        // Calculate Effective Demand (clamped by limit)
        long totalEffectiveDemand = 0;
        for (ElectricConsumer consumer : consumerNodes) {
            long req = consumer.getRequestedInput();
            long limit = consumer.getMaxInputPerTick();
            totalEffectiveDemand += Math.min(req, limit);
        }

        // 1. Storage Discharge Phase (High Priority first)
        // Sort DESCENDING
        if (availablePower < totalEffectiveDemand) {
            long deficit = totalEffectiveDemand - availablePower;
            storageNodes.sort(Comparator
                    .comparingInt(ElectricStorage::getPriority).reversed());

            for (ElectricStorage storage : storageNodes) {
                if (deficit <= 0)
                    break;

                long stored = storage.getStoredEnergy();
                long dischargeLimit = storage.getMaxOutputPerTick();
                long maxExtract = Math.min(stored, dischargeLimit);

                long toExtract = Math.min(deficit, maxExtract);

                long extracted = storage.extractEnergy(toExtract);
                availablePower += extracted;
                deficit -= extracted;
            }
        }

        // 2. Distribution Phase (High Priority consumers first)
        // Group by priority
        if (totalEffectiveDemand > 0) {
            Map<Integer, List<ElectricConsumer>> priorityGroups = new HashMap<>();

            for (ElectricConsumer consumer : consumerNodes) {
                priorityGroups.computeIfAbsent(consumer.getPriority(), k -> new ArrayList<>()).add(consumer);
            }

            // Iterate priorities DESCENDING
            var priorities = new ArrayList<>(priorityGroups.keySet());
            priorities.sort(Comparator.reverseOrder());

            for (Integer priority : priorities) {
                if (availablePower <= 0)
                    break;

                var group = priorityGroups.get(priority);

                // Recalculate group demand effectively clamped
                long groupDemand = 0;
                for (ElectricConsumer c : group) {
                    groupDemand += Math.min(c.getRequestedInput(), c.getMaxInputPerTick());
                }

                if (groupDemand == 0)
                    continue;

                double satisfaction = Math.min(1.0, (double) availablePower / groupDemand);

                for (ElectricConsumer consumer : group) {
                    long requested = consumer.getRequestedInput();
                    long limit = consumer.getMaxInputPerTick();
                    long effectiveRequest = Math.min(requested, limit);

                    long received = (long) (effectiveRequest * satisfaction);
                    consumer.receivePower(received);
                    availablePower -= received;
                }
            }
        }

        // 3. Storage Charge Phase (Low Priority first)
        // Whatever is left after distribution

        long excess = availablePower;

        if (excess > 0) {
            // Sort ASCENDING (Low priority charges first)
            storageNodes
                    .sort(Comparator.comparingInt(ElectricStorage::getPriority));

            for (ElectricStorage storage : storageNodes) {
                if (excess <= 0)
                    break;

                long capacity = storage.getCapacity();
                long stored = storage.getStoredEnergy();
                long space = capacity - stored;
                long chargeLimit = storage.getMaxInputPerTick();
                long maxInsert = Math.min(space, chargeLimit);

                long toInsert = Math.min(excess, maxInsert);

                long inserted = storage.insertEnergy(toInsert);
                excess -= inserted;
            }
        }
    }
}
