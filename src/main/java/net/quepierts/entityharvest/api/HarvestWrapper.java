package net.quepierts.entityharvest.api;

import net.minecraft.world.entity.Entity;

public abstract class HarvestWrapper<T extends Entity> implements Harvestable {
    protected T entity;

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
