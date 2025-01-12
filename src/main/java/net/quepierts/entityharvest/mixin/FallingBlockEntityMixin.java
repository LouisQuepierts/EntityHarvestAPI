package net.quepierts.entityharvest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.quepierts.entityharvest.Harvestable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin({FallingBlockEntity.class})
public abstract class FallingBlockEntityMixin extends Entity implements Harvestable {
    @Shadow private BlockState blockState;

    @Shadow public abstract BlockPos getStartPos();

    @Shadow public boolean dropItem;

    private FallingBlockEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    @Override
    public boolean canHarvest(Player player) {
        return true;
    }

    @Unique
    @Override
    public void onDestroyed(Player player) {
        if (this.dropItem && player.hasCorrectToolForDrops(this.blockState)) {
            this.spawnAtLocation(this.blockState.getBlock());
        }
        this.discard();

        Level level = this.level();
        level.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                this.blockState.getSoundType().getBreakSound(),
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F
        );
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.blockState), this.getX(), this.getY() + 0.5, this.getZ(), 0, 0.2, 0);
    }

    @Unique
    @Override
    public void onDestroying(Player player, int tick) {
        Level level = this.level();
        if (tick % 4 == 0) {
            level.playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    this.blockState.getSoundType().getHitSound(),
                    SoundSource.BLOCKS,
                    1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F
            );
        }

        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.blockState), this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
    }

    @Unique
    @Override
    public float getProgress(Player player) {
        return this.blockState.getDestroyProgress(player, this.level(), this.getStartPos());
    }

    @Unique
    @Override
    public VoxelShape getShape() {
        return this.blockState.getShape(this.level(), this.getStartPos());
    }
}
