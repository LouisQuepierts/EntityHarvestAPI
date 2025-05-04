package net.quepierts.entityharvest.api;

import net.neoforged.bus.api.Event;
import net.quepierts.entityharvest.harvest.HarvestWrapper;

import java.util.Map;

@SuppressWarnings("unused")
public class RegisterHarvestWrapperEvent extends Event {
    private final Map<Class<?>, HarvestWrapper<?>> wrappers;

    public RegisterHarvestWrapperEvent(Map<Class<?>, HarvestWrapper<?>> wrappers) {
        this.wrappers = wrappers;
    }

    public <T> void register(Class<T> clazz, HarvestWrapper<T> wrapper) {
        this.wrappers.put(clazz, wrapper);
    }
}
