package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
// 1.21.1 neoforge
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
import net.minecraft.world.InteractionHand;
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
import net.mcreator.thebackwoods.entity.BlindspotSplinterEntity;

import javax.annotation.Nullable;
import java.util.Comparator;

@EventBusSubscriber
public class BlindspotSplinterOnEntityTickUpdateProcedure {

	private static final double WATCH_DOT_THRESHOLD = 0.5;
	private static final double ACTIVE_MOVE_SPEED = 0.335;
	private static final double DEGRADED_MOVE_SPEED = 0.20;
	private static final float MINE_SPEED_MULTIPLIER = 50f;
	private static final float MINE_SPEED_BASE = 50f;
	private static final float DEGRADED_MINE_SPEED_MULTIPLIER = 100f;
	private static final float DEGRADED_MINE_SPEED_BASE = 100f;
	private static final float MAX_BREAKABLE_HARDNESS = 50f;
	private static final double TARGET_RANGE = 56;
	private static final double SPLINTER_SCAN_RADIUS = 48;
	private static final double MINE_RAY_DISTANCE = 2.0;
	private static final double ROSE_WILT_TICKS = 150;
	private static final int ROSE_SCAN_XZ = 6;
	private static final int ROSE_SCAN_Y = 3;
	private static final int RAGE_WATCH_THRESHOLD = 590;
	private static final int RAGE_THRESHOLD_HIT_BONUS = 100;
	private static final double RAGE_ESCAPE_RANGE = 16.0;

	private static final String K_RAGE_BONUS = "rage_watch_bonus";
	private static final String K_LAST_HURT_TIME = "rage_last_hurt_time";

	// age thresholds from display condition procedures
	private static final int AGE_THIRD_TO_LAST = 504000;  // stage 7 - slow mining + speed
	private static final int AGE_SECOND_TO_LAST = 576000; // stage 8 - stop bridging
	private static final int AGE_LAST = 648000;           // stage 9 - die

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute() {
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null || !(entity instanceof BlindspotSplinterEntity))
			return;

