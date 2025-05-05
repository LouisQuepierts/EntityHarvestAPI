package net.quepierts.entityharvest.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import net.quepierts.entityharvest.ai.MissingHeadPanicGoal;
import net.quepierts.entityharvest.api.DoubleLineIterator;
import net.quepierts.entityharvest.api.HarvestWrapper;
import net.quepierts.entityharvest.api.OutlineRelevant;
import net.quepierts.entityharvest.data.HarvestProgressAttachment;
import net.quepierts.entityharvest.network.SyncDestroyedParticlePacket;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class SkeletonHarvestWrapper<T extends AbstractSkeleton> extends HarvestWrapper<T> {
    public static final double PANIC_RUNNING_SPEED = 1.5;

    private final Vector3f shift;
    private final DoubleLineIterator shape;
    private final BlockState block;
    private final Item head;
    private final BooleanSupplier isEnabled;

    public SkeletonHarvestWrapper(Vector3f shift, VoxelShape shape, Block block, BooleanSupplier isEnabled) {
        this.shift = shift;
        this.shape = shape::forAllEdges;
        this.block = block.defaultBlockState();
        this.head = block.asItem();
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean canHarvest(Player player) {
        if (!this.isEnabled.getAsBoolean()) {
            return false;
        }

        if (this.entity == null) {
            return false;
        }

        /*if (!this.entity.hasEffect(MobEffects.WEAKNESS)) {
            return false;
        }*/

        ItemStack mainHandItem = player.getMainHandItem();
        Optional<HolderLookup.RegistryLookup<Enchantment>> lookup = player.level().registryAccess().lookup(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> enchantment = lookup.get().getOrThrow(Enchantments.SILK_TOUCH);
        boolean toolForDrops = mainHandItem.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState());
        return toolForDrops && mainHandItem.getEnchantmentLevel(enchantment) > 0;
    }

    @Override
    public void onDestroyed(Player player) {
        AbstractSkeleton skeleton = this.entity;
        skeleton.spawnAtLocation(new ItemStack(this.head), skeleton.getEyeHeight());

        skeleton.setLastHurtByPlayer(null);
        skeleton.setTarget(null);
        skeleton.goalSelector.getAvailableGoals().forEach(WrappedGoal::stop);
        skeleton.goalSelector.addGoal(-10, new MissingHeadPanicGoal(this.entity, PANIC_RUNNING_SPEED));

        Level level = skeleton.level();
        level.playSound(
                null,
                skeleton.getX(), skeleton.getY(), skeleton.getZ(),
                SoundType.STONE.getBreakSound(),
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F
        );

        if (player instanceof ServerPlayer serverPlayer) {
            final int blockId = Block.getId(block);
            final BlockPos blockPos = skeleton.getOnPos();
            final Vector3f position = new Vector3f((float) skeleton.getX(), (float) skeleton.getY() + skeleton.getEyeHeight(), (float) skeleton.getZ());
            PacketDistributor.sendToPlayer(serverPlayer, new SyncDestroyedParticlePacket(blockId, position, blockPos));
        }
    }

    @Override
    public void onDestroying(Player player, int tick) {
        AbstractSkeleton skeleton = this.entity;
        Level level = skeleton.level();
        if (tick % 4 == 0) {
            level.playSound(
                    null,
                    skeleton.getX(), skeleton.getY() + skeleton.getEyeHeight(), skeleton.getZ(),
                    SoundType.STONE.getHitSound(),
                    SoundSource.BLOCKS,
                    1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F
            );
        }

        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block), skeleton.getX(), skeleton.getY() + skeleton.getEyeHeight(), skeleton.getZ(), 0, 0, 0);
    }

    @Override
    public float getProgress(Player player) {
        return block.getDestroyProgress(player, this.entity.level(), this.entity.getOnPos());
    }

    @Override
    public OutlineRelevant getOutlineRelevant() {
        return OutlineRelevant.ROOT;
    }

    @Override
    public Vector3f getOutlineShift() {
        return shift;
    }

    @Override
    public DoubleLineIterator getOutline(boolean isShiftDown) {
        return shape;
    }

    @Override
    public void init(HarvestProgressAttachment attachment) {
        if (attachment.isDestroyed()) {
            this.entity.goalSelector.addGoal(-10, new MissingHeadPanicGoal(this.entity, PANIC_RUNNING_SPEED));
        }
    }
}
