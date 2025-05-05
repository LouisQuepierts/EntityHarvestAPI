package net.quepierts.entityharvest.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.quepierts.entityharvest.client.DestroyTextureVertexConsumer;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartRenderer.class)
public abstract class MinecartRendererMixin<T extends AbstractMinecart> extends EntityRenderer<T> {
    @Shadow @Final protected EntityModel<T> model;

    protected MinecartRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
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
        if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

        if (attachment.getProgress() == 0.0f) {
            return;
        }

        VertexConsumer sheeted = DestroyTextureVertexConsumer.getVertexConsumer(buffer, 4, 2, attachment.getDestroyType());
        this.model.renderToBuffer(poseStack, sheeted, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
