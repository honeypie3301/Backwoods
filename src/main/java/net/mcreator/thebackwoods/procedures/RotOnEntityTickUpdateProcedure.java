package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;

import net.mcreator.thebackwoods.entity.RotEntity;

import javax.annotation.Nullable;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber
public class RotOnEntityTickUpdateProcedure {
	private static final double TARGET_RANGE = 64.0;
	private static final double TELEPORT_MIN_GAP = 4.0;
	private static final double TELEPORT_BACK_OFFSET = 2.6;
	private static final double TELEPORT_SIDE_MIN = 1.6;
	private static final double TELEPORT_SIDE_MAX = 3.0;
	private static final double TELEPORT_MAX_VERTICAL_DIFF = 6.0;

	private static final int TP_DODGE_CD = 28;
	private static final int TP_FLANK_CD = 42;
	private static final int TP_COMBO_CD = 14;
	private static final int SONIC_CD = 145;
	private static final int SONIC_WINDUP_TICKS = 18;

	private static final double DODGE_TRIGGER_DIST = 5.8;
	private static final double DODGE_SWING_CHANCE = 0.72;
	private static final double FLANK_CHANCE = 0.22;
	private static final double BURST_CHANCE_LOW_HP = 0.012;

	private static final int MINE_REACH = 2;
	private static final int MINE_HEIGHT = 3;
	private static final int MINE_HALF_WIDTH = 1;
	private static final float MAX_BREAKABLE_HARDNESS = 60f;

	private static final String K_WOODBOUND = "the_backwoods:woodbound_entities";

