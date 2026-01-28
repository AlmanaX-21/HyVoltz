# Debugging & Common Issues

## Problem: Node Not Receiving Power

**Likely Cause**: Side Misconfiguration.
*   **Fix**: Check if your Generator is set to Output on the correct side, and the Consumer is set to Input. Remember `UP` on the generator flows into `DOWN` on the machine above it.

**Likely Cause**: Priority.
*   **Fix**: If you have another consumer with higher priority, it might be hogging all the power. Set priority to `0` to test.

## Problem: Network Not Forming

**Likely Cause**: Ghost Nodes.
*   **Fix**: Did you forget to call `detach()` when breaking a block previously? The system might think a node is still there. Restart the server to clear runtime state. There will possibly be an update to this in the future.

**Likely Cause**: Deferred Attachment.
*   **Fix**: Are you trying to attach before the world is loaded? HyVoltz waits for world load. Ensure the chunk is actually loaded. Try to wait a bit to ensure the world is loaded and everything is attached. 

## Problem: Throughput Limiting

**Symptom**: Machine fills up very slowly despite a huge generator.
*   **Cause**: Your `getMaxInputPerTick()` is too low.
*   **Fix**: Increase the limit in your `ElectricConsumer` implementation.
