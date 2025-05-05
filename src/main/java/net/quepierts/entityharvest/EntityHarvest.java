package net.quepierts.entityharvest;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.api.HarvestWrapper;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.api.RegisterHarvestWrapperEvent;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import net.quepierts.entityharvest.harvest.*;
import net.quepierts.entityharvest.network.SyncHarvestProgressPacket;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.IdentityHashMap;
import java.util.Map;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EntityHarvest.MODID)
public class EntityHarvest {
    public static final String MODID = "entityharvestapi";
    private static final Map<Class<?>, HarvestWrapper<?>> wrappers = new IdentityHashMap<>();

    public EntityHarvest(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        EntityHarvestAttachments.REGISTER.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        this.registerHarvestableWrappers();
    }

    private void registerHarvestableWrappers() {
        wrappers.put(Boat.class, new BoatHarvestWrapper<>());
        wrappers.put(ChestBoat.class, new BoatHarvestWrapper<ChestBoat>());

        wrappers.put(Minecart.class, new MinecartHarvestWrapper<Minecart>());
        wrappers.put(MinecartChest.class, new MinecartHarvestWrapper<MinecartChest>());
        wrappers.put(MinecartHopper.class, new MinecartHarvestWrapper<MinecartHopper>());
        wrappers.put(MinecartFurnace.class, new MinecartHarvestWrapper<MinecartFurnace>());
        wrappers.put(MinecartSpawner.class, new MinecartHarvestWrapper<MinecartSpawner>());
        wrappers.put(MinecartTNT.class, new MinecartHarvestWrapper<MinecartTNT>());

        wrappers.put(EndCrystal.class, new EndCrystalHarvestWrapper());
        wrappers.put(FallingBlockEntity.class, new FallingBlockEntityHarvestWrapper());
        wrappers.put(Skeleton.class, new SkeletonHarvestWrapper<Skeleton>(new Vector3f(0.0f, 24.0f / 16.0f, 0.0f), EntityHarvestShapes.SKELETON_SKULL, Blocks.SKELETON_SKULL, Config::isEnableSkeletonHarvest));
        wrappers.put(WitherSkeleton.class, new SkeletonHarvestWrapper<WitherSkeleton>(new Vector3f(0.0f, 29.0f / 16.0f, 0.0f), EntityHarvestShapes.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Config::isEnableWitherSkeletonHarvest));

        NeoForge.EVENT_BUS.post(new RegisterHarvestWrapperEvent(wrappers));
    }

    public static boolean onHarvestEntity(
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

        return performHarvest(player, entity, harvestable);
    }

    public static boolean performHarvest(
            Player player,
            Entity entity,
            Harvestable harvestable
    ) {
        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

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

    @SubscribeEvent
    private void onLoadEntityFromDisk(final EntityJoinLevelEvent event) {
        if (!event.loadedFromDisk()) {
            return;
        }

        Entity entity = event.getEntity();
        Harvestable harvestable = EntityHarvest.getHarvestable(entity);

        if (harvestable == null) {
            return;
        }

        if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);
        harvestable.init(attachment);
    }

    @SubscribeEvent
    private void onInteractEntity(final PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();

        if (!target.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = target.getData(EntityHarvestAttachments.HARVEST_PROGRESS);

        if (!attachment.isDestroyed()) {
            return;
        }

        Class<? extends Entity> type = target.getClass();
        ItemStack stack = event.getItemStack();
        if (type == Skeleton.class && stack.is(Items.SKELETON_SKULL)
                || type == WitherSkeleton.class && stack.is(Items.WITHER_SKELETON_SKULL)
        ) {
            attachment.setDestroyed(false);
            attachment.setProgress(0.0f);
            stack.shrink(1);
            event.getEntity().swing(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    private void onEntityTick(final EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);
            attachment.tick();
        }
    }

    @SubscribeEvent
    private void onStartTracking(final PlayerEvent.StartTracking event) {
        final Entity entity = event.getTarget();

        Level level = entity.level();
        if (level.isClientSide) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!entity.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)) {
            return;
        }

        HarvestProgressAttachment attachment = entity.getData(EntityHarvestAttachments.HARVEST_PROGRESS);
        SyncHarvestProgressPacket packet = new SyncHarvestProgressPacket(
                entity.getId(),
                attachment.getProgress(),
                attachment.isDestroyed()
        );
        PacketDistributor.sendToPlayer(serverPlayer, packet);
    }
}