	private static final String K_TP_DODGE_CD = "rot_tp_dodge_cd";
	private static final String K_TP_FLANK_CD = "rot_tp_flank_cd";
	private static final String K_TP_COMBO_CD = "rot_tp_combo_cd";
	private static final String K_SONIC_CD = "rot_sonic_cd";
	private static final String K_SONIC_WINDUP = "rot_sonic_windup";
	private static final String K_CREATIVE_MSG = "creative_msg_fired";
	private static final String K_AGE = "Age";
	private static final String K_ENRAGED = "rot_enraged";
	private static final String K_CRITICAL = "rot_critical";
	private static final String K_FLIGHT_TIMER = "rot_flight_timer";

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Pre event) {
		execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(entity instanceof RotEntity)) return;

		tickCooldown(entity, K_TP_DODGE_CD, 1);
		tickCooldown(entity, K_TP_FLANK_CD, 1);
		tickCooldown(entity, K_TP_COMBO_CD, 1);
		tickCooldown(entity, K_SONIC_CD, 1);

		Entity combatTarget = acquireTarget(world, entity, x, y, z);
		if (combatTarget == null) {
			handlePhaseModifiers(entity);
			handlePassengerAndGrowth(entity);
			return;
		}

		if (combatTarget instanceof Player p && p.getAbilities().instabuild && entity.getPersistentData().getDouble(K_CREATIVE_MSG) == 0) {
			if (!p.level().isClientSide()) p.displayClientMessage(Component.literal("You observe. It observes back."), true);
			entity.getPersistentData().putDouble(K_CREATIVE_MSG, 1);
			handlePhaseModifiers(entity);
			handlePassengerAndGrowth(entity);
			return;
		}

		lockLookAtTarget(entity, combatTarget);

		int windup = (int) entity.getPersistentData().getDouble(K_SONIC_WINDUP);
		if (windup > 0) {
			entity.getPersistentData().putDouble(K_SONIC_WINDUP, windup - 1);
			entity.setDeltaMovement(entity.getDeltaMovement().x() * 0.15, entity.getDeltaMovement().y(), entity.getDeltaMovement().z() * 0.15);

			if (world instanceof ServerLevel s && entity.tickCount % 3 == 0) {
				s.sendParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + 1.8, entity.getZ(), 10, 0.45, 0.35, 0.45, 0.01);
			}

			if (windup - 1 <= 0) {
				fireSonic(world, x, y, z, entity, 16, 16, true);
				entity.getPersistentData().putDouble(K_SONIC_CD, SONIC_CD);
			}

			handlePhaseModifiers(entity);
			handlePassengerAndGrowth(entity);
			return;
		}

		double dist = combatTarget.position().distanceTo(entity.position());

		tryPredictiveDodge(world, entity, combatTarget, dist);
		trySwarmComboTeleport(world, entity, combatTarget);
		tryFlankTeleport(world, entity, combatTarget, dist);

		if (entity.getPersistentData().getDouble(K_SONIC_CD) <= 0 && Math.random() < 0.012) {
			entity.getPersistentData().putDouble(K_SONIC_WINDUP, SONIC_WINDUP_TICKS);
			playHostileSound(world, entity.getX(), entity.getY(), entity.getZ(), "entity.warden.attack_impact", 1.7f, 0.75f);
		}

		if (entity instanceof LivingEntity liv && liv.getHealth() <= 180 && dist <= 14 && Math.random() < BURST_CHANCE_LOW_HP) {
			forceTeleportBehind(world, entity, combatTarget);
			fireSonic(world, x, y, z, entity, 15, 15, true);
			if (world instanceof Level l && !l.isClientSide()) {
				l.explode(null, entity.getX(), entity.getY(), entity.getZ(), 12.5f, Level.ExplosionInteraction.TNT);
			}
		}

		if (combatTarget instanceof LivingEntity tl && tl.hasEffect(MobEffects.NIGHT_VISION) && Math.random() < 0.14) {
			if (!tl.level().isClientSide()) tl.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, false, false));
		}

		tryFlightPunish(world, entity, combatTarget);
		handleForwardCarveMining(world, entity, combatTarget);

		handlePhaseModifiers(entity);
		handlePassengerAndGrowth(entity);
	}

	private static void lockLookAtTarget(Entity entity, Entity target) {
		entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getEyeY(), target.getZ()));
		if (entity instanceof Mob mob) {
			float yRot = (float) (Mth.atan2(target.getZ() - entity.getZ(), target.getX() - entity.getX()) * (180F / Math.PI)) - 90F;
			mob.setYRot(yRot);
			mob.setYHeadRot(yRot);
			mob.setYBodyRot(yRot);
		}
	}

	private static Entity acquireTarget(LevelAccessor world, Entity self, double x, double y, double z) {
		Entity target = (self instanceof Mob mob) ? mob.getTarget() : null;
		if (!isValidTarget(target, self)) {
			target = findEntityInWorldRange(world, LivingEntity.class, x, y, z, TARGET_RANGE);
			if (!isValidTarget(target, self)) target = null;
		}
		if (target == null && self instanceof Mob mob) mob.setTarget(null);
		return target;
	}

	private static boolean isValidTarget(Entity target, Entity self) {
	    if (target == null || !target.isAlive() || target == self) return false;
	    if (target instanceof RotEntity || target instanceof Villager || target instanceof AmbientCreature || target instanceof Animal || target instanceof Slime) return false;
	    if (target.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(K_WOODBOUND)))) return false;
	
	    return (target instanceof Player)
	            || (target instanceof Monster)
	            || (target instanceof net.minecraft.world.entity.animal.IronGolem)
	            || (target instanceof net.minecraft.world.entity.animal.SnowGolem);
	}

	private static void tryPredictiveDodge(LevelAccessor world, Entity self, Entity target, double dist) {
		if (self.getPersistentData().getDouble(K_TP_DODGE_CD) > 0) return;
		if (!(target instanceof LivingEntity tl) || dist > DODGE_TRIGGER_DIST) return;

		boolean likelySwingNow = tl.swinging;
		boolean likelySwingSoon = false;
		if (target instanceof Player p) likelySwingSoon = p.getAttackStrengthScale(0.5f) > 0.9f && dist < 4.6;

		if (!(likelySwingNow || likelySwingSoon)) return;
		if (Math.random() > DODGE_SWING_CHANCE) return;

		Vec3 look = target.getLookAngle().normalize();
		Vec3 right = new Vec3(-look.z, 0, look.x).normalize();

		double side = Mth.nextDouble(RandomSource.create(), TELEPORT_SIDE_MIN, TELEPORT_SIDE_MAX);
		if (RandomSource.create().nextBoolean()) side *= -1;

		double tx = target.getX() + right.x * side - look.x * 0.8;
		double tz = target.getZ() + right.z * side - look.z * 0.8;

		trySafeTeleportToGround(world, self, tx, tz, "entity.warden.attack_impact", 1.5f, 0.85f, K_TP_DODGE_CD, TP_DODGE_CD);
	}

	private static void tryFlankTeleport(LevelAccessor world, Entity self, Entity target, double dist) {
		if (self.getPersistentData().getDouble(K_TP_FLANK_CD) > 0) return;

		boolean noLos = true;
		if (self instanceof LivingEntity ls) noLos = !ls.hasLineOfSight(target);

		boolean shouldFlank = (dist > TELEPORT_MIN_GAP || noLos) && Math.random() < FLANK_CHANCE;
		if (!shouldFlank) return;

		Vec3 look = target.getLookAngle().normalize();
		Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
		double side = Mth.nextDouble(RandomSource.create(), TELEPORT_SIDE_MIN, TELEPORT_SIDE_MAX);
		if (RandomSource.create().nextBoolean()) side *= -1;

		double tx = target.getX() - look.x * TELEPORT_BACK_OFFSET + right.x * side;
		double tz = target.getZ() - look.z * TELEPORT_BACK_OFFSET + right.z * side;

		trySafeTeleportToGround(world, self, tx, tz, "entity.warden.attack_impact", 1.8f, 0.75f, K_TP_FLANK_CD, TP_FLANK_CD);
	}

	private static void trySwarmComboTeleport(LevelAccessor world, Entity self, Entity target) {
		if (self.getPersistentData().getDouble(K_TP_COMBO_CD) > 0) return;
		if (target.position().distanceTo(self.position()) > 16) return;

		int rotCount = countNearbyRot(world, target, 14);
		if (rotCount < 2) return;

		Vec3 f = target.getLookAngle().normalize();
		Vec3 r = new Vec3(-f.z, 0, f.x).normalize();

		double[] angles = new double[] {110, 130, 150, -110, -130, -150};
		for (double deg : angles) {
			double rad = Math.toRadians(deg);
			double cs = Math.cos(rad), sn = Math.sin(rad);

			Vec3 dir = new Vec3(f.x * cs + r.x * sn, 0, f.z * cs + r.z * sn).normalize();
			double radius = Mth.nextDouble(RandomSource.create(), 3.0, 4.5);

			double tx = target.getX() + dir.x * radius;
			double tz = target.getZ() + dir.z * radius;
			double ty = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) tx, (int) tz);

			Vec3 cand = new Vec3(tx, ty, tz);
			if (isInFrontOfTarget(target, cand)) continue;
			if (!isSafeTeleportSpot(world, tx, ty, tz, self.getY())) continue;

			teleportEntity(self, tx, ty, tz);
			playHostileSound(world, tx, ty, tz, "entity.warden.attack_impact", 1.6f, 0.8f);
			self.getPersistentData().putDouble(K_TP_COMBO_CD, TP_COMBO_CD);
			return;
		}
	}

	private static void forceTeleportBehind(LevelAccessor world, Entity self, Entity target) {
		Vec3 look = target.getLookAngle().normalize();
		double tx = target.getX() - look.x * TELEPORT_BACK_OFFSET;
		double tz = target.getZ() - look.z * TELEPORT_BACK_OFFSET;
		trySafeTeleportToGround(world, self, tx, tz, "entity.warden.attack_impact", 1.8f, 0.7f, K_TP_FLANK_CD, TP_FLANK_CD);
	}

	private static boolean isInFrontOfTarget(Entity target, Vec3 candidatePos) {
		Vec3 forward = target.getLookAngle().normalize();
		Vec3 toCandidate = candidatePos.subtract(target.position()).normalize();
		return forward.dot(toCandidate) > 0.25;
	}

	private static int countNearbyRot(LevelAccessor world, Entity centerTarget, double radius) {
		Vec3 c = centerTarget.position();
		return world.getEntitiesOfClass(RotEntity.class, new AABB(c, c).inflate(radius), e -> e.isAlive()).size();
	}

	private static void trySafeTeleportToGround(LevelAccessor world, Entity self, double targetX, double targetZ, String soundId, float vol, float pitch, String cdKey, int cdTicks) {
		double groundY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) targetX, (int) targetZ);
		if (!isSafeTeleportSpot(world, targetX, groundY, targetZ, self.getY())) return;

		teleportEntity(self, targetX, groundY, targetZ);
		playHostileSound(world, targetX, groundY, targetZ, soundId, vol, pitch);
		self.getPersistentData().putDouble(cdKey, cdTicks);
	}

	private static boolean isSafeTeleportSpot(LevelAccessor world, double x, double y, double z, double fromY) {
		if (Math.abs(y - fromY) > TELEPORT_MAX_VERTICAL_DIFF) return false;

		BlockPos feet = BlockPos.containing(x, y, z);
		BlockPos head = feet.above();
		BlockPos below = feet.below();

		BlockState feetState = world.getBlockState(feet);
		BlockState headState = world.getBlockState(head);
		BlockState belowState = world.getBlockState(below);

		if (!belowState.blocksMotion()) return false;
		if (!feetState.isAir() && !feetState.canBeReplaced()) return false;
		if (!headState.isAir() && !headState.canBeReplaced()) return false;
		return true;
	}

	private static void fireSonic(LevelAccessor world, double x, double y, double z, Entity self, float sonicDamage, float extraDamage, boolean knockback) {
		final Vec3 center = new Vec3(x, y, z);

		List<Entity> nearby = world.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(8), e -> true).stream()
				.sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList();

		for (Entity e : nearby) {
			if (e == self || e instanceof RotEntity || !(e instanceof LivingEntity)) continue;

			e.hurt(new DamageSource(world.holderOrThrow(DamageTypes.SONIC_BOOM)), sonicDamage);
			if (extraDamage > 0) e.hurt(new DamageSource(world.holderOrThrow(DamageTypes.EXPLOSION)), extraDamage);

			if (knockback) {
				double d = e.position().distanceTo(self.position());
				if (d > 0) {
					e.setDeltaMovement(new Vec3(((e.getX() - self.getX()) / d) * 2.0, 0.55, ((e.getZ() - self.getZ()) / d) * 2.0));
				}
			}

			if (e instanceof LivingEntity liv && !liv.level().isClientSide()) {
				liv.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 35, 1, false, false));
			}
		}

		ringParticles(world, self, ParticleTypes.SWEEP_ATTACK, 90, 10, 1);
		playHostileSound(world, x, y, z, "entity.warden.sonic_boom", 2.0f, 0.6f);
	}

	private static void handleForwardCarveMining(LevelAccessor world, Entity self, Entity target) {
		HitResult hit = self.level().clip(new ClipContext(
				self.getEyePosition(1f),
				self.getEyePosition(1f).add(self.getViewVector(1f).scale(3.0)),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				self
		));

		if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult)) {
			if (self instanceof RotEntity rotSet) rotSet.getEntityData().set(RotEntity.DATA_mineProgress, 0);
			return;
		}

		if (self instanceof RotEntity rotSet) {
			rotSet.getEntityData().set(RotEntity.DATA_mineProgress, rotSet.getEntityData().get(RotEntity.DATA_mineProgress) + 1);
		}

		if (self.tickCount % 6 == 0 && self instanceof LivingEntity liv) {
			liv.swing(InteractionHand.MAIN_HAND, true);
		}

		int progress = self instanceof RotEntity rot ? rot.getEntityData().get(RotEntity.DATA_mineProgress) : 0;
		if (progress < 10) return;

		Vec3 forward = self.getViewVector(1f).normalize();
		Vec3 right = new Vec3(-forward.z, 0, forward.x).normalize();
		int baseY = Mth.floor(self.getY());

		for (int depth = 1; depth <= MINE_REACH; depth++) {
			double cx = self.getX() + forward.x * depth;
			double cz = self.getZ() + forward.z * depth;

			for (int w = -MINE_HALF_WIDTH; w <= MINE_HALF_WIDTH; w++) {
				double px = cx + right.x * w;
				double pz = cz + right.z * w;

				for (int h = 0; h < MINE_HEIGHT; h++) {
					BlockPos bp = BlockPos.containing(px, baseY + h, pz);

					if (bp.getY() == (int) (target.getY() - 2)) continue;

					BlockState st = world.getBlockState(bp);
					float hard = st.getDestroySpeed(world, bp);
					if (hard < 0 || hard >= MAX_BREAKABLE_HARDNESS || st.isAir()) continue;

					world.destroyBlock(bp, false);
					if (world instanceof Level l) l.updateNeighborsAt(bp, l.getBlockState(bp).getBlock());
				}
			}
		}

		if (self instanceof RotEntity rotSet) rotSet.getEntityData().set(RotEntity.DATA_mineProgress, 0);
	}

	private static void tryFlightPunish(LevelAccessor world, Entity self, Entity target) {
		if (!(target instanceof Player p) || !p.isFallFlying()) return;

		if (target.getPersistentData().getDouble(K_FLIGHT_TIMER) == 0) {
			target.getPersistentData().putDouble(K_FLIGHT_TIMER, 35);
			playHostileSound(world, self.getX(), self.getY(), self.getZ(), "the_backwoods:rot_roar", 3f, 0.5f);
		}

		if (target.getPersistentData().getDouble(K_FLIGHT_TIMER) > 0) {
			target.getPersistentData().putDouble(K_FLIGHT_TIMER, target.getPersistentData().getDouble(K_FLIGHT_TIMER) - 1);
			if (target.getPersistentData().getDouble(K_FLIGHT_TIMER) == 0) {
				if (target instanceof LivingEntity liv && !liv.level().isClientSide()) {
					liv.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 1, false, false));
				}
				if (p.isFallFlying()) p.stopFallFlying();
			}
		}
	}

	private static void handlePhaseModifiers(Entity entity) {
		if (!(entity instanceof LivingEntity living)) return;

		double hp = living.getHealth();

		if (hp <= 180) {
			if (entity.getPersistentData().getDouble(K_ENRAGED) == 0) {
				entity.getPersistentData().putDouble(K_ENRAGED, 1);
				AttributeModifier mod = new AttributeModifier(ResourceLocation.parse("the_backwoods:speed_enraged"), 0.15, AttributeModifier.Operation.ADD_VALUE);
				if (!living.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(mod.id())) {
					living.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(mod);
				}
				if (!living.level().isClientSide()) living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 2, true, false));
			}
		} else if (entity.getPersistentData().getDouble(K_ENRAGED) == 1) {
			entity.getPersistentData().putDouble(K_ENRAGED, 0);
			living.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(ResourceLocation.parse("the_backwoods:speed_enraged"));
		}

		if (hp <= 90) {
			if (entity.getPersistentData().getDouble(K_CRITICAL) == 0) {
				entity.getPersistentData().putDouble(K_CRITICAL, 1);
				AttributeModifier mod = new AttributeModifier(ResourceLocation.parse("the_backwoods:speed_critical"), 0.25, AttributeModifier.Operation.ADD_VALUE);
				if (!living.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(mod.id())) {
					living.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(mod);
				}
				if (!living.level().isClientSide()) living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 8, true, false));
			}
		} else if (entity.getPersistentData().getDouble(K_CRITICAL) == 1) {
			entity.getPersistentData().putDouble(K_CRITICAL, 0);
			living.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(ResourceLocation.parse("the_backwoods:speed_critical"));
		}
	}

	private static void handlePassengerAndGrowth(Entity entity) {
		if (entity.isPassenger()) entity.stopRiding();

		entity.getPersistentData().putDouble(K_AGE, entity.getPersistentData().getDouble(K_AGE) + 1);
		if (entity.getPersistentData().getDouble(K_AGE) % 1200 == 0) {
			if (entity instanceof LivingEntity living && living.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
				living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(living.getAttribute(Attributes.MAX_HEALTH).getBaseValue() + 2);
				living.setHealth(living.getHealth() + 2);
			}
		}
	}

	private static void tickCooldown(Entity e, String key, int step) {
		double v = e.getPersistentData().getDouble(key);
		if (v > 0) e.getPersistentData().putDouble(key, Math.max(0, v - step));
	}

	private static void playHostileSound(LevelAccessor world, double x, double y, double z, String soundId, float volume, float pitch) {
		if (!(world instanceof Level level)) return;
		if (!level.isClientSide()) {
			level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)), SoundSource.HOSTILE, volume, pitch);
		} else {
			level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)), SoundSource.HOSTILE, volume, pitch, false);
		}
	}

	private static void ringParticles(LevelAccessor world, Entity entity, net.minecraft.core.particles.ParticleOptions particle, int points, double radius, int countEach) {
		if (!(world instanceof ServerLevel level)) return;
		for (int i = 0; i < points; i++) {
			double angle = (i / (double) points) * Math.PI * 2;
			level.sendParticles(
					particle,
					entity.getX() + Math.cos(angle) * radius,
					entity.getY() + 2,
					entity.getZ() + Math.sin(angle) * radius,
					countEach, 0, 0, 0, 0
			);
		}
	}

	private static void teleportEntity(Entity ent, double x, double y, double z) {
		ent.teleportTo(x, y, z);
		if (ent instanceof ServerPlayer sp) {
			sp.connection.teleport(x, y, z, ent.getYRot(), ent.getXRot());
		}
	}

	private static Entity findEntityInWorldRange(LevelAccessor world, Class<? extends Entity> clazz, double x, double y, double z, double range) {
		return world.getEntitiesOfClass(clazz, AABB.ofSize(new Vec3(x, y, z), range, range, range), e -> true)
				.stream()
				.sorted(Comparator.comparingDouble(e -> e.distanceToSqr(x, y, z)))
				.findFirst()
				.orElse(null);
	}
}