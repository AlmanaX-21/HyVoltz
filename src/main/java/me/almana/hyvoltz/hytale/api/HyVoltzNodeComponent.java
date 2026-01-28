package me.almana.hyvoltz.hytale.api;

import com.hypixel.hytale.protocol.BlockPosition;
import me.almana.hyvoltz.core.node.ElectricNode;
import me.almana.hyvoltz.hytale.HyVoltzEngine;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Hytale-facing component to manage a HyVoltz node's lifecycle.
 */
public class HyVoltzNodeComponent {

    private final ElectricNode node;
    private final AtomicBoolean attached = new AtomicBoolean(false);
    private final AtomicReference<UUID> worldId = new AtomicReference<>();
    private BlockPosition position;
    private NodeSideConfig sideConfig = NodeSideConfig.omnidirectional();

    public HyVoltzNodeComponent(ElectricNode node) {
        this.node = node;
    }

    public void setSideConfig(NodeSideConfig config) {
        this.sideConfig = config != null ? config : NodeSideConfig.omnidirectional();
    }

    public NodeSideConfig getSideConfig() {
        return sideConfig;
    }

    /**
     * Attaches the node to the specified world's energy network at the given
     * position.
     * Safe to call multiple times i.e. idempotent.
     *
     * @param worldId The UUID of the world to attach to.
     * @param pos     The position of the block.
     */
    public void attach(UUID worldId, BlockPosition pos) {
        this.worldId.set(worldId);
        this.position = pos;
        HyVoltzEngine.getInstance().requestAttach(this, worldId);
    }

    /**
     * Detaches the node from its current world network.
     * Safe to call even if not attached.
     */
    public void detach() {
        UUID currentWorldId = worldId.get();
        if (currentWorldId != null) {
            HyVoltzEngine.getInstance().cancelAttach(this, currentWorldId);
            worldId.set(null);
        }
    }

    /**
     * Internal callback used by HyVoltzEngine.
     * Do not call manually.
     */
    public void internalOnAttach() {
        attached.set(true);
    }

    /**
     * Internal callback used by HyVoltzEngine.
     * Do not call manually.
     */
    public void internalOnDetach() {
        attached.set(false);
    }

    /**
     * @return The underlying ElectricNode.
     */
    public ElectricNode getNode() {
        return node;
    }

    public BlockPosition getPosition() {
        return position;
    }

    public boolean isAttached() {
        return attached.get();
    }
}
