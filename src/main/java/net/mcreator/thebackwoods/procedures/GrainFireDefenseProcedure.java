package net.mcreator.thebackwoods.procedures;
// 1.21.1 neoforge
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import java.util.Set;

@EventBusSubscriber
public class GrainFireDefenseProcedure {

    private static final int CHECK_INTERVAL_TICKS = 8;
    private static final int FIRE_SCAN_RADIUS_XZ = 128;
    private static final int FIRE_SCAN_RADIUS_Y = 48;
    private static final int BIG_FIRE_THRESHOLD = 40; 
    private static final int PURGE_COOLDOWN_TICKS = 20 * 6; // 6 sec

    private static final String NBT_COOLDOWN_KEY = "grain_fire_purge_cd";

	private static final ResourceKey<Level> THE_GRAIN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:the_grain")
    );

    private static final ResourceKey<Level> BACKWOODS = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:backwoods")
    );

    // 1. Declare the resource key for the sub strata dimension
    private static final ResourceKey<Level> THE_SUB_STRATA = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:the_sub_strata")
    );

    // 2. Add it to the immutable Set of allowed dimensions
    private static final Set<ResourceKey<Level>> VALID_DIMENSIONS = Set.of(THE_GRAIN, BACKWOODS, THE_SUB_STRATA);
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null) return;

        Level level = player.level();
        if (level.isClientSide()) return;

        // Ensure we are in one of the two dimensions
        if (!VALID_DIMENSIONS.contains(level.dimension())) return;
        
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        // FIXED: Using orElse(0) to convert Optional<Integer> to int
        int cd = player.getPersistentData().getInt(NBT_COOLDOWN_KEY);
        
        if (cd > 0) {
            player.getPersistentData().putInt(NBT_COOLDOWN_KEY, Math.max(0, cd - CHECK_INTERVAL_TICKS));
            return;
        }

        BlockPos center = player.blockPosition();

        int fireCount = countFireInRadius(level, center);
        if (fireCount < BIG_FIRE_THRESHOLD) return;

        // Apply visual cues
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 5, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 5, false, false));

        // Play Sound using safe Optional handling
        BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse("minecraft:ambient.cave")).ifPresent(cave -> {
            level.playSound(null, center, cave, SoundSource.AMBIENT, 1.3f, 0.85f);
        });

        // Perform the purge
        purgeFireInRadius(level, center);

        // Set the cooldown
        player.getPersistentData().putInt(NBT_COOLDOWN_KEY, PURGE_COOLDOWN_TICKS);
    }

    private static int countFireInRadius(Level level, BlockPos center) {
        int count = 0;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dx = -FIRE_SCAN_RADIUS_XZ; dx <= FIRE_SCAN_RADIUS_XZ; dx += 4) {
            for (int dz = -FIRE_SCAN_RADIUS_XZ; dz <= FIRE_SCAN_RADIUS_XZ; dz += 4) {
                for (int dy = -FIRE_SCAN_RADIUS_Y; dy <= FIRE_SCAN_RADIUS_Y; dy += 4) {
                    p.set(cx + dx, cy + dy, cz + dz);
                    if (isFire(level.getBlockState(p))) count += 64;
                }
            }
        }
        return count;
    }

    private static void purgeFireInRadius(Level level, BlockPos center) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dx = -FIRE_SCAN_RADIUS_XZ; dx <= FIRE_SCAN_RADIUS_XZ; dx += 2) {
            for (int dz = -FIRE_SCAN_RADIUS_XZ; dz <= FIRE_SCAN_RADIUS_XZ; dz += 2) {
                for (int dy = -FIRE_SCAN_RADIUS_Y; dy <= FIRE_SCAN_RADIUS_Y; dy += 2) {
                    for (int ox = 0; ox < 2; ox++) {
                        for (int oy = 0; oy < 2; oy++) {
                            for (int oz = 0; oz < 2; oz++) {
                                p.set(cx + dx + ox, cy + dy + oy, cz + dz + oz);
                                if (isFire(level.getBlockState(p))) {
                                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isFire(BlockState state) {
        return state.getBlock() instanceof BaseFireBlock
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE);
    }
}