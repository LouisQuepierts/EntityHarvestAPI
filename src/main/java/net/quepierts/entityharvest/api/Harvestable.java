package net.quepierts.entityharvest.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The interface for the entity that can be harvested.
 */
public interface Harvestable {

    /**
     * Check if the entity can be harvested.
     * @param player the player who is harvesting the entity
     * @return can the entity be harvested
     */
    boolean canHarvest(Player player);

    /**
     * Called when the entity is destroyed by harvest.
     * @param player the player who is harvesting the entity
     */
    void onDestroyed(Player player);

    /**
     * Called when the entity is destroying by harvest.
     * @param player the player who is harvesting the entity
     * @param tick the tick of the entity is destroying
     */
    void onDestroying(Player player, int tick);

    /**
     * Get the progress of the entity is destroying.
     * @param player the player who is harvesting the entity
     * @return the progress of the entity is destroying
     */
    float getProgress(Player player);

    /**
     * Get the shape of the entity for rendering outlines.
     * @return the shape of the entity
     */
    default VoxelShape getShape() {
        return Shapes.block();
    }
}
