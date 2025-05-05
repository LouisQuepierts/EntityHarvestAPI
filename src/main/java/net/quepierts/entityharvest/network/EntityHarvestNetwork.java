package net.quepierts.entityharvest.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.quepierts.entityharvest.EntityHarvest;
import net.quepierts.entityharvest.client.ClientPacketHandler;

@EventBusSubscriber(modid = EntityHarvest.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class EntityHarvestNetwork {
    public static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    private static void onRegisterPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                SyncHarvestProgressPacket.TYPE,
                SyncHarvestProgressPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(
                        () -> ClientPacketHandler.onSyncHarvestProgress(packet)
                )
        );
        registrar.playToClient(
                SyncDestroyedParticlePacket.TYPE,
                SyncDestroyedParticlePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(
                        () -> ClientPacketHandler.onDestroyParticlePacket(packet)
                )
        );
        registrar.playToClient(
                SyncHarvestEntityPacket.TYPE,
                SyncHarvestEntityPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(
                        () -> ClientPacketHandler.onHarvestPacket(packet)
                )
        );
        registrar.playToServer(
                UpdateHarvestEntityPacket.TYPE,
                UpdateHarvestEntityPacket.STREAM_CODEC,
                (packet, context) -> {
                    Player player = context.player();
                    packet.handle(player);
                }
        );
    }
}
