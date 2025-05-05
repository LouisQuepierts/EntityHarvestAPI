package net.quepierts.entityharvest.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.quepierts.entityharvest.client.DestroyTextureVertexConsumer;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class SkeletonRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    @Shadow protected M model;

    protected SkeletonRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    shift = At.Shift.AFTER
            )
    )
    private void entityharvest$renderDestroy(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!(this.model instanceof SkeletonModel<?> skeletonModel)) {
            return;
        }

        if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);
        if (attachment.getProgress() == 0.0f) {
            return;
        }

        VertexConsumer sheeted = DestroyTextureVertexConsumer.getVertexConsumer(buffer, 4, 2, attachment.getDestroyType());
        skeletonModel.head.render(poseStack, sheeted, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
