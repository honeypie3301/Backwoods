package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.thebackwoods.init.TheBackwoodsModBlocks;
import net.mcreator.thebackwoods.entity.LogSplinterEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

@EventBusSubscriber
public class LogSplinterOnEntityTickUpdateProcedure {

	// Higher = must look more directly at Log Splinter to freeze it, lower = wider freeze cone.
	private static final double WATCH_DOT_THRESHOLD = 0.5;

	// Log Splinter base move speed while active.
	private static final double ACTIVE_MOVE_SPEED = 0.325;

	// Mining formula: threshold = destroySpeed * MINE_SPEED_MULTIPLIER + MINE_SPEED_BASE.
	// Increase these to make mining slower.
	private static final float MINE_SPEED_MULTIPLIER = 40f;
	private static final float MINE_SPEED_BASE = 40f;

	// Blocks with destroy speed >= this are treated as too hard to mine.
	private static final float MAX_BREAKABLE_HARDNESS = 50f;

	// Log Splinter scan radius around ticking entity.
	private static final double LOG_SPLINTER_SCAN_RADIUS = 28;

	// Log Splinter target range for finding a player.
	private static final double TARGET_RANGE = 56;

	// Forward mining ray distance.
	private static final double MINE_RAY_DISTANCE = 3.0;

	// Ash Rose item wilt time in ticks for Log Splinter.
	private static final double ROSE_WILT_TICKS = 750;

	// Nearby Ash Rose block scan box around Log Splinter.
	private static final int ROSE_SCAN_XZ = 6;
	private static final int ROSE_SCAN_Y = 3;

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
		if (!(entity instanceof LogSplinterEntity))
        		return;

		final Vec3 center = new Vec3(x, y, z);

		for (LogSplinterEntity logSplinter : world.getEntitiesOfClass(LogSplinterEntity.class, new AABB(center, center).inflate(LOG_SPLINTER_SCAN_RADIUS), e -> true)
				.stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList()) {

			Player foundPlayer = (Player) findEntityInWorldRange(world, Player.class, logSplinter.getX(), logSplinter.getY(), logSplinter.getZ(), TARGET_RANGE);
			if (foundPlayer == null)
				continue;

			int frozenByRose = logSplinter.getEntityData().get(LogSplinterEntity.DATA_frozenByRose);

			Vec3 toLogSplinter = logSplinter.getEyePosition().subtract(foundPlayer.getEyePosition()).normalize();
			double dot = foundPlayer.getLookAngle().normalize().dot(toLogSplinter);
			boolean facing = dot > WATCH_DOT_THRESHOLD;
			boolean canSee = foundPlayer.hasLineOfSight(logSplinter);
			boolean isWatched = facing && canSee;

			if (isWatched || frozenByRose == 1) {
				setSpeed(logSplinter, 0);
				if (logSplinter instanceof Mob mob) {
					mob.getNavigation().stop();
				}
				continue;
			}

			logSplinter.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(foundPlayer.getX(), foundPlayer.getEyeY(), foundPlayer.getZ()));
			setSpeed(logSplinter, ACTIVE_MOVE_SPEED);

			Vec3 logEyes = logSplinter.getEyePosition(1f);
			Vec3 logView = logSplinter.getViewVector(1f);
			Vec3 blockCheckTarget = logEyes.add(logView.scale(MINE_RAY_DISTANCE));

			HitResult hit = world.clip(new ClipContext(logEyes, blockCheckTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, logSplinter));

