package net.quepierts.entityharvest.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.InputEvent;
import net.quepierts.entityharvest.EntityHarvest;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.data.Attachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Shadow @Final public Options options;

    @Inject(
            method = "continueAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V",
                    shift = At.Shift.AFTER
            )
    )
    private void continueAttackFallingBlock(
            boolean leftClick,
            CallbackInfo ci
    ) {
        if (leftClick && this.hitResult != null && this.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) this.hitResult).getEntity();
            Harvestable harvestable = EntityHarvest.getHarvestable(entity);

            if (harvestable == null) {
                return;
            }

            if (!entity.hasData(Attachments.HARVEST_PROGRESS)) {
                return;
            }

            HarvestProgressAttachment attachment = entity.getData(Attachments.HARVEST_PROGRESS);

            if (attachment.isDestroyed()) {
                return;
            }

            if (!harvestable.canHarvest(this.player)) {
                return;
            }
            this.gameMode.attack(this.player, entity);

            InputEvent.InteractionKeyMappingTriggered inputEvent = ClientHooks.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
            if (inputEvent.shouldSwingHand()) {
                this.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }
}
