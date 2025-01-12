package net.quepierts.entityharvest;

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
import net.quepierts.entityharvest.data.Attachments;

@EventBusSubscriber(modid = EntityHarvest.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onRenderEntityOutline(final RenderHighlightEvent.Entity event) {
        EntityHitResult target = event.getTarget();
        Entity entity = target.getEntity();
        PoseStack poseStack = event.getPoseStack();

        MultiBufferSource multiBufferSource = event.getMultiBufferSource();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        DeltaTracker tracker = event.getDeltaTracker();
        float partialTick = tracker.getGameTimeDeltaPartialTick(false);
        Vec3 position = entity.getPosition(partialTick);

        if (entity instanceof Harvestable harvestable) {
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

            PoseStack.Pose posestack$pose = poseStack.last();

            shape.forAllEdges((p_323073_, p_323074_, p_323075_, p_323076_, p_323077_, p_323078_) -> {
                float f = (float)(p_323076_ - p_323073_);
                float f1 = (float)(p_323077_ - p_323074_);
                float f2 = (float)(p_323078_ - p_323075_);
                float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                f /= f3;
                f1 /= f3;
                f2 /= f3;
                consumer.addVertex(posestack$pose, (float)(p_323073_ + x), (float)(p_323074_ + y), (float)(p_323075_ + z)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(posestack$pose, f, f1, f2);
                consumer.addVertex(posestack$pose, (float)(p_323076_ + x), (float)(p_323077_ + y), (float)(p_323078_ + z)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(posestack$pose, f, f1, f2);
            });
        }
    }
}
