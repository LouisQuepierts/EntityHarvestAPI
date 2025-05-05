package net.quepierts.entityharvest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.entityharvest.EntityHarvest;
import org.joml.Vector3f;

public record SyncDestroyedParticlePacket(
        int blockId,
        Vector3f position,
        BlockPos startBlockPos
) implements CustomPacketPayload {
    public static final Type<SyncDestroyedParticlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EntityHarvest.MODID, "destroy_particle"));

    public static final StreamCodec<ByteBuf, SyncDestroyedParticlePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncDestroyedParticlePacket::blockId,
            ByteBufCodecs.VECTOR3F,
            SyncDestroyedParticlePacket::position,
            BlockPos.STREAM_CODEC,
            SyncDestroyedParticlePacket::startBlockPos,
            SyncDestroyedParticlePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
