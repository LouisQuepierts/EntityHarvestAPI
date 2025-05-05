package net.quepierts.entityharvest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.entityharvest.EntityHarvest;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SyncHarvestEntityPacket(
        UUID playerUuid,
        int entityId,
        boolean shouldSwingArm
) implements CustomPacketPayload {
    public static final Type<SyncHarvestEntityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EntityHarvest.MODID, "sync_harvest_entity")
    );

    public static final StreamCodec<ByteBuf, SyncHarvestEntityPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            SyncHarvestEntityPacket::playerUuid,
            ByteBufCodecs.INT,
            SyncHarvestEntityPacket::entityId,
            ByteBufCodecs.BOOL,
            SyncHarvestEntityPacket::shouldSwingArm,
            SyncHarvestEntityPacket::new
    );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
