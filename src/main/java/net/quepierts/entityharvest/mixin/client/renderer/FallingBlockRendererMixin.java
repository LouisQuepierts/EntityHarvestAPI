package net.quepierts.entityharvest.mixin.client.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.quepierts.entityharvest.client.DestroyTextureVertexConsumer;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
public abstract class FallingBlockRendererMixin extends EntityRenderer<FallingBlockEntity> {
    @Shadow @Final private BlockRenderDispatcher dispatcher;

    protected FallingBlockRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/BakedModel;getRenderTypes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;)Lnet/neoforged/neoforge/client/ChunkRenderTypeSet;"
            )
    )
    private void entityharvest$renderDestroy(
            FallingBlockEntity entity,
            float entityYaw, float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci,
            @Local BlockState blockstate,
            @Local BlockPos blockPos,
            @Local Level level
    ) {
        if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

        if (attachment.getProgress() == 0.0f) {
            return;
        }

        VertexConsumer sheeted = DestroyTextureVertexConsumer.getVertexConsumer(buffer, 64, 32, attachment.getDestroyType());

        BakedModel model = this.dispatcher.getBlockModel(blockstate);
        long seed = blockstate.getSeed(entity.getStartPos());
        this.dispatcher.getModelRenderer().tesselateBlock(
                level, model, blockstate, blockPos,
                poseStack, sheeted, false,
                RandomSource.create(), seed,
                OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null
        );
    }

}
