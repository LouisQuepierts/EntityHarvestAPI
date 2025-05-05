package net.quepierts.entityharvest.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.Config;
import net.quepierts.entityharvest.api.DoubleLineIterator;
import net.quepierts.entityharvest.api.HarvestWrapper;
import net.quepierts.entityharvest.api.OutlineRelevant;
import net.quepierts.entityharvest.network.SyncDestroyedParticlePacket;
import org.joml.Vector3f;

public class BoatHarvestWrapper<T extends Boat> extends HarvestWrapper<T> {
    @Override
    public boolean canHarvest(Player player) {
        return true;
    }

    @Override
    public void onDestroyed(Player player) {
        if (this.entity instanceof ChestBoat chest) {
            chest.destroy(player.damageSources().playerAttack(player));
        } else {
            this.entity.destroy(this.entity.getDropItem());
        }


        Level level = this.entity.level();
        level.playSound(
                null,
                this.entity.getX(), this.entity.getY(), this.entity.getZ(),
                (this.entity.getVariant() == Boat.Type.BAMBOO ? SoundType.BAMBOO : SoundType.WOOD).getBreakSound(),
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F
        );

        if (player instanceof ServerPlayer serverPlayer) {
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
            int blockId = Block.getId(state);
            final BlockPos blockPos = entity.getOnPos();
            final Vector3f position = new Vector3f((float) entity.getX(), (float) entity.getY(), (float) entity.getZ());
            PacketDistributor.sendToPlayer(serverPlayer, new SyncDestroyedParticlePacket(blockId, position, blockPos));
        }
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

    @Override
    public DoubleLineIterator getOutline(boolean isShiftDown) {
        return (this.entity.getVariant().isRaft() ? EntityHarvestShapes.RAFT : EntityHarvestShapes.BOAT)::forAllEdges;
    }

    @Override
    public OutlineRelevant getOutlineRelevant() {
        return OutlineRelevant.ROOT;
    }

    @Override
    public boolean isOverrideHarvest() {
        return Config.isOverrideBoatHarvest();
    }
}