		if (entity.isPassenger()) {
			Entity vehicle = entity.getVehicle();
			if (vehicle instanceof net.minecraft.world.entity.vehicle.Boat || vehicle instanceof net.minecraft.world.entity.vehicle.ChestBoat) {
				entity.stopRiding();
				entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.2, 0));
			}
		}

		final Vec3 center = new Vec3(x, y, z);

		for (BlindspotSplinterEntity splinter : world.getEntitiesOfClass(BlindspotSplinterEntity.class, new AABB(center, center).inflate(SPLINTER_SCAN_RADIUS), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList()) {

			int age = splinter.getEntityData().get(BlindspotSplinterEntity.DATA_Age);

			// die at last stage
			if (age >= AGE_LAST) {
				splinter.kill();
				continue;
			}

			boolean isDegraded = age >= AGE_THIRD_TO_LAST;
			boolean isCritical = age >= AGE_SECOND_TO_LAST;

			Player foundPlayer = (Player) findEntityInWorldRange(world, Player.class, splinter.getX(), splinter.getY(), splinter.getZ(), TARGET_RANGE);
			if (foundPlayer == null) {
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_watchTimer, 0);
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_isEnraged, 0);
				splinter.getPersistentData().putInt(K_RAGE_BONUS, 0);
				continue;
			}

			if (world instanceof Level level && level.isClientSide()) {
				splinter.setInvisible(isThirdPersonFrontForLocalPlayer(foundPlayer));
			}

			int frozenByRose = splinter.getEntityData().get(BlindspotSplinterEntity.DATA_frozenByRose);
			int isEnraged = splinter.getEntityData().get(BlindspotSplinterEntity.DATA_isEnraged);
			int watchTimer = splinter.getEntityData().get(BlindspotSplinterEntity.DATA_watchTimer);

			// NEW: decrease threshold by +280 bonus each time hit by player
			if (splinter.getLastHurtByMob() instanceof Player) {
				int lastSeenHurtTime = splinter.getPersistentData().getInt(K_LAST_HURT_TIME);
				int currentHurtTime = splinter.hurtTime;
				if (currentHurtTime > 0 && currentHurtTime != lastSeenHurtTime) {
					int bonus = splinter.getPersistentData().getInt(K_RAGE_BONUS) + RAGE_THRESHOLD_HIT_BONUS;
					splinter.getPersistentData().putInt(K_RAGE_BONUS, bonus);
					splinter.getPersistentData().putInt(K_LAST_HURT_TIME, currentHurtTime);
				}
			}
			int effectiveRageThreshold = Math.max(1, RAGE_WATCH_THRESHOLD - splinter.getPersistentData().getInt(K_RAGE_BONUS));

			Vec3 toSplinter = splinter.getEyePosition().subtract(foundPlayer.getEyePosition()).normalize();
			double dot = foundPlayer.getLookAngle().normalize().dot(toSplinter);
			boolean facing = dot > WATCH_DOT_THRESHOLD;
			boolean canSee = foundPlayer.hasLineOfSight(splinter);
			boolean isWatched = facing && canSee;

			double distToPlayer = splinter.position().distanceTo(foundPlayer.position());
			if (isEnraged == 1 && distToPlayer > RAGE_ESCAPE_RANGE) {
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_isEnraged, 0);
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_watchTimer, 0);
				isEnraged = 0;
			}

			if (isWatched && isEnraged == 0) {
				watchTimer++;
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_watchTimer, watchTimer);
				if (watchTimer >= effectiveRageThreshold) {
					splinter.getEntityData().set(BlindspotSplinterEntity.DATA_isEnraged, 1);
					splinter.getEntityData().set(BlindspotSplinterEntity.DATA_watchTimer, 0);
					isEnraged = 1;
				}
			} else if (!isWatched && isEnraged == 0) {
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_watchTimer, 0);
			}

			if ((isWatched && isEnraged == 0) || frozenByRose == 1) {
				setSpeed(splinter, 0);
				if (splinter instanceof Mob mob) {
					mob.getNavigation().stop();
				}
				continue;
			}

			splinter.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(foundPlayer.getX(), foundPlayer.getEyeY(), foundPlayer.getZ()));
			setSpeed(splinter, isDegraded ? DEGRADED_MOVE_SPEED : ACTIVE_MOVE_SPEED);

			Vec3 splinterEyes = splinter.getEyePosition(1f);
			Vec3 splinterView = splinter.getViewVector(1f);
			Vec3 blockCheckTarget = splinterEyes.add(splinterView.scale(MINE_RAY_DISTANCE));

			HitResult hit = world.clip(new ClipContext(splinterEyes, blockCheckTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, splinter));

			if (hit.getType() == HitResult.Type.BLOCK) {
				BlockPos facePos = ((BlockHitResult) hit).getBlockPos();
				BlockPos feetPos = new BlockPos(facePos.getX(), Mth.floor(splinter.getY()), facePos.getZ());

				boolean canMineFeet = canMine(world, feetPos, foundPlayer);
				boolean canMineFace = canMine(world, facePos, foundPlayer);

				if (canMineFeet || canMineFace) {
					int mineProgress = splinter.getEntityData().get(BlindspotSplinterEntity.DATA_mineProgress) + 1;
					splinter.getEntityData().set(BlindspotSplinterEntity.DATA_mineProgress, mineProgress);

					if (splinter.tickCount % 6 == 0) {
						splinter.swing(InteractionHand.MAIN_HAND, true);
					}

					BlockPos trackPos = canMineFeet ? feetPos : facePos;
					splinter.getEntityData().set(BlindspotSplinterEntity.DATA_mineX, trackPos.getX());
					splinter.getEntityData().set(BlindspotSplinterEntity.DATA_mineY, trackPos.getY());
					splinter.getEntityData().set(BlindspotSplinterEntity.DATA_mineZ, trackPos.getZ());

					float speedRef = canMineFeet ? world.getBlockState(feetPos).getDestroySpeed(world, feetPos) : world.getBlockState(facePos).getDestroySpeed(world, facePos);
					float mineThreshold = isDegraded
						? speedRef * DEGRADED_MINE_SPEED_MULTIPLIER + DEGRADED_MINE_SPEED_BASE
						: speedRef * MINE_SPEED_MULTIPLIER + MINE_SPEED_BASE;

					if (mineProgress > mineThreshold) {
						if (canMineFeet) {
							world.destroyBlock(feetPos, false);
							if (world instanceof Level level)
								level.updateNeighborsAt(feetPos, level.getBlockState(feetPos).getBlock());
						}
						if (canMineFace) {
							world.destroyBlock(facePos, false);
							if (world instanceof Level level)
								level.updateNeighborsAt(facePos, level.getBlockState(facePos).getBlock());
						}
						if (splinter instanceof Mob mob) {
							mob.getNavigation().stop();
						}
						splinter.getEntityData().set(BlindspotSplinterEntity.DATA_mineProgress, 0);
					}
				} else {
					splinter.getEntityData().set(BlindspotSplinterEntity.DATA_mineProgress, 0);
				}
			} else {
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_mineProgress, 0);

				double heightDiff = foundPlayer.getY() - splinter.getY();
				double horizontalDist = splinter.position().distanceTo(foundPlayer.position());

				if (!isCritical) {
					if (heightDiff >= 1.4 && horizontalDist < 12) {
						if (world instanceof ServerLevel serverLevel) {
							CommandSourceStack src = new CommandSourceStack(CommandSource.NULL, new Vec3(splinter.getX(), splinter.getY(), splinter.getZ()), Vec2.ZERO, serverLevel, 4, "", Component.literal(""), serverLevel.getServer(), null).withSuppressedOutput();
							serverLevel.getServer().getCommands().performPrefixedCommand(src, "fill ~ ~-1 ~ ~ ~-1 ~ minecraft:oak_planks replace minecraft:air");
							serverLevel.getServer().getCommands().performPrefixedCommand(src, "fill ~ ~ ~ ~ ~1 ~ minecraft:air replace minecraft:oak_planks");
						}
						world.destroyBlock(BlockPos.containing(splinter.getX(), splinter.getY() + 2, splinter.getZ()), false);
						world.destroyBlock(BlockPos.containing(splinter.getX(), splinter.getY() + 3, splinter.getZ()), false);
						if (splinter.onGround()) {
							splinter.setDeltaMovement(new Vec3(splinter.getDeltaMovement().x(), 0.4, splinter.getDeltaMovement().z()));
						}
						splinter.fallDistance = 0;
					} else if (heightDiff > -0.5 && heightDiff < 2.5) {
						Vec3 look = splinter.getLookAngle();
						BlockPos bridgePos = BlockPos.containing(splinter.getX() + look.x, splinter.getY() - 1, splinter.getZ() + look.z);
						if (!(world.getBlockFloorHeight(bridgePos) > 0)) {
							world.setBlock(bridgePos, Blocks.OAK_PLANKS.defaultBlockState(), 3);
						}
					}
				}
			}

			boolean foundRose = checkHeldRose(foundPlayer, splinter, world, foundPlayer.getX(), foundPlayer.getY(), foundPlayer.getZ());
			if (!foundRose && splinter.tickCount % 5 == 0) {
				foundRose = checkNearbyRoseBlocks(world, splinter);
			}

			if (foundRose) {
				setSpeed(splinter, 0);
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_isEnraged, 0);
				splinter.getEntityData().set(BlindspotSplinterEntity.DATA_watchTimer, 0);
				splinter.getPersistentData().putInt(K_RAGE_BONUS, 0);
				if (splinter instanceof Mob mob) {
					mob.getNavigation().stop();
				}
			}
		}
	}

	private static boolean isThirdPersonFrontForLocalPlayer(Player player) {
		try {
			Object minecraft = Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null);
			Object localPlayer = minecraft.getClass().getField("player").get(minecraft);
			if (localPlayer == null || localPlayer != player)
				return false;
			Object options = minecraft.getClass().getField("options").get(minecraft);
			Object cameraType = options.getClass().getMethod("getCameraType").invoke(options);
			boolean isMirrored = (boolean) cameraType.getClass().getMethod("isMirrored").invoke(cameraType);
			boolean isFirstPerson = (boolean) cameraType.getClass().getMethod("isFirstPerson").invoke(cameraType);
			return isMirrored && !isFirstPerson;
		} catch (Throwable ignored) {
			return false;
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

	private static boolean checkHeldRose(Entity holder, BlindspotSplinterEntity splinter, LevelAccessor world, double x, double y, double z) {
		if (!(holder instanceof LivingEntity living))
			return false;

		ItemStack main = living.getMainHandItem();
		ItemStack off = living.getOffhandItem();
		boolean mainIsRose = main.getItem() == TheBackwoodsModBlocks.ASH_ROSE.get().asItem();
		boolean offIsRose = off.getItem() == TheBackwoodsModBlocks.ASH_ROSE.get().asItem();

		if (!mainIsRose && !offIsRose)
			return false;

		setSpeed(splinter, 0);
		if (splinter instanceof Mob mob) {
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
		            level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.fire.extinguish")), SoundSource.NEUTRAL, 1f, 1f);
		        } else {
		            level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.fire.extinguish")), SoundSource.NEUTRAL, 1f, 1f, false);
		        }
		    }
		    rose.shrink(1);
		    CustomData.update(DataComponents.CUSTOM_DATA, rose, tag -> tag.putDouble("wilt_timer", 0));
		}
	}

	private static boolean checkNearbyRoseBlocks(LevelAccessor world, BlindspotSplinterEntity splinter) {
		for (int sx = -ROSE_SCAN_XZ; sx < ROSE_SCAN_XZ; sx++) {
			for (int sy = -ROSE_SCAN_Y; sy < ROSE_SCAN_Y; sy++) {
				for (int sz = -ROSE_SCAN_XZ; sz < ROSE_SCAN_XZ; sz++) {
					BlockPos check = BlockPos.containing(splinter.getX() + sx, splinter.getY() + sy, splinter.getZ() + sz);
					if (world.getBlockState(check).getBlock() == TheBackwoodsModBlocks.ASH_ROSE.get()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static Entity findEntityInWorldRange(LevelAccessor world, Class<? extends Entity> clazz, double x, double y, double z, double range) {
		return world.getEntitiesOfClass(clazz, AABB.ofSize(new Vec3(x, y, z), range, range, range), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(x, y, z))).findFirst().orElse(null);
	}
}