package net.mcreator.thebackwoods.procedures;
// 1.21.1 neo
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import net.mcreator.thebackwoods.entity.StiltWalkerEntity;
import net.mcreator.thebackwoods.entity.BlindspotSplinterEntity;
import net.mcreator.thebackwoods.init.TheBackwoodsModEntities;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber
public class StiltWalkerOnEntityTickUpdateProcedure {

	// Working stare/rage core
	private static final double WATCH_DOT_THRESHOLD = 0.35;
	private static final int WATCH_TICKS_TO_ENRAGE = 100;
	private static final double ESCAPE_RANGE = 22.0;
	private static final double TARGET_RANGE = 48.0;
	private static final double APPROACH_SPEED = 0.14;

	// Stalk cycle trigger
	private static final double APPROACH_TRIGGER = 6.0;
	private static final int STALK_MIN_TICKS = 120;
	private static final int STALK_MAX_TICKS = 260;

	// Teleport
	private static final double TELEPORT_RADIUS = 24.0;
	private static final int TELEPORT_ATTEMPTS = 20;
	private static final int REQUIRED_AIR_BLOCKS = 4;

	// Projectile dodge
	private static final double PROJECTILE_DODGE_RANGE = 8.0;
	private static final double PROJECTILE_DODGE_DOT_THRESHOLD = 0.6;
	private static final int PROJECTILE_DODGE_COOLDOWN = 10;

	// Mining
	private static final double MINE_RAY_DISTANCE = 2.0;
	private static final float MINE_SPEED_MULTIPLIER = 50f;
	private static final float MINE_SPEED_BASE = 50f;
	private static final float MAX_BREAKABLE_HARDNESS = 50f;

	// Summoned splinter cleanup
	private static final int SUMMONED_IDLE_TIMEOUT = 140;
	private static final double SUMMONED_IDLE_PLAYER_RANGE = 14.0;

	public static void execute() {
	}

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		Entity e = event.getEntity();
		if (e.level().isClientSide()) return;

		// cleanup for summoned splinters
		if (e instanceof BlindspotSplinterEntity splinter) {
			handleSummonedSplinterCleanup(splinter.level(), splinter);
			return;
		}

