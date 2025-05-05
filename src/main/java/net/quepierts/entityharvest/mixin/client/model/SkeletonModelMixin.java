package net.quepierts.entityharvest.mixin.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonModel.class)
public abstract class SkeletonModelMixin<T extends Mob & RangedAttackMob> extends HumanoidModel<T> {
    public SkeletonModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V",
            at = @At("RETURN")
    )
    private void entityharvest$hideHead(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS) && entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS).isDestroyed()) {
            this.head.xScale = 0;
            this.head.yScale = 0;
            this.head.zScale = 0;

            this.leftArm.xRot += Mth.PI;
            this.leftArm.zRot += Mth.PI / 30f;
            this.rightArm.xRot += Mth.PI;
            this.rightArm.zRot -= Mth.PI / 30f;
        } else {
            this.head.xScale = 1;
            this.head.yScale = 1;
            this.head.zScale = 1;
        }
    }
}
