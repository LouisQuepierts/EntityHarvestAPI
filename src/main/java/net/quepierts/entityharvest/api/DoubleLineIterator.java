package net.quepierts.entityharvest.api;

import net.minecraft.world.phys.shapes.Shapes;

public interface DoubleLineIterator {
    void foreach(Shapes.DoubleLineConsumer consumer);
}
