package net.quepierts.entityharvest.harvest;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EntityHarvestShapes {
    public static final VoxelShape SHULKER;

    public static final VoxelShape BOAT;
    public static final VoxelShape RAFT;

    public static final VoxelShape CHEST_ON_BOAT;

    public static final VoxelShape SKELETON_SKULL;
    public static final VoxelShape WITHER_SKELETON_SKULL;

    static {
        SHULKER = Shapes.or(
                Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0),
                Block.box(0.0, 4.0, 0.0, 4.0, 8.0, 4.0),
                Block.box(0.0, 4.0, 12.0, 4.0, 8.0, 16.0),
                Block.box(12.0, 4.0, 0.0, 16.0, 8.0, 4.0),
                Block.box(12.0, 4.0, 12.0, 16.0, 8.0, 16.0)
        );

        BOAT = Shapes.or(
                Block.box(0.0D, 0.0D, -6.0D, 16.0D, 3.0D, 22.0D),
                Block.box(-2.0D, 3.0D, -6.0D, 0.0D, 9.0D, 22.0D),
                Block.box(16.0D, 3.0D, -6.0D, 18.0D, 9.0D, 22.0D),
                Block.box(-1.0D, 3.0D, -8.0D, 17.0D, 9.0D, -6.0D),
                Block.box(0.0D, 3.0D, 22.0D, 16.0D, 9.0D, 24.0D)
        );

        RAFT = Shapes.or(
                Block.box(0.0D, 0.0D, -6.0D, 16.0D, 8.0D, 22.0D),
                Block.box(-2.0D, 4.0D, -6.0D, 18.0D, 8.0D, 22.0D)
        );

        CHEST_ON_BOAT = Block.box(4.0D, 0.0D, 4.0D, 8.0D, 8.0D, 8.0D);

        SKELETON_SKULL = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);
        WITHER_SKELETON_SKULL = Block.box(3.250D, 0.0D, 3.25D, 12.75D, 9.5D, 12.75D);
    }
}
