package net.quepierts.entityharvest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = EntityHarvest.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER;

    public static final ModConfigSpec.BooleanValue ENABLE_FALLING_BLOCK_HARVEST;

    public static final ModConfigSpec.BooleanValue ENABLE_SKELETON_HARVEST;

    public static final ModConfigSpec.BooleanValue ENABLE_WITHER_SKELETON_HARVEST;

    public static final ModConfigSpec.BooleanValue OVERRIDE_BOAT_HARVEST;

    public static final ModConfigSpec.BooleanValue OVERRIDE_MINECART_HARVEST;

    public static final ModConfigSpec.BooleanValue OVERRIDE_END_CRYSTAL_HARVEST;

    public static final ModConfigSpec SPEC;

    private static boolean enableFallingBlockHarvest;
    private static boolean enableSkeletonHarvest;
    private static boolean enableWitherSkeletonHarvest;
    private static boolean overrideBoatHarvest;
    private static boolean overrideMinecartHarvest;
    private static boolean overrideEndCrystalHarvest;

    @SubscribeEvent
    private static void onLoadConfig(final ModConfigEvent event) {
        enableFallingBlockHarvest = ENABLE_FALLING_BLOCK_HARVEST.get();
        enableSkeletonHarvest = ENABLE_SKELETON_HARVEST.get();
        enableWitherSkeletonHarvest = ENABLE_WITHER_SKELETON_HARVEST.get();
        overrideBoatHarvest = OVERRIDE_BOAT_HARVEST.get();
        overrideMinecartHarvest = OVERRIDE_MINECART_HARVEST.get();
        overrideEndCrystalHarvest = OVERRIDE_END_CRYSTAL_HARVEST.get();
    }

    public static boolean isEnableFallingBlockHarvest() {
        return enableFallingBlockHarvest;
    }

    public static boolean isEnableSkeletonHarvest() {
        return enableSkeletonHarvest;
    }

    public static boolean isEnableWitherSkeletonHarvest() {
        return enableWitherSkeletonHarvest;
    }

    public static boolean isOverrideBoatHarvest() {
        return overrideBoatHarvest;
    }

    public static boolean isOverrideMinecartHarvest() {
        return overrideMinecartHarvest;
    }

    public static boolean isOverrideEndCrystalHarvest() {
        return overrideEndCrystalHarvest;
    }

    static {
        BUILDER = new ModConfigSpec.Builder()
                .comment("Builtin Behaviours")
                .push("Behaviours");

        BUILDER.comment("Enable Options")
                .push("Enable Options");

        ENABLE_FALLING_BLOCK_HARVEST = BUILDER
                .comment("Enable falling block harvest")
                .define("enable_falling_block_harvest", true);

        ENABLE_SKELETON_HARVEST = BUILDER
                .comment("Enable skeleton harvest")
                .define("enable_skeleton_harvest", true);

        ENABLE_WITHER_SKELETON_HARVEST = BUILDER
                .comment("Enable wither skeleton harvest")
                .define("enable_wither_skeleton_harvest", true);

        BUILDER.pop()
                .comment("Override Options")
                .push("Override Options");

        OVERRIDE_BOAT_HARVEST = BUILDER
                .comment("Override boat harvest")
                .define("override_boat_harvest", true);

        OVERRIDE_MINECART_HARVEST = BUILDER
                .comment("Override minecart harvest")
                .define("override_minecart_harvest", true);

        OVERRIDE_END_CRYSTAL_HARVEST = BUILDER
                .comment("Override end crystal harvest")
                .define("override_end_crystal_harvest", true);

        BUILDER.pop().pop();
        SPEC = BUILDER.build();
    }
}
