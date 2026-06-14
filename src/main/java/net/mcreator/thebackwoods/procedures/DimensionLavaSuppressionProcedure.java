package net.mcreator.thebackwoods.procedures;
// 1.21.8 neo / 1.21.1 neo

import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class DimensionLavaSuppressionProcedure {

    // --- TUNABLE CONSTANTS ---
    private static final int CHECK_INTERVAL_TICKS = 3;           // Scan roughly 7 times per second
    private static final int SCAN_RADIUS_XZ = 96;                
    private static final int SCAN_RADIUS_Y = 24;                 // Optimized vertical range (25 blocks deep)
    private static final int Y_THRESHOLD = -54;                  // only suppress at or below this Y
    private static final int MAX_CHECKS_PER_PLAYER_SCAN = 1000000; // High budget so the scan actually completes
    private static final int MAX_REMOVALS_PER_PLAYER_SCAN = 8192; // Allows removing vast sections of lakes at once
    
    private static final long PLAYER_LAVA_PROTECTION_TICKS = 20L * 60L * 10L; // 10 minutes
    private static final int PROTECTION_CLEANUP_INTERVAL_TICKS = 20 * 30;     // every 30 seconds
    // -------------------------

    // OPTIMIZATION: Store primitives (Long coordinates) instead of heavy concatenated Strings
    private static final Map<Long, Long> PLAYER_PLACED_LAVA = new ConcurrentHashMap<>();

    private static final ResourceKey<Level> BACKWOODS = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:backwoods"));
    private static final ResourceKey<Level> THE_GRAIN = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:the_grain"));
    private static final ResourceKey<Level> THE_SUB_STRATA = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:the_sub_strata"));

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getPlacedBlock().getFluidState().is(Fluids.LAVA)) return;
        if (!(event.getLevel() instanceof Level level)) return;

        ResourceKey<Level> dim = level.dimension();
        if (!isTargetDimension(dim)) return;

        PLAYER_PLACED_LAVA.put(event.getPos().asLong(), level.getGameTime());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        Level level = player.level();
        ResourceKey<Level> dim = level.dimension();

        if (!isTargetDimension(dim)) return;

        // Trigger scan if player is within range of the vertical threshold zone
        if (player.getY() > Y_THRESHOLD + SCAN_RADIUS_Y) return;

        cleanupOldPlayerPlacedLava(level);

        BlockPos center = player.blockPosition();
        
        // OPTIMIZATION: One reusable position pointer prevents millions of temporary objects from lagging garbage collection
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int checked = 0;
        int removed = 0;

        for (int dy = -SCAN_RADIUS_Y; dy <= SCAN_RADIUS_Y; dy++) {
            int absY = center.getY() + dy;
            if (absY > Y_THRESHOLD) continue;

            for (int dx = -SCAN_RADIUS_XZ; dx <= SCAN_RADIUS_XZ; dx++) {
                for (int dz = -SCAN_RADIUS_XZ; dz <= SCAN_RADIUS_XZ; dz++) {
                    checked++;
                    if (checked > MAX_CHECKS_PER_PLAYER_SCAN) return;

                    mutablePos.set(center.getX() + dx, absY, center.getZ() + dz);

                    // OPTIMIZATION: Skip safely if the player's giant radius touches un-generated chunks
                    if (!level.hasChunkAt(mutablePos)) continue;

                    // Ignore if this specific coordinate belongs to player-placed lava
                    if (PLAYER_PLACED_LAVA.containsKey(mutablePos.asLong())) continue;

                    BlockState state = level.getBlockState(mutablePos);

                    if (state.is(Blocks.LAVA) || state.getFluidState().is(Fluids.LAVA)) {
                        level.setBlock(mutablePos, Blocks.OAK_PLANKS.defaultBlockState(), 2);
                        removed++;

                        if (removed >= MAX_REMOVALS_PER_PLAYER_SCAN) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private static void cleanupOldPlayerPlacedLava(Level level) {
        if (level.getGameTime() % PROTECTION_CLEANUP_INTERVAL_TICKS != 0) return;

        long now = level.getGameTime();
        PLAYER_PLACED_LAVA.entrySet().removeIf(entry -> now - entry.getValue() > PLAYER_LAVA_PROTECTION_TICKS);
    }

    private static boolean isTargetDimension(ResourceKey<Level> dim) {
        return dim.equals(BACKWOODS) || dim.equals(THE_GRAIN) || dim.equals(THE_SUB_STRATA);
    }
}