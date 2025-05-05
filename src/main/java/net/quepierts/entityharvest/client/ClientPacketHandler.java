package net.quepierts.entityharvest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.quepierts.entityharvest.EntityHarvest;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import net.quepierts.entityharvest.network.SyncDestroyedParticlePacket;
import net.quepierts.entityharvest.network.SyncHarvestEntityPacket;
import net.quepierts.entityharvest.network.SyncHarvestProgressPacket;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {
    public static void onHarvestPacket(SyncHarvestEntityPacket packet) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        final Player player = level.getPlayerByUUID(packet.playerUuid());
        final Entity target = level.getEntity(packet.entityId());
        if (player == null || target == null) {
            return;
        }

        player.swing(InteractionHand.MAIN_HAND);
        EntityHarvest.onHarvestEntity(target, player);
    }

    public static void onDestroyParticlePacket(SyncDestroyedParticlePacket packet) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null) {
            return;
        }

        BlockState state = Block.stateById(packet.blockId());
        if (state.isAir()) {
            return;
        }

        final BlockPos blockPos = packet.startBlockPos();
        final Vector3f position = packet.position();
        final VoxelShape shape = state.getShape(level, blockPos);
        final ParticleEngine engine = Minecraft.getInstance().particleEngine;

        shape.forAllBoxes((p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
            double d1 = Math.min(1.0, p_172276_ - p_172273_);
            double d2 = Math.min(1.0, p_172277_ - p_172274_);
            double d3 = Math.min(1.0, p_172278_ - p_172275_);
            int i = Math.max(2, Mth.ceil(d1 / 0.25));
            int j = Math.max(2, Mth.ceil(d2 / 0.25));
            int k = Math.max(2, Mth.ceil(d3 / 0.25));

            for(int l = 0; l < i; ++l) {
                for(int i1 = 0; i1 < j; ++i1) {
                    for(int j1 = 0; j1 < k; ++j1) {
                        double d4 = ((double)l + 0.5) / (double)i;
                        double d5 = ((double)i1 + 0.5) / (double)j;
                        double d6 = ((double)j1 + 0.5) / (double)k;
                        double d7 = d4 * d1 + p_172273_;
                        double d8 = d5 * d2 + p_172274_;
                        double d9 = d6 * d3 + p_172275_;
                        engine.add((new TerrainParticle(
                                level,
                                position.x() + d7 - 0.5,
                                position.y() + d8,
                                position.z() + d9 - 0.5,
                                d4 - 0.5,
                                d5 - 0.5,
                                d6 - 0.5,
                                state,
                                blockPos)
                        ).updateSprite(state, blockPos));
                    }
                }
            }

        });
    }

    public static void onSyncHarvestProgress(SyncHarvestProgressPacket packet) {
        Level level = Minecraft.getInstance().level;
        Entity entity = level.getEntity(packet.entityId());

        if (entity != null) {
            HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);
            attachment.setProgress(packet.progress());
            attachment.setDestroyed(packet.destroyed());
        }
    }
}
