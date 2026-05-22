package net.mcreator.thebackwoods.world.features;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.Mth;

public class OakStalactiteFeature extends Feature<NoneFeatureConfiguration> {
    public OakStalactiteFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Ensure we are placing under a solid ceiling
        if (!world.getBlockState(origin.above()).isSolidRender(world, origin.above())) {
            return false;
        }

        // ==========================================
        //         REFINED STALACTITE SETTINGS
        // ==========================================
        int minHeight = 8;        // Increased for length
        int heightVariation = 12; 
        float baseRadius = 1.5f + random.nextFloat() * 2.0f; // Thinner base
        float roughness = 0.4f;
        int maxAnchorDepth = 6;   // Deeper embedding for blending
        // ==========================================

        int height = minHeight + random.nextInt(heightVariation);
        BlockState logBlock = Blocks.OAK_LOG.defaultBlockState();

        // 1. Generate Body (Grow Downward)
        for (int y = 0; y < height; y++) {
            // Using a square root curve for a more "organic" taper
            float progress = (float) y / height;
            float targetRadius = baseRadius * (float) Math.pow(1.0f - progress, 1.5);

            if (targetRadius < 0.1f) break;

            int loopRange = (int) Math.ceil(targetRadius + roughness);
            for (int x = -loopRange; x <= loopRange; x++) {
                for (int z = -loopRange; z <= loopRange; z++) {
                    float noise = (float) (Math.sin(x * 1.5 + y) * Math.cos(z * 1.5 + y)) * roughness;
                    if (x * x + z * z <= (targetRadius + noise) * (targetRadius + noise)) {
                        BlockPos pos = origin.below(y).offset(x, 0, z);
                        if (world.isEmptyBlock(pos) || !world.getBlockState(pos).isSolidRender(world, pos)) {
                            world.setBlock(pos, logBlock, 2);
                        }
                    }
                }
            }
        }

        // 2. Anchor/Blending (Grow Upward into ceiling)
        int anchorRange = (int) Math.ceil(baseRadius);
        for (int x = -anchorRange; x <= anchorRange; x++) {
            for (int z = -anchorRange; z <= anchorRange; z++) {
                if (x * x + z * z <= (baseRadius * baseRadius)) {
                    for (int dy = 0; dy <= maxAnchorDepth; dy++) {
                        BlockPos checkPos = origin.above(dy).offset(x, 0, z);
                        // Force place logs into the ceiling to break up the flat plank texture
                        world.setBlock(checkPos, logBlock, 2);
                        
                        // If we break out of the ceiling into air above, stop
                        if (world.isEmptyBlock(checkPos.above())) break;
                    }
                }
            }
        }

        return true;
    }
}