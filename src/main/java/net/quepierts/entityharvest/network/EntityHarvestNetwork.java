package net.quepierts.entityharvest.network;

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
                ClientBoundHarvestProgressPacket.TYPE,
                ClientBoundHarvestProgressPacket.STREAM_CODEC,
                ClientBoundHarvestProgressPacket::handle
        );
        registrar.playToClient(
                DestroyedParticlePacket.TYPE,
                DestroyedParticlePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(
                        () -> ClientPacketHandler.onDestroyParticlePacket(packet)
                )
        );
    }
}
