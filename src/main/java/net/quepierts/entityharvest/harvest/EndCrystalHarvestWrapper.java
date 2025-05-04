package net.quepierts.entityharvest.harvest;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class EndCrystalHarvestWrapper extends HarvestWrapper<EndCrystal> {
    @Override
    public boolean canHarvest(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        Optional<HolderLookup.RegistryLookup<Enchantment>> lookup = player.level().registryAccess().lookup(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> enchantment = lookup.get().getOrThrow(Enchantments.SILK_TOUCH);
        return mainHandItem.getEnchantmentLevel(enchantment) > 0;
    }

    @Override
    public void onDestroyed(Player player) {
        this.entity.spawnAtLocation(Items.END_CRYSTAL);
        this.entity.discard();
    }

    @Override
    public void onDestroying(Player player, int tick) {

    }

    @Override
    public float getProgress(Player player) {
        return Blocks.OBSIDIAN.defaultBlockState().getDestroyProgress(player, null, null);
    }
}
