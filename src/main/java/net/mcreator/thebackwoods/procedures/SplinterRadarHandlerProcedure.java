package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.mcreator.thebackwoods.init.TheBackwoodsModBlocks;

@EventBusSubscriber
public class SplinterRadarHandlerProcedure {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player != null && !player.level().isClientSide()) {
            Level world = player.level();
            
            // Scan every 100 ticks (5 seconds)
            if (world.getGameTime() % 100 == 0) {
                BlockPos playerPos = player.blockPosition();
                int range = 24; // This is your strict "Active" radius

                // Using a slightly tighter step to make sure we don't miss tubes
                for (int dx = -range; dx <= range; dx += 4) {
                    for (int dy = -10; dy <= 10; dy += 4) {
                        for (int dz = -range; dz <= range; dz += 4) {
                            BlockPos targetPos = playerPos.offset(dx, dy, dz);
                            
                            if (world.getBlockState(targetPos).getBlock() == TheBackwoodsModBlocks.BLINDSPOT_SPLINTER_SPAWNER.get()) {
                                // Only triggers if the block is within this radius
                                SplinterNestBlockTickProcedure.execute(world, targetPos.getX(), targetPos.getY(), targetPos.getZ());
                            }
                        }
                    }
                }
            }
        }
    }
}