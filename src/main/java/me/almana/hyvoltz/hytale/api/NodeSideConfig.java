package me.almana.hyvoltz.hytale.api;

import com.hypixel.hytale.math.vector.Vector3i;

public interface NodeSideConfig {

    boolean canInput(Vector3i direction);

    boolean canOutput(Vector3i direction);

    static NodeSideConfig omnidirectional() {
        return new NodeSideConfig() {
            @Override
            public boolean canInput(Vector3i direction) {
                return true;
            }

            @Override
            public boolean canOutput(Vector3i direction) {
                return true;
            }
        };
    }
}
