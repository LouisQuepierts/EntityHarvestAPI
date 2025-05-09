package net.quepierts.entityharvest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.api.DoubleLineIterator;
import net.quepierts.entityharvest.api.Harvestable;
import net.quepierts.entityharvest.data.EntityHarvestAttachments;
import net.quepierts.entityharvest.harvest.EntityHarvestShapes;
import net.quepierts.entityharvest.network.SyncDestroyedParticlePacket;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Shulker.class)
public abstract class ShulkerMixin extends Entity implements Harvestable {
    @Shadow @Final protected static EntityDataAccessor<Byte> DATA_PEEK_ID;

    @Shadow protected abstract boolean isClosed();

    private ShulkerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    @Override
    public boolean canHarvest(Player player) {
        return this.isClosed() && player.getMainHandItem().canPerformAction(ItemAbilities.PICKAXE_DIG);
    }

    @Unique
    @Override
    public void onDestroyed(Player player) {
        this.spawnAtLocation(new ItemStack(Items.SHULKER_SHELL, 1), 1);

        Level level = this.level();
        level.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundType.STONE.getBreakSound(),
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F
        );

        if (player instanceof ServerPlayer serverPlayer) {
            final int blockId = Block.getId(Blocks.SHULKER_BOX.defaultBlockState());
            final BlockPos blockPos = this.getOnPos();
            final Vector3f position = new Vector3f((float) this.getX(), (float) this.getY(), (float) this.getZ());
            PacketDistributor.sendToPlayer(serverPlayer, new SyncDestroyedParticlePacket(blockId, position, blockPos));
        }
    }

    @Unique
    @Override
    public void onDestroying(Player player, int tick) {
        Level level = this.level();
        if (tick % 4 == 0) {
            level.playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundType.STONE.getHitSound(),
                    SoundSource.BLOCKS,
                    1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F
            );
        }

        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SHULKER_BOX.defaultBlockState()), this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
    }

    @Unique
    @Override
    public float getProgress(Player player) {
        return Blocks.END_STONE.defaultBlockState().getDestroyProgress(player, this.level(), this.getOnPos());
    }

    @Unique
    @Override
    public DoubleLineIterator getOutline(boolean isShiftDown) {
        return EntityHarvestShapes.SHULKER::forAllEdges;
    }

    @Inject(
            method = "isClosed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void entityharvest$isClosed(CallbackInfoReturnable<Boolean> cir) {
        if (this.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)
                && this.getData(EntityHarvestAttachments.HARVEST_PROGRESS).isDestroyed()
        ) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(
            method = "setRawPeekAmount",
            at = @At("HEAD"),
            cancellable = true
    )
    private void entityharvest$setRawPeekAmount(int peekAmount, CallbackInfo ci) {
        if (this.hasData(EntityHarvestAttachments.HARVEST_PROGRESS)
                && this.getData(EntityHarvestAttachments.HARVEST_PROGRESS).isDestroyed()
        ) {
            this.entityData.set(DATA_PEEK_ID, (byte) 8);
            ci.cancel();
        }
    }
}
