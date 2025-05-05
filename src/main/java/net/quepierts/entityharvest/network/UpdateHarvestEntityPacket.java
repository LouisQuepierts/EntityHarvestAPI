package net.quepierts.entityharvest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.quepierts.entityharvest.EntityHarvest;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record UpdateHarvestEntityPacket(
        int entityId,
        boolean shouldSwingArm
) implements CustomPacketPayload {
    public static final Type<UpdateHarvestEntityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EntityHarvest.MODID, "update_harvest_entity")
    );

    public static final StreamCodec<ByteBuf, UpdateHarvestEntityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateHarvestEntityPacket::entityId,
            ByteBufCodecs.BOOL,
            UpdateHarvestEntityPacket::shouldSwingArm,
            UpdateHarvestEntityPacket::new
    );

    public void handle(Player player) {
        Entity entity = player.level().getEntity(this.entityId());

        if (entity == null) {
            return;
        }

        if (!EntityHarvest.onHarvestEntity(entity, player)) {
            return;
        }

        SyncHarvestEntityPacket packet = new SyncHarvestEntityPacket(player.getUUID(), this.entityId(), this.shouldSwingArm());
        ClientboundCustomPayloadPacket payload = new ClientboundCustomPayloadPacket(packet);
        MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer());

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            if (serverPlayer != player) {
                serverPlayer.connection.send(payload);
            }
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
