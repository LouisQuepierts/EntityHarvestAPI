package net.quepierts.entityharvest.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.quepierts.entityharvest.client.DestroyTextureVertexConsumer;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalRenderer.class)
public class EndCrystalRendererMixin {
    @Unique
    private VertexConsumer entityharvest$destroy;

    @Inject(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void entityharvest$setup(
            EndCrystal entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

        if (attachment.getProgress() == 0.0f) {
            return;
        }

        this.entityharvest$destroy = DestroyTextureVertexConsumer.getVertexConsumer(buffer, 8, 4, attachment.getDestroyType());
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN")
    )
    private void entityharvest$cleanup(
            EndCrystal entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        this.entityharvest$destroy = null;
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
            )
    )
    private void entityharvest$renderDestroy(
            ModelPart instance,
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            Operation<Void> original
    ) {
        instance.render(poseStack, buffer, packedLight, packedOverlay);
        if (this.entityharvest$destroy != null) {
            instance.render(poseStack, this.entityharvest$destroy, packedLight, packedOverlay);
        }
    }
}
