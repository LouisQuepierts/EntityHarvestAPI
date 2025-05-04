package net.quepierts.entityharvest.harvest;

import net.quepierts.entityharvest.api.Harvestable;

public abstract class HarvestWrapper<T> implements Harvestable {
    protected T entity;

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