		if (!(e instanceof StiltWalkerEntity stilt)) return;
		execute(stilt.level(), stilt.getX(), stilt.getY(), stilt.getZ(), stilt);
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(entity instanceof StiltWalkerEntity stilt)) return;

		if (tryDodgeProjectile(world, stilt)) return;

		Player target = findNearestPlayer(world, x, y, z, TARGET_RANGE);
		if (target == null || target.isCreative() || target.isSpectator()) {
			stilt.getPersistentData().putInt("sw_watch_timer", 0);
			stilt.getPersistentData().putBoolean("sw_enraged", false);
			stilt.getPersistentData().putInt("sw_stalk_timer", 0);
			setSpeed(stilt, 0.0);
			if (stilt instanceof Mob mob) mob.getNavigation().stop();
			return;
		}

		stilt.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getEyeY(), target.getZ()));

		boolean enraged = stilt.getPersistentData().getBoolean("sw_enraged");
		int watchTimer = stilt.getPersistentData().getInt("sw_watch_timer");

		Vec3 toStilt = stilt.getBoundingBox().getCenter().subtract(target.getEyePosition());
		if (toStilt.lengthSqr() > 1.0e-8) toStilt = toStilt.normalize();
		double dot = target.getLookAngle().normalize().dot(toStilt);
		boolean watched = dot > WATCH_DOT_THRESHOLD && target.hasLineOfSight(stilt);

		double dist = stilt.distanceTo(target);

		// escape resets rage
		if (enraged && dist > ESCAPE_RANGE) {
			enraged = false;
			watchTimer = 0;
			stilt.getPersistentData().putInt("sw_stalk_timer", 0);
		}

		// stare builds rage
		if (!enraged) {
			if (watched) {
				watchTimer++;
				if (watchTimer >= WATCH_TICKS_TO_ENRAGE) {
					enraged = true;
					watchTimer = 0;
					playAlertSound(world, stilt.blockPosition());
				}
			} else {
				watchTimer = 0;
			}
		}

		stilt.getPersistentData().putBoolean("sw_enraged", enraged);
		stilt.getPersistentData().putInt("sw_watch_timer", watchTimer);

		// dormant until enraged
		if (!enraged) {
			setSpeed(stilt, 0.0);
			if (stilt instanceof Mob mob) mob.getNavigation().stop();
			return;
		}

		// enraged: stalk
		setSpeed(stilt, APPROACH_SPEED);
		if (stilt instanceof Mob mob) {
			mob.setTarget(target);
			mob.getNavigation().moveTo(target, 1.0);
		}

		// mine while approaching
		tryMineFrontBlock(world, stilt, target);

		// stalk timer logic
		int stalkTimer = stilt.getPersistentData().getInt("sw_stalk_timer") + 1;
		stilt.getPersistentData().putInt("sw_stalk_timer", stalkTimer);

		int stalkGoal = stilt.getPersistentData().getInt("sw_stalk_goal");
		if (stalkGoal <= 0) {
			stalkGoal = Mth.nextInt(stilt.getRandom(), STALK_MIN_TICKS, STALK_MAX_TICKS);
			stilt.getPersistentData().putInt("sw_stalk_goal", stalkGoal);
		}

		boolean approached = dist <= APPROACH_TRIGGER;
		boolean timerDone = stalkTimer >= stalkGoal;

		if (approached || timerDone) {
			// if player still staring at trigger moment, do punishment sequence
			if (watched) {
				target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
				playAlertSound(world, stilt.blockPosition());
				spawnReinforcements(world, stilt);
			}

			teleportAway(world, stilt);

			// reset cycle
			stilt.getPersistentData().putInt("sw_stalk_timer", 0);
			stilt.getPersistentData().putInt("sw_stalk_goal", Mth.nextInt(stilt.getRandom(), STALK_MIN_TICKS, STALK_MAX_TICKS));
			stilt.getPersistentData().putInt("sw_watch_timer", 0);
			stilt.getPersistentData().putBoolean("sw_enraged", false);
		}
	}

	private static boolean tryDodgeProjectile(LevelAccessor world, StiltWalkerEntity stilt) {
		int dodgeCooldown = stilt.getPersistentData().getInt("sw_projectile_dodge_cooldown");
		if (dodgeCooldown > 0) {
			stilt.getPersistentData().putInt("sw_projectile_dodge_cooldown", dodgeCooldown - 1);
			return false;
		}

		AABB dodgeBox = stilt.getBoundingBox().inflate(PROJECTILE_DODGE_RANGE);
		List<Projectile> projectiles = world.getEntitiesOfClass(Projectile.class, dodgeBox, Projectile::isAlive);

		Vec3 stiltCenter = stilt.getBoundingBox().getCenter();
		for (Projectile projectile : projectiles) {
			if (projectile.getOwner() == stilt) continue;

			Vec3 motion = projectile.getDeltaMovement();
			if (motion.lengthSqr() < 1.0e-6) continue;

			Vec3 projectileToStilt = stiltCenter.subtract(projectile.position());
			if (projectileToStilt.lengthSqr() < 1.0e-6) continue;

			double approachDot = motion.normalize().dot(projectileToStilt.normalize());
			if (approachDot < PROJECTILE_DODGE_DOT_THRESHOLD) continue;

			teleportAway(world, stilt);
			stilt.getPersistentData().putInt("sw_projectile_dodge_cooldown", PROJECTILE_DODGE_COOLDOWN);
			return true;
		}

		return false;
	}

	private static void tryMineFrontBlock(LevelAccessor world, StiltWalkerEntity stilt, Player target) {
		Vec3 eyes = stilt.getEyePosition(1f);
		Vec3 view = stilt.getViewVector(1f);
		HitResult hit = world.clip(new ClipContext(eyes, eyes.add(view.scale(MINE_RAY_DISTANCE)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, stilt));

		if (hit.getType() != HitResult.Type.BLOCK) {
			stilt.getPersistentData().putInt("sw_mine_progress", 0);
			return;
		}

		BlockPos facePos = ((BlockHitResult) hit).getBlockPos();
		BlockPos feetPos = new BlockPos(facePos.getX(), Mth.floor(stilt.getY()), facePos.getZ());
		BlockPos torsoPos = feetPos.above(1);

		boolean canMineFeet = canMine(world, feetPos, target);
		boolean canMineTorso = canMine(world, torsoPos, target);
		boolean canMineHead = canMine(world, facePos, target);

		if (!(canMineFeet || canMineTorso || canMineHead)) {
			stilt.getPersistentData().putInt("sw_mine_progress", 0);
			return;
		}

		int mineProgress = stilt.getPersistentData().getInt("sw_mine_progress") + 1;
		stilt.getPersistentData().putInt("sw_mine_progress", mineProgress);

		if (stilt.tickCount % 6 == 0) stilt.swing(InteractionHand.MAIN_HAND);

		float speedRef = 0f;
		if (canMineFeet) speedRef = Math.max(speedRef, world.getBlockState(feetPos).getDestroySpeed(world, feetPos));
		if (canMineTorso) speedRef = Math.max(speedRef, world.getBlockState(torsoPos).getDestroySpeed(world, torsoPos));
		if (canMineHead) speedRef = Math.max(speedRef, world.getBlockState(facePos).getDestroySpeed(world, facePos));

		float mineThreshold = speedRef * MINE_SPEED_MULTIPLIER + MINE_SPEED_BASE;

		if (mineProgress > mineThreshold) {
			if (canMineFeet) world.destroyBlock(feetPos, false);
			if (canMineTorso) world.destroyBlock(torsoPos, false);
			if (canMineHead) world.destroyBlock(facePos, false);
			stilt.getPersistentData().putInt("sw_mine_progress", 0);
		}
	}

	private static boolean canMine(LevelAccessor world, BlockPos pos, Player player) {
		float speed = world.getBlockState(pos).getDestroySpeed(world, pos);
		return speed >= 0 && speed < MAX_BREAKABLE_HARDNESS && pos.getY() != (int) (player.getY() - 2) && !world.getBlockState(pos).isAir();
	}

	private static void spawnReinforcements(LevelAccessor world, StiltWalkerEntity stilt) {
		if (!(world instanceof ServerLevel serverLevel)) return;

		int count = Mth.nextInt(stilt.getRandom(), 2, 3);
		for (int i = 0; i < count; i++) {
			double sx = stilt.getX() + (stilt.getRandom().nextDouble() - 0.5) * 2.0;
			double sy = stilt.getY();
			double sz = stilt.getZ() + (stilt.getRandom().nextDouble() - 0.5) * 2.0;

			BlindspotSplinterEntity splinter = new BlindspotSplinterEntity(TheBackwoodsModEntities.BLINDSPOT_SPLINTER.get(), serverLevel);
			splinter.moveTo(sx, sy, sz, stilt.getYRot(), stilt.getXRot());
			splinter.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(splinter.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
			splinter.getEntityData().set(BlindspotSplinterEntity.DATA_isEnraged, 1);

			splinter.getPersistentData().putBoolean("bw_stilt_summoned", true);
			splinter.getPersistentData().putInt("bw_stilt_idle_ticks", 0);

			serverLevel.addFreshEntity(splinter);
		}
	}

	private static void handleSummonedSplinterCleanup(LevelAccessor world, BlindspotSplinterEntity splinter) {
		if (!(world instanceof ServerLevel serverLevel)) return;
		if (!splinter.getPersistentData().getBoolean("bw_stilt_summoned")) return;

		Player nearby = findNearestPlayer(world, splinter.getX(), splinter.getY(), splinter.getZ(), SUMMONED_IDLE_PLAYER_RANGE);

		int idle = splinter.getPersistentData().getInt("bw_stilt_idle_ticks");
		idle = (nearby == null) ? idle + 1 : 0;
		splinter.getPersistentData().putInt("bw_stilt_idle_ticks", idle);

		if (idle >= SUMMONED_IDLE_TIMEOUT) {
			serverLevel.sendParticles(ParticleTypes.SMOKE, splinter.getX(), splinter.getY() + 0.8, splinter.getZ(), 16, 0.25, 0.4, 0.25, 0.01);
			serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, splinter.getX(), splinter.getY() + 0.8, splinter.getZ(), 8, 0.2, 0.35, 0.2, 0.005);
			splinter.discard();
		}
	}

	private static void teleportAway(LevelAccessor world, StiltWalkerEntity stilt) {
		for (int i = 0; i < TELEPORT_ATTEMPTS; i++) {
			double tx = stilt.getX() + (stilt.getRandom().nextDouble() - 0.5) * 2.0 * TELEPORT_RADIUS;
			double ty = stilt.getY() + Mth.nextInt(stilt.getRandom(), -4, 6);
			double tz = stilt.getZ() + (stilt.getRandom().nextDouble() - 0.5) * 2.0 * TELEPORT_RADIUS;

			BlockPos base = BlockPos.containing(tx, ty, tz);
			if (!isValidTeleportSpot(world, base)) continue;

			stilt.teleportTo(tx, ty, tz);
			playEndermanTeleportSound(world, base);
			if (stilt instanceof Mob mob) mob.getNavigation().stop();
			return;
		}
	}

	private static boolean isValidTeleportSpot(LevelAccessor world, BlockPos base) {
		if (world.getBlockState(base.below()).isAir()) return false;
		for (int i = 0; i < REQUIRED_AIR_BLOCKS; i++) {
			if (!world.getBlockState(base.above(i)).isAir()) return false;
		}
		return true;
	}

	private static void playAlertSound(LevelAccessor world, BlockPos pos) {
		if (world instanceof Level level) {
			level.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 2.0f, 0.75f);
		}
	}

	private static void playEndermanTeleportSound(LevelAccessor world, BlockPos pos) {
		if (world instanceof Level level) {
			level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.6f, 0.7f);
		}
	}

	private static void setSpeed(LivingEntity entity, double speed) {
		if (entity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
			entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
		}
	}

	private static Player findNearestPlayer(LevelAccessor world, double x, double y, double z, double range) {
		List<Player> players = world.getEntitiesOfClass(Player.class,
				AABB.ofSize(new Vec3(x, y, z), range, range, range),
				p -> !p.isCreative() && !p.isSpectator());

		return players.stream()
				.min(Comparator.comparingDouble(p -> p.distanceToSqr(x, y, z)))
				.orElse(null);
	}
}