package net.quepierts.entityharvest.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.network.DestroyedParticlePacket;
import org.joml.Vector3f;

public class FallingBlockEntityHarvestWrapper extends HarvestWrapper<FallingBlockEntity> {
    @Override
    public boolean canHarvest(Player player) {
        FallingBlockEntity entity = this.entity;
        return player.hasCorrectToolForDrops(entity.getBlockState(), entity.level(), entity.getStartPos());
    }

    @Override
    public void onDestroyed(Player player) {
        FallingBlockEntity entity = this.entity;

        BlockState blockState = entity.getBlockState();
        if (player instanceof ServerPlayer serverPlayer) {
            final int blockId = Block.getId(blockState);
            final BlockPos blockPos = entity.getStartPos();
            final Vector3f position = new Vector3f((float) entity.getX(), (float) entity.getY(), (float) entity.getZ());
            PacketDistributor.sendToPlayer(serverPlayer, new DestroyedParticlePacket(blockId, position, blockPos));
        }

        if (entity.dropItem
                && !player.isCreative()
                && player.hasCorrectToolForDrops(blockState, entity.level(), entity.getStartPos())) {
            entity.spawnAtLocation(blockState.getBlock());
        }
        entity.discard();

        Level level = entity.level();
        level.playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                blockState.getSoundType(entity.level(), entity.getStartPos(), entity).getBreakSound(),
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F
        );
    }

    @Override
    public void onDestroying(Player player, int tick) {
        FallingBlockEntity entity = this.entity;
        Level level = entity.level();
        BlockState blockState = entity.getBlockState();

        if (tick % 4 == 0) {
            SoundType soundType = blockState.getSoundType(entity.level(), entity.getStartPos(), entity);
            level.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    soundType.getHitSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 8.0F,
                    soundType.getPitch() * 0.5F
            );
        }

        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), entity.getX(), entity.getY() + 0.5, entity.getZ(), 0, 0, 0);
    }

    @Override
    public float getProgress(Player player) {
        FallingBlockEntity entity = this.entity;
        return entity.getBlockState().getDestroyProgress(player, entity.level(), entity.getStartPos());
    }
}
