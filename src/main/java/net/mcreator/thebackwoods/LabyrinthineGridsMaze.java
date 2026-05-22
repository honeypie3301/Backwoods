package net.mcreator.thebackwoods;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

public class LabyrinthineGridsMaze extends Feature<NoneFeatureConfiguration> {
    public LabyrinthineGridsMaze() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        
        int size = 16;           
        int minWallHeight = 56;
        int maxWallHeight = 100;
        int seaLevel = 63;       

        // 1. Fill the chunk with a Randomized Skyline
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                // Determine a unique height for THIS specific 1x1 column
                int columnHeight = minWallHeight + random.nextInt(maxWallHeight - minWallHeight + 1);
                
                for (int y = 0; y < columnHeight; y++) {
                    BlockPos wallPos = new BlockPos(origin.getX() + x, seaLevel + y, origin.getZ() + z);
                    world.setBlock(wallPos, Blocks.OAK_PLANKS.defaultBlockState(), 2);
                }
            }
        }

        // 2. High-Density Carver
        for (int x = 1; x < size - 1; x += 2) {
            for (int z = 1; z < size - 1; z += 2) {
                
                // Carve the current cell (We use maxWallHeight to ensure the path is clear to the top)
                carvePillar(world, origin.getX() + x, origin.getZ() + z, seaLevel, maxWallHeight);

                if (random.nextFloat() > 0.3) {
                    carvePillar(world, origin.getX() + x + 1, origin.getZ() + z, seaLevel, maxWallHeight);
                }
                
                if (random.nextFloat() > 0.3) {
                    carvePillar(world, origin.getX() + x, origin.getZ() + z + 1, seaLevel, maxWallHeight);
                }

                if (random.nextFloat() > 0.9) {
                    carvePillar(world, origin.getX() + x + 1, origin.getZ() + z + 1, seaLevel, maxWallHeight);
                }
            }
        }

        return true;
    }

    private void carvePillar(WorldGenLevel world, int x, int z, int seaLevel, int height) {
        for (int y = 0; y < height; y++) {
            // Carving air up to the maximum possible height to ensure no "ceilings" are left behind
            world.setBlock(new BlockPos(x, seaLevel + y, z), Blocks.AIR.defaultBlockState(), 2);
            world.setBlock(new BlockPos(x + 1, seaLevel + y, z), Blocks.AIR.defaultBlockState(), 2);
        }
    }
}