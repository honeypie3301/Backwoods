package net.mcreator.thebackwoods.entity;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import net.mcreator.thebackwoods.procedures.RotOnInitialEntitySpawnProcedure;
import net.mcreator.thebackwoods.procedures.RotOnEntityTickUpdateProcedure;
import net.mcreator.thebackwoods.procedures.RotEntityIsHurtProcedure;
import net.mcreator.thebackwoods.procedures.RotEntityDiesProcedure;
import net.mcreator.thebackwoods.init.TheBackwoodsModItems;

import javax.annotation.Nullable;

public class RotEntity extends Monster {
	public static final EntityDataAccessor<Integer> DATA_mineProgress = SynchedEntityData.defineId(RotEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_mineX = SynchedEntityData.defineId(RotEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_mineY = SynchedEntityData.defineId(RotEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_mineZ = SynchedEntityData.defineId(RotEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_animationTimer = SynchedEntityData.defineId(RotEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DATA_sonicCooldown = SynchedEntityData.defineId(RotEntity.class, EntityDataSerializers.INT);

	public RotEntity(EntityType<RotEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
		refreshDimensions();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_mineProgress, 0);
		builder.define(DATA_mineX, 0);
		builder.define(DATA_mineY, 0);
		builder.define(DATA_mineZ, 0);
		builder.define(DATA_animationTimer, 0);
		builder.define(DATA_sonicCooldown, 0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, (float) 6));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, false) {
			@Override
			protected boolean canPerformAttack(LivingEntity entity) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
			}
		});
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, true, false));
		this.goalSelector.addGoal(4, new BreakDoorGoal(this, e -> true));
	}

	@Override
	public Vec3 getPassengerRidingPosition(Entity entity) {
		return super.getPassengerRidingPosition(entity).add(0, -0.35F, 0);
	}

	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHitIn) {
		super.dropCustomDeathLoot(serverLevel, source, recentlyHitIn);
		this.spawnAtLocation(new ItemStack(TheBackwoodsModItems.SEEP.get()));
	}

	@Override
	public SoundEvent getAmbientSound() {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_idle"));
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_step")), 0.15f, 1);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:splinter_hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("intentionally_empty"));
	}

	@Override
	public boolean hurt(DamageSource damagesource, float amount) {
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		Level world = this.level();
		Entity entity = this;
		Entity sourceentity = damagesource.getEntity();
		Entity immediatesourceentity = damagesource.getDirectEntity();

		RotEntityIsHurtProcedure.execute(world, x, y, z, entity);
		if (damagesource.is(DamageTypes.IN_FIRE))
			return false;
		if (damagesource.is(DamageTypes.FALL))
			return false;
		if (damagesource.is(DamageTypes.EXPLOSION) || damagesource.is(DamageTypes.PLAYER_EXPLOSION))
			return false;
		return super.hurt(damagesource, amount);
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		RotEntityDiesProcedure.execute(source.getEntity());
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata) {
		SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata);
		RotOnInitialEntitySpawnProcedure.execute(world, this);
		return retval;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("DatamineProgress", this.entityData.get(DATA_mineProgress));
		compound.putInt("DatamineX", this.entityData.get(DATA_mineX));
		compound.putInt("DatamineY", this.entityData.get(DATA_mineY));
		compound.putInt("DatamineZ", this.entityData.get(DATA_mineZ));
		compound.putInt("DataanimationTimer", this.entityData.get(DATA_animationTimer));
		compound.putInt("DatasonicCooldown", this.entityData.get(DATA_sonicCooldown));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("DatamineProgress"))
			this.entityData.set(DATA_mineProgress, compound.getInt("DatamineProgress"));
		if (compound.contains("DatamineX"))
			this.entityData.set(DATA_mineX, compound.getInt("DatamineX"));
		if (compound.contains("DatamineY"))
			this.entityData.set(DATA_mineY, compound.getInt("DatamineY"));
		if (compound.contains("DatamineZ"))
			this.entityData.set(DATA_mineZ, compound.getInt("DatamineZ"));
		if (compound.contains("DataanimationTimer"))
			this.entityData.set(DATA_animationTimer, compound.getInt("DataanimationTimer"));
		if (compound.contains("DatasonicCooldown"))
			this.entityData.set(DATA_sonicCooldown, compound.getInt("DatasonicCooldown"));
	}

	@Override
	public void baseTick() {
		super.baseTick();
		RotOnEntityTickUpdateProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
	}

	@Override
	public boolean canDrownInFluidType(FluidType type) {
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		Level world = this.level();
		Entity entity = this;
		return false;
	}

	@Override
	public boolean isPushedByFluid() {
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		Level world = this.level();
		Entity entity = this;
		return false;
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return super.getDefaultDimensions(pose).scale(1.2f);
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
		builder = builder.add(Attributes.MAX_HEALTH, 550);
		builder = builder.add(Attributes.ARMOR, 15);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 18);
		builder = builder.add(Attributes.FOLLOW_RANGE, 64);
		builder = builder.add(Attributes.STEP_HEIGHT, 1.5);
		builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
		builder = builder.add(Attributes.ATTACK_KNOCKBACK, 0.6);
		return builder;
	}
}