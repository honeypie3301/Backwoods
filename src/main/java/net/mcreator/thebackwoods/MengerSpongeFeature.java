package net.mcreator.thebackwoods.world.feature;

// 1.21.1 neoforge compatible
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MengerSpongeFeature extends Feature<NoneFeatureConfiguration> {

    public MengerSpongeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level  = context.level();
        BlockPos      origin = context.origin();

        // --- CONFIGURABLE BOUNDS (81x81x81) ---
        int bottomY = -62; 
        int topY    = 18;        
        int spongeSize = 81;     
        // --------------------------------------

        BlockState plankState = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState airState   = Blocks.AIR.defaultBlockState();

        // Direct memory array target
        ChunkAccess chunk = level.getChunk(origin);
        int minX = origin.getX() & (~15);
        int minZ = origin.getZ() & (~15);

        for (int rx = 0; rx < 16; rx++) {
            for (int rz = 0; rz < 16; rz++) {
                int blockX = minX + rx;
                int blockZ = minZ + rz;

                for (int currentY = bottomY; currentY <= topY; currentY++) {
                    BlockPos targetPos = new BlockPos(blockX, currentY, blockZ);

                    // Protection layer check: Don't let the sponge slice into existing floor structures
                    if (currentY <= -59) {
                        BlockState existingBlock = chunk.getBlockState(targetPos);
                        if (existingBlock.is(Blocks.BEDROCK) || existingBlock.is(Blocks.OAK_PLANKS)) {
                            continue;
                        }
                    }

                    // Align the global coordinates to our 81x81x81 grid sectors
                    int fx = Math.floorMod(blockX, spongeSize);
                    int fy = currentY - bottomY; 
                    int fz = Math.floorMod(blockZ, spongeSize);

                    // Evaluate if this specific 3D point belongs inside the fractal matrix
                    if (isMengerSpongeSolid(fx, fy, fz)) {
                        chunk.setBlockState(targetPos, plankState, false); // false for 1.21.1 physics bypass
                    } else {
                        chunk.setBlockState(targetPos, airState, false);
                    }
                }
            }
        }

        return true;
    }

    private boolean isMengerSpongeSolid(int x, int y, int z) {
        while (x > 0 || y > 0 || z > 0) {
            int checkX = x % 3;
            int checkY = y % 3;
            int checkZ = z % 3;

            int centerCount = 0;
            if (checkX == 1) centerCount++;
            if (checkY == 1) centerCount++;
            if (checkZ == 1) centerCount++;

            if (centerCount >= 2) {
                return false;
            }

            x /= 3;
            y /= 3;
            z /= 3;
        }
        return true;
    }
}