			if (hit.getType() == HitResult.Type.BLOCK) {
				BlockPos facePos = ((BlockHitResult) hit).getBlockPos();
				BlockPos feetPos = new BlockPos(facePos.getX(), Mth.floor(logSplinter.getY()), facePos.getZ());

				boolean canMineFeet = canMine(world, feetPos, foundPlayer);
				boolean canMineFace = canMine(world, facePos, foundPlayer);

				if (canMineFeet || canMineFace) {
					int mineProgress = logSplinter.getEntityData().get(LogSplinterEntity.DATA_mineProgress) + 1;
					logSplinter.getEntityData().set(LogSplinterEntity.DATA_mineProgress, mineProgress);

					BlockPos trackPos = canMineFeet ? feetPos : facePos;
					logSplinter.getEntityData().set(LogSplinterEntity.DATA_mineX, trackPos.getX());
					logSplinter.getEntityData().set(LogSplinterEntity.DATA_mineY, trackPos.getY());
					logSplinter.getEntityData().set(LogSplinterEntity.DATA_mineZ, trackPos.getZ());

					float speedRef = canMineFeet ? world.getBlockState(feetPos).getDestroySpeed(world, feetPos) : world.getBlockState(facePos).getDestroySpeed(world, facePos);
					float mineThreshold = speedRef * MINE_SPEED_MULTIPLIER + MINE_SPEED_BASE;

					if (mineProgress > mineThreshold) {
						if (canMineFeet) {
							world.destroyBlock(feetPos, false);
							if (world instanceof Level level) {
								level.updateNeighborsAt(feetPos, level.getBlockState(feetPos).getBlock());
							}
						}
						if (canMineFace) {
							world.destroyBlock(facePos, false);
							if (world instanceof Level level) {
								level.updateNeighborsAt(facePos, level.getBlockState(facePos).getBlock());
							}
						}
						if (logSplinter instanceof Mob mob) {
							mob.getNavigation().stop();
						}
						logSplinter.getEntityData().set(LogSplinterEntity.DATA_mineProgress, 0);
					}
				} else {
					logSplinter.getEntityData().set(LogSplinterEntity.DATA_mineProgress, 0);
				}
			} else {
				logSplinter.getEntityData().set(LogSplinterEntity.DATA_mineProgress, 0);

				double heightDiff = foundPlayer.getY() - logSplinter.getY();
				double horizontalDist = logSplinter.position().distanceTo(foundPlayer.position());

				if (heightDiff >= 1.4 && horizontalDist < 12) {
					if (world instanceof ServerLevel serverLevel) {
						CommandSourceStack src = new CommandSourceStack(CommandSource.NULL, new Vec3(logSplinter.getX(), logSplinter.getY(), logSplinter.getZ()), Vec2.ZERO, serverLevel, 4, "", Component.literal(""), serverLevel.getServer(), null).withSuppressedOutput();
						serverLevel.getServer().getCommands().performPrefixedCommand(src, "fill ~ ~-1 ~ ~ ~-1 ~ minecraft:oak_planks replace minecraft:air");
						serverLevel.getServer().getCommands().performPrefixedCommand(src, "fill ~ ~ ~ ~ ~1 ~ minecraft:air replace minecraft:oak_planks");
					}
					world.destroyBlock(BlockPos.containing(logSplinter.getX(), logSplinter.getY() + 2, logSplinter.getZ()), false);
					world.destroyBlock(BlockPos.containing(logSplinter.getX(), logSplinter.getY() + 3, logSplinter.getZ()), false);
					if (logSplinter.onGround()) {
						logSplinter.setDeltaMovement(new Vec3(logSplinter.getDeltaMovement().x(), 0.4, logSplinter.getDeltaMovement().z()));
					}
					logSplinter.fallDistance = 0;
				} else if (heightDiff > -0.5 && heightDiff < 2.5) {
					Vec3 look = logSplinter.getLookAngle();
					BlockPos bridgePos = BlockPos.containing(logSplinter.getX() + look.x, logSplinter.getY() - 1, logSplinter.getZ() + look.z);
					if (!(world.getBlockFloorHeight(bridgePos) > 0)) {
						world.setBlock(bridgePos, Blocks.OAK_WOOD.defaultBlockState(), 3);
					}
				}
			}

			boolean foundRose = checkHeldRose(foundPlayer, logSplinter, world, foundPlayer.getX(), foundPlayer.getY(), foundPlayer.getZ());

			if (!foundRose && logSplinter.tickCount % 5 == 0) {
				foundRose = checkNearbyRoseBlocks(world, logSplinter);
			}

			if (foundRose) {
				setSpeed(logSplinter, 0);
				if (logSplinter instanceof Mob mob) {
					mob.getNavigation().stop();
				}
			}
		}
	}

	private static void setSpeed(LivingEntity entity, double speed) {
		if (entity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
			entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
		}
	}

	private static boolean canMine(LevelAccessor world, BlockPos pos, Player player) {
		float speed = world.getBlockState(pos).getDestroySpeed(world, pos);
		if (speed < 0 || speed >= MAX_BREAKABLE_HARDNESS)
			return false;
		if (pos.getY() == (int) (player.getY() - 2))
			return false;
		return !world.getBlockState(pos).isAir();
	}

	private static boolean checkHeldRose(Entity holder, LogSplinterEntity logSplinter, LevelAccessor world, double x, double y, double z) {
		if (!(holder instanceof LivingEntity living))
			return false;

		ItemStack main = living.getMainHandItem();
		ItemStack off = living.getOffhandItem();

		boolean mainIsRose = main.getItem() == TheBackwoodsModBlocks.ASH_ROSE.get().asItem();
		boolean offIsRose = off.getItem() == TheBackwoodsModBlocks.ASH_ROSE.get().asItem();

		if (!mainIsRose && !offIsRose)
			return false;

		setSpeed(logSplinter, 0);
		if (logSplinter instanceof Mob mob) {
			mob.getNavigation().stop();
		}

		if (mainIsRose)
			tickRoseItem(main, world, x, y, z);
		if (offIsRose)
			tickRoseItem(off, world, x, y, z);

		return true;
	}

	private static void tickRoseItem(ItemStack rose, LevelAccessor world, double x, double y, double z) {
		double wiltTimer = rose.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("wilt_timer") + 1;
		CustomData.update(DataComponents.CUSTOM_DATA, rose, tag -> tag.putDouble("wilt_timer", wiltTimer));

		if (wiltTimer > ROSE_WILT_TICKS) {
			if (world instanceof Level level) {
				if (!level.isClientSide()) {
					level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.fire.extinguish")), SoundSource.NEUTRAL, 1, 1);
				} else {
					level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.fire.extinguish")), SoundSource.NEUTRAL, 1, 1, false);
				}
			}
			rose.shrink(1);
			CustomData.update(DataComponents.CUSTOM_DATA, rose, tag -> tag.putDouble("wilt_timer", 0));
		}
	}

	private static boolean checkNearbyRoseBlocks(LevelAccessor world, LogSplinterEntity logSplinter) {
		for (int sx = -ROSE_SCAN_XZ; sx < ROSE_SCAN_XZ; sx++) {
			for (int sy = -ROSE_SCAN_Y; sy < ROSE_SCAN_Y; sy++) {
				for (int sz = -ROSE_SCAN_XZ; sz < ROSE_SCAN_XZ; sz++) {
					BlockPos check = BlockPos.containing(logSplinter.getX() + sx, logSplinter.getY() + sy, logSplinter.getZ() + sz);
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