package net.quepierts.entityharvest;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.quepierts.entityharvest.data.Attachments;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EntityHarvest.MODID)
public class EntityHarvest {
    public static final String MODID = "entityharvest";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EntityHarvest(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        Attachments.REGISTER.register(modEventBus);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (target instanceof Harvestable harvestable && harvestable.canHarvest(player)) {
            HarvestProgressAttachment attachment = target.getData(Attachments.HARVEST_PROGRESS);

            if (attachment.isDestroyed()) {
                return;
            }

            float progress = harvestable.getProgress(player) + attachment.getProgress();
            attachment.setProgress(progress);
            harvestable.onDestroying(player, attachment.getDestroyTick());
            event.setCanceled(true);

            if (attachment.isDestroyed()) {
                harvestable.onDestroyed(player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityTick(final EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.hasData(Attachments.HARVEST_PROGRESS)) {
            HarvestProgressAttachment attachment = entity.getData(Attachments.HARVEST_PROGRESS);
            attachment.tick();
        }
    }
}
