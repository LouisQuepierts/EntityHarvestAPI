package net.quepierts.entityharvest.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;
import net.quepierts.entityharvest.client.DestroyTextureVertexConsumer;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ShulkerRenderer.class)
public abstract class ShulkerRendererMixin extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
    public ShulkerRendererMixin(EntityRendererProvider.Context context, ShulkerModel<Shulker> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Unique
    @Override
    public void render(
            @NotNull Shulker entity,
            float entityYaw,
            float partialTicks,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        ModelPart lid = this.model.getLid();

        if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

        if (attachment.getProgress() != 0.0f) {
            float rotLerp = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
            poseStack.pushPose();
            float scale = entity.getScale();
            poseStack.scale(scale, scale, scale);
            float bob = this.getBob(entity, partialTicks);
            this.setupRotations(entity, poseStack, bob, rotLerp, partialTicks, scale);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.scale(entity, poseStack, partialTicks);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            VertexConsumer sheeted = DestroyTextureVertexConsumer.getVertexConsumer(buffer, 8, 4, attachment.getDestroyType());

            boolean bodyVisible = this.isBodyVisible(entity);
            boolean visible = !bodyVisible && !entity.isInvisibleTo(Minecraft.getInstance().player);
            int i = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            lid.render(poseStack, sheeted, packedLight, i, visible ? 654311423 : -1);
            poseStack.popPose();
        }
    }
}
