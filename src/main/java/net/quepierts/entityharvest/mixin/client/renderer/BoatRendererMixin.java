package net.quepierts.entityharvest.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.quepierts.entityharvest.client.DestroyTextureVertexConsumer;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatRenderer.class)
public abstract class BoatRendererMixin {
    @Shadow public abstract Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat);

    @Inject(
            method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ListModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void entityharvest$renderDestroy(
            Boat entity,
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

        VertexConsumer sheeted = DestroyTextureVertexConsumer.getVertexConsumer(buffer, 8, 4, attachment.getDestroyType());
        Pair<ResourceLocation, ListModel<Boat>> pair = this.getModelWithLocation(entity);
        ListModel<Boat> model = pair.getSecond();
        model.renderToBuffer(poseStack, sheeted, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
