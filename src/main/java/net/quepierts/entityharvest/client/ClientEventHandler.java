package net.quepierts.entityharvest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.EntityHarvest;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.api.OutlineRelevant;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import net.quepierts.entityharvest.network.UpdateHarvestEntityPacket;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(modid = EntityHarvest.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    private static void onClickInputEvent(final InputEvent.InteractionKeyMappingTriggered event) {
        final Minecraft minecraft = Minecraft.getInstance();

        if (!event.isAttack() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        final LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        final HitResult result = minecraft.hitResult;
        if (result == null || result.getType() != HitResult.Type.ENTITY) {
            return;
        }

        final Entity entity = ((EntityHitResult) result).getEntity();
        final Harvestable harvestable = EntityHarvest.getHarvestable(entity);

        if (harvestable == null) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

        if (attachment.isDestroyed()) {
            return;
        }

        if (!harvestable.canHarvest(player)) {
            if (harvestable.isOverrideHarvest()) {
                event.setCanceled(true);
            }
            return;
        }

        if (EntityHarvest.performHarvest(player, entity, harvestable)) {
            UpdateHarvestEntityPacket packet = new UpdateHarvestEntityPacket(entity.getId(), true);
            PacketDistributor.sendToServer(packet);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    private static void onRenderEntityOutline(final RenderHighlightEvent.Entity event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || player.isSpectator()) {
            return;
        }

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

        if (!harvestable.canHarvest(player)
                && !harvestable.isOverrideHarvest()) {
            return;
        }

        if (entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS) && entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS).isDestroyed()) {
            return;
        }

        double x = position.x() - 0.5 - camPos.x();
        double y = position.y() - camPos.y();
        double z = position.z() - 0.5 - camPos.z();
        VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        Vector3f shift = harvestable.getOutlineShift();
        if (shift.x() != 0.0F || shift.y() != 0.0F || shift.z() != 0.0F) {
            poseStack.translate(shift.x(), shift.y(), shift.z());
        }

        float xRot = 0;
        float yRot = 0;
        if (harvestable.getOutlineRelevant()== OutlineRelevant.ROOT) {
            xRot = entity.getViewXRot(partialTick);
            yRot = entity.getViewYRot(partialTick);
        }

        if (xRot != 0.0F || yRot != 0.0F) {
            poseStack.translate(0.5F, 0F, 0.5F);
            poseStack.mulPose(new Quaternionf().rotationZYX(0.0F, -yRot * Mth.DEG_TO_RAD, xRot * Mth.DEG_TO_RAD));
            poseStack.translate(-0.5F, 0F, -0.5F);
        }

        PoseStack.Pose posestack$pose = poseStack.last();
        harvestable.getOutline(player.isShiftKeyDown()).foreach((x1, y1, z1, x2, y2, z2) -> {
            float f = (float)(x2 - x1);
            float f1 = (float)(y2 - y1);
            float f2 = (float)(z2 - z1);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            consumer.addVertex(posestack$pose, (float)(x1), (float)(y1), (float)(z1)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(posestack$pose, f, f1, f2);
            consumer.addVertex(posestack$pose, (float)(x2), (float)(y2), (float)(z2)).setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(posestack$pose, f, f1, f2);
        });
        poseStack.popPose();
    }
}
