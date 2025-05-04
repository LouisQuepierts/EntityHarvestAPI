package net.quepierts.entityharvest.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.quepierts.entityharvest.data.Attachments;
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
    private void entityharvest$render(
            Boat entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!entity.hasData(Attachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment progressAttachment = entity.getData(Attachments.HARVEST_PROGRESS);

        if (progressAttachment.getProgress() == 0.0f) {
            return;
        }

        VertexConsumer sheeted = new SheetedDecalTextureGenerator(
                buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(progressAttachment.getDestroyType())),
                poseStack.last(), 1.0f
        );
        Pair<ResourceLocation, ListModel<Boat>> pair = this.getModelWithLocation(entity);
        ListModel<Boat> model = pair.getSecond();
        model.renderToBuffer(poseStack, sheeted, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
