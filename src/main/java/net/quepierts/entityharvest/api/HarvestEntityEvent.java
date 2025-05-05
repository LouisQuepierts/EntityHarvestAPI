package net.quepierts.entityharvest.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

@SuppressWarnings("unused")
public final class HarvestEntityEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final Entity target;
    private final Harvestable harvestable;

    public HarvestEntityEvent(Player player, Entity target, Harvestable harvestable) {
        this.player = player;
        this.target = target;
        this.harvestable = harvestable;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getTarget() {
        return target;
    }

    public Harvestable getHarvestable() {
        return harvestable;
    }
}
