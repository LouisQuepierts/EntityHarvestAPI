package net.quepierts.entityharvest.harvest;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;

public class BoatHarvestWrapper extends HarvestWrapper<Boat> {
    @Override
    public boolean canHarvest(Player player) {
        return player.getMainHandItem().canPerformAction(ItemAbilities.AXE_DIG);
    }

    @Override
    public void onDestroyed(Player player) {
        this.entity.spawnAtLocation(this.entity.getDropItem());
        this.entity.discard();
    }

    @Override
    public void onDestroying(Player player, int tick) {
        Level level = this.entity.level();
        if (tick % 4 == 0) {
            level.playSound(
                    null,
                    this.entity.getX(), this.entity.getY(), this.entity.getZ(),
                    (this.entity.getVariant() == Boat.Type.BAMBOO ? SoundType.BAMBOO : SoundType.WOOD).getHitSound(),
                    SoundSource.BLOCKS,
                    1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F
            );
        }

        BlockState state = (switch (this.entity.getVariant()) {
            case CHERRY -> Blocks.CHERRY_PLANKS;
            case DARK_OAK -> Blocks.DARK_OAK_PLANKS;
            case MANGROVE -> Blocks.MANGROVE_PLANKS;
            case BAMBOO -> Blocks.BAMBOO;
            case OAK -> Blocks.OAK_PLANKS;
            case SPRUCE -> Blocks.SPRUCE_PLANKS;
            case BIRCH -> Blocks.BIRCH_PLANKS;
            case JUNGLE -> Blocks.JUNGLE_PLANKS;
            case ACACIA -> Blocks.ACACIA_PLANKS;
        }).defaultBlockState();
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), this.entity.getX(), this.entity.getY() + 0.5, this.entity.getZ(), 0, 0, 0);
    }

    @Override
    public float getProgress(Player player) {
        return Blocks.OAK_PLANKS.defaultBlockState().getDestroyProgress(player, null, null);
    }
}
