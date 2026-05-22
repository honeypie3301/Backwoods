package net.mcreator.thebackwoods.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.mcreator.thebackwoods.entity.BlindspotSplinterEntity;

public class BlindspotSplinterNaturalEntitySpawningConditionProcedure {
	public static boolean execute(LevelAccessor world, double x, double y, double z) {
		if (world.getBiome(BlockPos.containing(x, y, z)).is(ResourceLocation.parse("the_backwoods:uniform_grain"))) {
			if (!(!world.getEntitiesOfClass(BlindspotSplinterEntity.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(1000 / 2d), e -> true).isEmpty()) && world.getBlockState(BlockPos.containing(x, y - 1, z)).canOcclude()) {
				return true;
			}
		}
		if (world.getBiome(BlockPos.containing(x, y, z)).is(ResourceLocation.parse("the_backwoods:labyrinthine_grids"))) {
			if (!(!world.getEntitiesOfClass(BlindspotSplinterEntity.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(192 / 2d), e -> true).isEmpty()) && world.getBlockState(BlockPos.containing(x, y - 1, z)).canOcclude()) {
				return true;
			}
		}
		if (world.getBiome(BlockPos.containing(x, y, z)).is(ResourceLocation.parse("the_backwoods:pillar_thicket"))) {
			if (!(!world.getEntitiesOfClass(BlindspotSplinterEntity.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(192 / 2d), e -> true).isEmpty()) && world.getBlockState(BlockPos.containing(x, y - 1, z)).canOcclude()) {
				return true;
			}
		}
		return false;
	}
}