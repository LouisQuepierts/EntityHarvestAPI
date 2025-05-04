package net.quepierts.entityharvest;

import com.mojang.logging.LogUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.api.RegisterHarvestWrapperEvent;
import net.quepierts.entityharvest.data.Attachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import net.quepierts.entityharvest.harvest.BoatHarvestWrapper;
import net.quepierts.entityharvest.harvest.EndCrystalHarvestWrapper;
import net.quepierts.entityharvest.harvest.FallingBlockEntityHarvestWrapper;
import net.quepierts.entityharvest.harvest.HarvestWrapper;
import net.quepierts.entityharvest.network.ClientBoundHarvestProgressPacket;
import net.quepierts.entityharvest.network.EntityHarvestNetwork;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EntityHarvest.MODID)
public class EntityHarvest {
    public static final String MODID = "entityharvest";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Class<?>, HarvestWrapper<?>> wrappers = new IdentityHashMap<>();

    public EntityHarvest(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        Attachments.REGISTER.register(modEventBus);

        this.registerHarvestableWrappers();
    }

    private void registerHarvestableWrappers() {
        wrappers.put(Boat.class, new BoatHarvestWrapper());
        wrappers.put(EndCrystal.class, new EndCrystalHarvestWrapper());
        wrappers.put(FallingBlockEntity.class, new FallingBlockEntityHarvestWrapper());

        NeoForge.EVENT_BUS.post(new RegisterHarvestWrapperEvent(wrappers));
    }

    public static boolean onEntityHurt(
            @NotNull Entity entity,
            @NotNull Player player
    ) {
        if (entity.isRemoved()) {
            return false;
        }

        Harvestable harvestable = EntityHarvest.getHarvestable(entity);

        if (harvestable == null || !harvestable.canHarvest(player)) {
            return false;
        }

        HarvestProgressAttachment attachment = entity.getData(Attachments.HARVEST_PROGRESS);

        if (attachment.isDestroyed()) {
            return false;
        }

        float progress = player.isCreative() ? 1.0f : harvestable.getProgress(player) + attachment.getProgress();
        attachment.setProgress(progress);
        harvestable.onDestroying(player, attachment.getDestroyTick());

        if (attachment.isDestroyed()) {
            harvestable.onDestroyed(player);

            ItemStack itemstack = player.getMainHandItem();
            ItemStack original = itemstack.copy();
            itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            if (itemstack.isEmpty()) {
                EventHooks.onPlayerDestroyItem(player, original, InteractionHand.MAIN_HAND);
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Harvestable getHarvestable(T entity) {
        if (entity instanceof Harvestable) {
            return (Harvestable) entity;
        }

        HarvestWrapper<?> wrapper = wrappers.get(entity.getClass());
        if (wrapper != null) {
            ((HarvestWrapper<T>) wrapper).setEntity(entity);
        }
        return wrapper;
    }

//    @SubscribeEvent
    private void onAttackTarget(final AttackEntityEvent event) {
        Player player = event.getEntity();
        if (EntityHarvest.onEntityHurt(event.getTarget(), player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    private void onEntityTick(final EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.hasData(Attachments.HARVEST_PROGRESS)) {
            HarvestProgressAttachment attachment = entity.getData(Attachments.HARVEST_PROGRESS);
            attachment.tick();
        }
    }

    @SubscribeEvent
    private void onChunkWatchSent(final ChunkWatchEvent.Sent event) {
        ServerLevel level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        ChunkPos chunkPos = event.getPos();
        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();

        AABB aabb = new AABB(
                x, level.getMinBuildHeight(), z,
                x + 16, level.getMaxBuildHeight(), z + 16
        );

        ServerPlayer player = event.getPlayer();
        List<Entity> entities = level.getEntities(player, aabb);
        ClientBoundHarvestProgressPacket packet = null;
        List<ClientBoundHarvestProgressPacket> packets = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.hasData(Attachments.HARVEST_PROGRESS)) {
                HarvestProgressAttachment attachment = entity.getData(Attachments.HARVEST_PROGRESS);
                ClientBoundHarvestProgressPacket temp = new ClientBoundHarvestProgressPacket(entity.getId(), attachment.getProgress(), attachment.isDestroyed());

                if (packet == null) {
                    packet = temp;
                } else {
                    packets.add(temp);
                }
            }
        }

        if (packet != null) {
            PacketDistributor.sendToPlayer(player, packet, packets.toArray(CustomPacketPayload[]::new));
        }
    }
}
