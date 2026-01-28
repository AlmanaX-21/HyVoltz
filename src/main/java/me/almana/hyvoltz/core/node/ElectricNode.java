package me.almana.hyvoltz.core.node;

import java.util.UUID;

/**
 * Base abstraction for electric nodes.
 */
public interface ElectricNode {
    UUID getId();

    ElectricNodeType getType();
}
