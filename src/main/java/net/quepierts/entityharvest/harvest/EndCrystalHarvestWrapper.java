package net.quepierts.entityharvest.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.Config;
import net.quepierts.entityharvest.api.DoubleLineIterator;
import net.quepierts.entityharvest.api.HarvestWrapper;
import net.quepierts.entityharvest.network.SyncDestroyedParticlePacket;
import org.joml.Vector3f;

import java.util.Optional;

public class EndCrystalHarvestWrapper extends HarvestWrapper<EndCrystal> {
    private static final VoxelShape SHAPE;

    private final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
    private Holder.Reference<Enchantment> enchantment;

    @Override
    public boolean canHarvest(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();

        boolean toolForDrops = mainHandItem.isCorrectToolForDrops(obsidian);
        if (this.isOverrideHarvest()) {
            return toolForDrops;
        } else {
            return toolForDrops && this.hasCorrectEnchantment(mainHandItem);
        }
    }

    @Override
    public void onDestroyed(Player player) {
        EndCrystal entity = this.entity;

        ItemStack mainHandItem = player.getMainHandItem();
        boolean shouldDrop = !this.isOverrideHarvest() || this.hasCorrectEnchantment(mainHandItem);

        if (shouldDrop) {
            entity.spawnAtLocation(Items.END_CRYSTAL);
            entity.discard();

            if (player instanceof ServerPlayer serverPlayer) {
                final int blockId = Block.getId(obsidian);
                final BlockPos blockPos = entity.getOnPos();
                final Vector3f position = new Vector3f((float) entity.getX(), (float) entity.getY(), (float) entity.getZ());
                PacketDistributor.sendToPlayer(serverPlayer, new SyncDestroyedParticlePacket(blockId, position, blockPos));
            }
        } else {
            DamageSource damageSource = player.damageSources().playerAttack(player);
            this.entity.hurt(damageSource, 1.0f);
        }
    }

    @Override
    public void onDestroying(Player player, int tick) {
        EndCrystal entity = this.entity;
        Level level = entity.level();

        if (tick % 4 == 0) {
            SoundType soundType = obsidian.getSoundType(entity.level(), entity.getOnPos(), entity);
            level.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    soundType.getHitSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 8.0F,
                    soundType.getPitch() * 0.5F
            );
        }

        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, obsidian), entity.getX(), entity.getY() + 0.5, entity.getZ(), 0, 0, 0);
    }

    @Override
    public float getProgress(Player player) {
        return Blocks.OBSIDIAN.defaultBlockState().getDestroyProgress(player, this.entity.level(), this.entity.getOnPos());
    }

    @Override
    public DoubleLineIterator getOutline(boolean isShiftDown) {
        return SHAPE::forAllEdges;
    }

    @Override
    public boolean isOverrideHarvest() {
        return Config.isOverrideEndCrystalHarvest();
    }

    private boolean hasCorrectEnchantment(ItemStack itemStack) {
        if (this.enchantment == null) {
            Optional<HolderLookup.RegistryLookup<Enchantment>> lookup = this.entity.level().registryAccess().lookup(Registries.ENCHANTMENT);
            if (lookup.isEmpty()) {
                return false;
            }
            this.enchantment = lookup.get().getOrThrow(Enchantments.SILK_TOUCH);
        }
        return itemStack.getEnchantmentLevel(this.enchantment) > 0;
    }

    static {
        SHAPE = Block.box(
                -4.0, 0.0, -4.0,
                 20.0, 40.0, 20.0
        );
    }
}
