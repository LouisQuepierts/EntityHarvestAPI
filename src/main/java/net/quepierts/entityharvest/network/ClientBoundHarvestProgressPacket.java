package net.quepierts.entityharvest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.quepierts.entityharvest.EntityHarvest;
import net.quepierts.entityharvest.data.Attachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.jetbrains.annotations.NotNull;

public record ClientBoundHarvestProgressPacket(
        int id,
        float progress,
        boolean destroyed
) implements CustomPacketPayload {
    public static final Type<ClientBoundHarvestProgressPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EntityHarvest.MODID, "harvest_progress"));

    public static final StreamCodec<ByteBuf, ClientBoundHarvestProgressPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientBoundHarvestProgressPacket::id,
            ByteBufCodecs.FLOAT,
            ClientBoundHarvestProgressPacket::progress,
            ByteBufCodecs.BOOL,
            ClientBoundHarvestProgressPacket::destroyed,
            ClientBoundHarvestProgressPacket::new
    );

    public static void handle(ClientBoundHarvestProgressPacket packet, IPayloadContext context) {
        Level level = context.player().level();
        Entity entity = level.getEntity(packet.id());

        if (entity != null) {
            HarvestProgressAttachment attachment = entity.getData(Attachments.HARVEST_PROGRESS);
            attachment.setProgress(packet.progress());
            attachment.setDestroyed(packet.destroyed());
        }
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
