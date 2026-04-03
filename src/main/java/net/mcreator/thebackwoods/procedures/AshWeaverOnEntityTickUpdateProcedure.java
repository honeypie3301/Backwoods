package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;

import net.mcreator.thebackwoods.init.TheBackwoodsModBlocks;
import net.mcreator.thebackwoods.entity.AshWeaverEntity;
import net.mcreator.thebackwoods.entity.SplinterEntity;
import net.mcreator.thebackwoods.entity.LogSplinterEntity;
import net.mcreator.thebackwoods.entity.HollowEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

@EventBusSubscriber
public class AshWeaverOnEntityTickUpdateProcedure {

	// Player target scan range for movement and rose placement.
	private static final double PLAYER_RANGE = 32.0;

	// Threat scan range (Splinter, Log Splinter, Hollow) required to allow rose placement.
	private static final double THREAT_RANGE = 32.0;

	// Ash Weaver follows player until within this distance.
	private static final double STOP_DISTANCE = 3.0;

	// Ash Weaver navigation speed toward player.
	private static final double MOVE_SPEED = 1.45;

	// Max real Ash Rose blocks allowed near the target player before placement is blocked.
	private static final int MAX_NEARBY_ROSES = 3;

	// Radius around player to count existing Ash Roses.
	private static final int ROSE_COUNT_RADIUS_XZ = 8;
	private static final int ROSE_COUNT_RADIUS_Y = 3;

	// Placement random offset from player.
	private static final int PLACE_OFFSET_MIN = -3;
	private static final int PLACE_OFFSET_MAX = 3;

	// Minimum spacing from other Ash Roses for new placement.
	private static final int MIN_ROSE_SPACING = 2;

	// Chance per tick to attempt placement when eligible.
	private static final double PLACE_CHANCE_PER_TICK = 1.0 / 140.0;

	// Cooldown after successful placement.
	private static final int PLACE_COOLDOWN_TICKS = 120;

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (!(entity instanceof AshWeaverEntity ashWeaver))
			return;

		Player foundPlayer = (Player) findEntityInWorldRange(world, Player.class, x, y, z, PLAYER_RANGE);
		if (foundPlayer != null && ashWeaver instanceof LivingEntity living && living.hasLineOfSight(foundPlayer)) {
			double distance = ashWeaver.position().distanceTo(foundPlayer.position());
			if (distance > STOP_DISTANCE) {
				if (ashWeaver instanceof Mob mob) {
					mob.getNavigation().moveTo(foundPlayer.getX(), Math.floor(foundPlayer.getY()), foundPlayer.getZ(), MOVE_SPEED);
				}
			} else {
				if (ashWeaver instanceof Mob mob) {
					mob.getNavigation().stop();
				}
			}
		}

		int roseCooldown = ashWeaver.getEntityData().get(AshWeaverEntity.DATA_roseCooldown);
		if (roseCooldown > 0) {
			ashWeaver.getEntityData().set(AshWeaverEntity.DATA_roseCooldown, roseCooldown - 1);
			return;
		}

		if (foundPlayer == null)
			return;

		boolean threatNearby = findEntityInWorldRange(world, SplinterEntity.class, x, y, z, THREAT_RANGE) != null
				|| findEntityInWorldRange(world, LogSplinterEntity.class, x, y, z, THREAT_RANGE) != null
				|| findEntityInWorldRange(world, HollowEntity.class, x, y, z, THREAT_RANGE) != null;

		if (!threatNearby) {
			ashWeaver.getEntityData().set(AshWeaverEntity.DATA_roseCount, 0);
			return;
		}

		int nearbyRoseCount = countNearbyAshRoses(world, foundPlayer.blockPosition(), ROSE_COUNT_RADIUS_XZ, ROSE_COUNT_RADIUS_Y);
		ashWeaver.getEntityData().set(AshWeaverEntity.DATA_roseCount, nearbyRoseCount);

		if (nearbyRoseCount >= MAX_NEARBY_ROSES)
			return;

		if (Math.random() >= PLACE_CHANCE_PER_TICK)
			return;

		int offsetX = Mth.nextInt(RandomSource.create(), PLACE_OFFSET_MIN, PLACE_OFFSET_MAX);
		int offsetZ = Mth.nextInt(RandomSource.create(), PLACE_OFFSET_MIN, PLACE_OFFSET_MAX);

		BlockPos targetPos = BlockPos.containing(foundPlayer.getX() + offsetX, foundPlayer.getY(), foundPlayer.getZ() + offsetZ);
		BlockPos belowPos = targetPos.below();

		boolean airAtTarget = world.getBlockState(targetPos).isAir();
		boolean solidBelow = world.getBlockFloorHeight(belowPos) > 0;
		boolean spacedFromOtherRoses = !hasNearbyAshRose(world, targetPos, MIN_ROSE_SPACING);

		if (airAtTarget && solidBelow && spacedFromOtherRoses) {
			world.setBlock(targetPos, TheBackwoodsModBlocks.ASH_ROSE.get().defaultBlockState(), 3);
			ashWeaver.getEntityData().set(AshWeaverEntity.DATA_roseCooldown, PLACE_COOLDOWN_TICKS);
			ashWeaver.getEntityData().set(AshWeaverEntity.DATA_roseCount, nearbyRoseCount + 1);
		}
	}

	private static int countNearbyAshRoses(LevelAccessor world, BlockPos center, int rXZ, int rY) {
		int count = 0;
		for (int sx = -rXZ; sx <= rXZ; sx++) {
			for (int sy = -rY; sy <= rY; sy++) {
				for (int sz = -rXZ; sz <= rXZ; sz++) {
					BlockPos check = center.offset(sx, sy, sz);
					if (world.getBlockState(check).getBlock() == TheBackwoodsModBlocks.ASH_ROSE.get()) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private static boolean hasNearbyAshRose(LevelAccessor world, BlockPos center, int radius) {
		for (int sx = -radius; sx <= radius; sx++) {
			for (int sy = -1; sy <= 1; sy++) {
				for (int sz = -radius; sz <= radius; sz++) {
					BlockPos check = center.offset(sx, sy, sz);
					if (world.getBlockState(check).getBlock() == TheBackwoodsModBlocks.ASH_ROSE.get()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static Entity findEntityInWorldRange(LevelAccessor world, Class<? extends Entity> clazz, double x, double y, double z, double range) {
		return world.getEntitiesOfClass(clazz, AABB.ofSize(new Vec3(x, y, z), range, range, range), e -> true)
				.stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(x, y, z))).findFirst().orElse(null);
	}
}