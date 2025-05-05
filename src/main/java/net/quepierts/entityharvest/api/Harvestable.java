package net.quepierts.entityharvest.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.Shapes;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.joml.Vector3f;

/**
 * The interface for the entity that can be harvested.
 */
public interface Harvestable {
    Vector3f ZERO = new Vector3f();

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
    default DoubleLineIterator getOutline(boolean isShiftDown) {
        return Shapes.block()::forAllEdges;
    }

    /**
     * Get the outline relevant of the entity for rendering outlines.
     * @return the outline relevant of the entity
     */
    default OutlineRelevant getOutlineRelevant() {
        return OutlineRelevant.NONE;
    }

    /**
     * Get the shift of the outline.
     * @return the shift of the outline
     */
    default Vector3f getOutlineShift() {
        return ZERO;
    }

    /**
     * Whether to override the default harvest behavior.
     * If not, the attack will be handled by the game.
     * Or else the attack will be handled by the mod.
     * @return whether to override the default harvest behavior
     */
    default boolean isOverrideHarvest() {
        return false;
    }

    /**
     * Initialize the entity by attachment
     * @param attachment the attachment of the entity
     */
    default void init(HarvestProgressAttachment attachment) {

    }
}
