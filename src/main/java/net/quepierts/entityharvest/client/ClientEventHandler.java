package net.quepierts.entityharvest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.quepierts.entityharvest.EntityHarvest;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.data.Attachments;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = EntityHarvest.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onRenderEntityOutline(final RenderHighlightEvent.Entity event) {
        EntityHitResult target = event.getTarget();
        Entity entity = target.getEntity();
        Harvestable harvestable = EntityHarvest.getHarvestable(entity);

        if (harvestable == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();

        MultiBufferSource multiBufferSource = event.getMultiBufferSource();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        DeltaTracker tracker = event.getDeltaTracker();
        float partialTick = tracker.getGameTimeDeltaPartialTick(false);
        Vec3 position = entity.getPosition(partialTick);

        if (!harvestable.canHarvest(Minecraft.getInstance().player)) {
            return;
        }

        if (entity.hasData(Attachments.HARVEST_PROGRESS) && entity.getData(Attachments.HARVEST_PROGRESS).isDestroyed()) {
            return;
        }

        VoxelShape shape = harvestable.getShape();
        double x = position.x() - 0.5 - camPos.x();
        double y = position.y() - camPos.y();
        double z = position.z() - 0.5 - camPos.z();
        VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        float xRot = entity.getXRot();
        float yRot = entity.getYRot();

        if (xRot != 0.0F || yRot != 0.0F) {
            poseStack.translate(0.5F, 0F, 0.5F);
            poseStack.mulPose(new Quaternionf().rotationZYX(0.0F, -yRot * Mth.DEG_TO_RAD, -xRot * Mth.DEG_TO_RAD));
            poseStack.translate(-0.5F, 0F, -0.5F);
        }

        PoseStack.Pose posestack$pose = poseStack.last();

        shape.forAllEdges((p_323073_, p_323074_, p_323075_, p_323076_, p_323077_, p_323078_) -> {
            float f = (float)(p_323076_ - p_323073_);
            float f1 = (float)(p_323077_ - p_323074_);
            float f2 = (float)(p_323078_ - p_323075_);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            consumer.addVertex(posestack$pose, (float)(p_323073_), (float)(p_323074_), (float)(p_323075_)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(posestack$pose, f, f1, f2);
            consumer.addVertex(posestack$pose, (float)(p_323076_), (float)(p_323077_), (float)(p_323078_)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(posestack$pose, f, f1, f2);
        });
        poseStack.popPose();
    }
}
