package net.quepierts.entityharvest.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.quepierts.entityharvest.EntityHarvest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void entityharvest$harvest(
            Entity target,
            CallbackInfo ci
    ) {
        if (EntityHarvest.onEntityHurt(target, (Player) (Object) this)) {
            ci.cancel();
        }
    }
}
