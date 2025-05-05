package net.quepierts.entityharvest.api;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

import java.util.Map;

@SuppressWarnings("unused")
public final class RegisterHarvestWrapperEvent extends Event {
    private final Map<Class<?>, HarvestWrapper<?>> wrappers;

    public RegisterHarvestWrapperEvent(Map<Class<?>, HarvestWrapper<?>> wrappers) {
        this.wrappers = wrappers;
    }

    public <T extends Entity> void register(Class<T> clazz, HarvestWrapper<T> wrapper) {
        this.wrappers.put(clazz, wrapper);
    }
}
