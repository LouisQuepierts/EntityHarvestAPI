package net.quepierts.entityharvest.mixin.client;

import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Shulker;
import net.quepierts.entityharvest.data.Attachments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerModel.class)
public class ShulkerModelMixin<T extends Shulker> {
    @Shadow @Final private ModelPart lid;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/monster/Shulker;FFFFF)V",
            at = @At("HEAD")
    )
    private void hideLid(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.hasData(Attachments.HARVEST_PROGRESS) && entity.getData(Attachments.HARVEST_PROGRESS).isDestroyed()) {
            this.lid.xScale = 0;
            this.lid.yScale = 0;
            this.lid.zScale = 0;
        } else {
            this.lid.xScale = 1;
            this.lid.yScale = 1;
            this.lid.zScale = 1;
        }
    }
}
