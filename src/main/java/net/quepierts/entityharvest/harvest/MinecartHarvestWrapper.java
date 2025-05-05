package net.quepierts.entityharvest.harvest;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.quepierts.entityharvest.Config;
import net.quepierts.entityharvest.api.DoubleLineIterator;
import net.quepierts.entityharvest.api.HarvestWrapper;
import net.quepierts.entityharvest.api.OutlineRelevant;

public class MinecartHarvestWrapper<T extends AbstractMinecart> extends HarvestWrapper<T> {
    private static final VoxelShape SHAPE;

    private final BlockState ironBlock = Blocks.IRON_BLOCK.defaultBlockState();
    private final ItemStack fakeItemStack = new ItemStack(Items.MINECART);

    @Override
    public boolean canHarvest(Player player) {
        return player.getMainHandItem().isCorrectToolForDrops(ironBlock);
    }

    @Override
    public void onDestroyed(Player player) {
        if (this.entity instanceof AbstractMinecartContainer container) {
            container.destroy(player.damageSources().playerAttack(player));
        } else {
            this.entity.destroy(Items.MINECART);
        }
    }

    @Override
    public void onDestroying(Player player, int tick) {
        Level level = this.entity.level();

        if (tick % 4 == 0) {
            SoundType soundType = ironBlock.getSoundType(entity.level(), entity.getOnPos(), entity);
            level.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    soundType.getHitSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 8.0F,
                    soundType.getPitch() * 0.5F
            );
        }
        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, fakeItemStack), this.entity.getX(), this.entity.getY() + 0.5, this.entity.getZ(), 0, 0, 0);
    }

    @Override
    public float getProgress(Player player) {
        return ironBlock.getDestroyProgress(player, entity.level(), entity.getOnPos());
    }

    @Override
    public OutlineRelevant getOutlineRelevant() {
        return OutlineRelevant.ROOT;
    }

    @Override
    public DoubleLineIterator getOutline(boolean isShiftDown) {
        return SHAPE::forAllEdges;
    }

    @Override
    public boolean isOverrideHarvest() {
        return Config.isOverrideMinecartHarvest();
    }

    static {
        SHAPE = Block.box(-2.0D, 1.0D, 0.0D, 18.0D, 11.0D, 16.0D);
    }
}
