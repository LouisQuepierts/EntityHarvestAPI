package net.quepierts.entityharvest.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"
            ),
            cancellable = true
    )
    private void onAttackedHarvestableEntity(Player player, Entity targetEntity, CallbackInfo ci) {
        if (targetEntity instanceof Harvestable harvestable) {
            if (harvestable.isOverrideHarvest() || harvestable.canHarvest(player)) {
                if (!targetEntity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS) || !targetEntity.getData(EntityHarvestAttachments.HARVEST_PROGRESS).isDestroyed()) {
                    ci.cancel();
                }
            }
        }
    }
}
