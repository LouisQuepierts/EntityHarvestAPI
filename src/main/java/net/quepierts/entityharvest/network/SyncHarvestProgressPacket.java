package net.quepierts.entityharvest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.entityharvest.EntityHarvest;
import org.jetbrains.annotations.NotNull;

public record SyncHarvestProgressPacket(
        int entityId,
        float progress,
        boolean destroyed
) implements CustomPacketPayload {
    public static final Type<SyncHarvestProgressPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EntityHarvest.MODID, "harvest_progress"));

    public static final StreamCodec<ByteBuf, SyncHarvestProgressPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncHarvestProgressPacket::entityId,
            ByteBufCodecs.FLOAT,
            SyncHarvestProgressPacket::progress,
            ByteBufCodecs.BOOL,
            SyncHarvestProgressPacket::destroyed,
            SyncHarvestProgressPacket::new
    );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
