package net.mcreator.thebackwoods.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class BlindspotSplinterSpawnerBlockAddedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		world.scheduleTick(BlockPos.containing(x, y, z), world.getBlockState(BlockPos.containing(x, y, z)).getBlock(), 100);
	}
}