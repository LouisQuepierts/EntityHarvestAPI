package net.quepierts.entityharvest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.entityharvest.EntityHarvest;
import org.joml.Vector3f;

public record DestroyedParticlePacket(
        int blockId,
        Vector3f position,
        BlockPos startBlockPos
) implements CustomPacketPayload {
    public static final Type<DestroyedParticlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EntityHarvest.MODID, "destroy_particle"));

    public static final StreamCodec<ByteBuf, DestroyedParticlePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DestroyedParticlePacket::blockId,
            ByteBufCodecs.VECTOR3F,
            DestroyedParticlePacket::position,
            BlockPos.STREAM_CODEC,
            DestroyedParticlePacket::startBlockPos,
            DestroyedParticlePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
