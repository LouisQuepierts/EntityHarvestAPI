package net.quepierts.entityharvest.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.EntityHarvest;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import net.quepierts.entityharvest.network.UpdateHarvestEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public LocalPlayer player;

    @Inject(
            method = "continueAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V",
                    shift = At.Shift.AFTER
            )
    )
    private void entityharvest$continueHarvest(
            boolean leftClick,
            CallbackInfo ci
    ) {
        LocalPlayer player = this.player;

        if (player == null) {
            return;
        }

        if (leftClick && this.hitResult != null && this.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) this.hitResult).getEntity();
            Harvestable harvestable = EntityHarvest.getHarvestable(entity);

            if (harvestable == null) {
                return;
            }

            if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
                return;
            }

            HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

            if (attachment.isDestroyed()) {
                return;
            }

            if (!harvestable.canHarvest(player) && !harvestable.isOverrideHarvest()) {
                return;
            }

            player.swing(InteractionHand.MAIN_HAND);
            if (EntityHarvest.performHarvest(player, entity, harvestable)) {
                UpdateHarvestEntityPacket packet = new UpdateHarvestEntityPacket(entity.getId(), true);
                PacketDistributor.sendToServer(packet);
            }
        }
    }
}
