package net.quepierts.entityharvest;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface Harvestable {
    boolean canHarvest(Player player);

    void onDestroyed(Player player);

    void onDestroying(Player player, int tick);

    float getProgress(Player player);

    default VoxelShape getShape() {
        return Shapes.block();
    }
}
