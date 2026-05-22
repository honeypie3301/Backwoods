package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

@EventBusSubscriber
public class GrainTntDefenseProcedure {

    private static final int CHECK_INTERVAL_TICKS = 8;
    private static final int TNT_SCAN_RADIUS_XZ = 28;
    private static final int TNT_SCAN_RADIUS_Y = 12;
    private static final int BIG_TNT_THRESHOLD = 1; 
    private static final int PURGE_COOLDOWN_TICKS = 20 * 2; // 6 sec

    private static final String NBT_COOLDOWN_KEY = "grain_tnt_purge_cd";

    private static final ResourceKey<Level> THE_GRAIN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:the_grain")
    );

    private static final ResourceKey<Level> BACKWOODS = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("the_backwoods:backwoods")
    );

    // Set containing both dimensions for the check
    private static final Set<ResourceKey<Level>> VALID_DIMENSIONS = Set.of(THE_GRAIN, BACKWOODS);

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null) return;

        Level level = player.level();
        if (level.isClientSide()) return;
        
        // Multi-dimension check
        if (!VALID_DIMENSIONS.contains(level.dimension())) return;
        
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        // FIXED: Optional handling for NBT
        int cd = player.getPersistentData().getInt(NBT_COOLDOWN_KEY);
        
        if (cd > 0) {
            player.getPersistentData().putInt(NBT_COOLDOWN_KEY, Math.max(0, cd - CHECK_INTERVAL_TICKS));
            return;
        }

        BlockPos center = player.blockPosition();

        int tntCount = countPrimedTntInRadius(level, center);
        if (tntCount < BIG_TNT_THRESHOLD) return;

        // conceal + cue
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 5, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 5, false, false));

        // FIXED: Sound Registry handling
        BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse("minecraft:ambient.cave")).ifPresent(cave ->
            level.playSound(null, center, cave, SoundSource.AMBIENT, 1.3f, 0.85f)
        );

        // hard purge all primed TNT in zone after 2 second delay
        BlockPos capturedCenter = center;
        Level capturedLevel = level;
        net.mcreator.thebackwoods.TheBackwoodsMod.queueServerWork(40, () -> {
            purgePrimedTntInRadius(capturedLevel, capturedCenter);
        });

        player.getPersistentData().putInt(NBT_COOLDOWN_KEY, PURGE_COOLDOWN_TICKS);
    }

    private static int countPrimedTntInRadius(Level level, BlockPos center) {
        AABB box = AABB.ofSize(
                new Vec3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5),
                TNT_SCAN_RADIUS_XZ * 2.0,
                TNT_SCAN_RADIUS_Y * 2.0,
                TNT_SCAN_RADIUS_XZ * 2.0
        );
        return level.getEntitiesOfClass(PrimedTnt.class, box, t -> t.isAlive()).size();
    }

    private static void purgePrimedTntInRadius(Level level, BlockPos center) {
        AABB box = AABB.ofSize(
                new Vec3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5),
                TNT_SCAN_RADIUS_XZ * 2.0,
                TNT_SCAN_RADIUS_Y * 2.0,
                TNT_SCAN_RADIUS_XZ * 2.0
        );

        List<PrimedTnt> tnts = level.getEntitiesOfClass(PrimedTnt.class, box, t -> t.isAlive());
        for (PrimedTnt tnt : tnts) {
            tnt.discard();
        }
    }
}