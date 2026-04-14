package net.mcreator.thebackwoods.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.mcreator.thebackwoods.init.TheBackwoodsModEntities;
import net.mcreator.thebackwoods.entity.BlindspotSplinterEntity;

public class SplinterNestBlockTickProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z) {
        BlockPos _pos = BlockPos.containing(x, y, z);

        // 1. THE STOP SWITCH: If no player is within 24 blocks, ABORT.
        // This ensures the spawner "stops" outside the radius.
        if (world.getEntitiesOfClass(Player.class, new AABB(_pos).inflate(24)).isEmpty()) {
            return;
        }

        // 2. THE WAVE LIMIT: Only spawn 2 if the area is empty.
        if (world.getEntitiesOfClass(BlindspotSplinterEntity.class, new AABB(_pos).inflate(6)).isEmpty()) {
            if (world instanceof ServerLevel _level) {
                for (int i = 0; i < 2; i++) {
                    TheBackwoodsModEntities.BLINDSPOT_SPLINTER.get().spawn(_level, _pos, MobSpawnType.MOB_SUMMONED);
                }
            }
        }
    }
}