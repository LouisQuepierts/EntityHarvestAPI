package net.quepierts.entityharvest.mixin;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.ItemAbilities;
import net.quepierts.entityharvest.Harvestable;
import net.quepierts.entityharvest.data.Attachments;
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

    private ShulkerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    @Override
    public boolean canHarvest(Player player) {
        return player.getMainHandItem().canPerformAction(ItemAbilities.PICKAXE_DIG);
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
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SHULKER_BOX.defaultBlockState()), this.getX(), this.getY() + 0.5, this.getZ(), 0, 0.2, 0);
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

    @Inject(
            method = "isClosed",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eh$isClosed(CallbackInfoReturnable<Boolean> cir) {
        if (this.hasData(Attachments.HARVEST_PROGRESS) && this.getData(Attachments.HARVEST_PROGRESS).isDestroyed()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(
            method = "setRawPeekAmount",
            at = @At("HEAD"),
            cancellable = true
    )
    private void eh$setRawPeekAmount(int peekAmount, CallbackInfo ci) {
        if (this.hasData(Attachments.HARVEST_PROGRESS) && this.getData(Attachments.HARVEST_PROGRESS).isDestroyed()) {
            this.entityData.set(DATA_PEEK_ID, (byte) 0);
            ci.cancel();
        }
    }
}
