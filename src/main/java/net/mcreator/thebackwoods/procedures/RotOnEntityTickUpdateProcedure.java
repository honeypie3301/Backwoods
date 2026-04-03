package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;

import net.mcreator.thebackwoods.entity.RotEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

@EventBusSubscriber
public class RotOnEntityTickUpdateProcedure {
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
		Entity foundPlayer = null;
		double loop = 0;
		double particleAmount = 0;
		double xRadius = 0;
		double zRadius = 0;
		double yRadius = 0;
		double masterRadius = 0;
		if (entity instanceof RotEntity) {
			foundPlayer = findEntityInWorldRange(world, Player.class, x, y, z, 64);
			if ((entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_sonicCooldown) : 0) > 0) {
				if (entity instanceof RotEntity _datEntSetI)
					_datEntSetI.getEntityData().set(RotEntity.DATA_sonicCooldown, (int) ((entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_sonicCooldown) : 0) - 1));
			}
			if (!(foundPlayer == null)) {
				if ((foundPlayer instanceof Player _plr ? _plr.getAbilities().instabuild : false) && entity.getPersistentData().getDouble("creative_msg_fired") == 0) {
					if (foundPlayer instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal("You observe. It observes back."), true);
					entity.getPersistentData().putDouble("creative_msg_fired", 1);
				}
				if (!(foundPlayer instanceof Player _plr ? _plr.getAbilities().instabuild : false)) {
					if (entity instanceof LivingEntity _livingEntity12 && _livingEntity12.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED))
						_livingEntity12.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
								((foundPlayer instanceof LivingEntity _livingEntity11 && _livingEntity11.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED) ? _livingEntity11.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() : 0) + 0.32));
					if (Mth.nextDouble(RandomSource.create(), 0, 1) < 0.3) {
						entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((foundPlayer.getX()), (foundPlayer.getY() + 0.8), (foundPlayer.getZ())));
					}
					if ((foundPlayer instanceof LivingEntity _livEnt ? _livEnt.getArmorValue() : 0) >= 16 || foundPlayer instanceof LivingEntity _livEnt19 && _livEnt19.isFallFlying()
							|| hasEntityInInventory(foundPlayer, new ItemStack(Items.TOTEM_OF_UNDYING)) || (foundPlayer instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.TOTEM_OF_UNDYING
							|| (foundPlayer instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == Items.TOTEM_OF_UNDYING
							|| (foundPlayer instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.NETHERITE_SWORD
							|| (foundPlayer instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.NETHERITE_AXE) {
						if ((foundPlayer.position()).distanceTo((entity.position())) < 5) {
							if (Mth.nextDouble(RandomSource.create(), 0, 1) < 0.075) {
								if (foundPlayer instanceof LivingEntity _livEnt33 && _livEnt33.swinging) {
									entity.getPersistentData().putDouble("dodge_x", (entity.getX() + Mth.nextDouble(RandomSource.create(), 1, 3)));
									entity.getPersistentData().putDouble("dodge_z", (entity.getZ() + Mth.nextDouble(RandomSource.create(), 1, 3)));
									{
										Entity _ent = entity;
										_ent.teleportTo((entity.getPersistentData().getDouble("dodge_x")), (entity.getY()), (entity.getPersistentData().getDouble("dodge_z")));
										if (_ent instanceof ServerPlayer _serverPlayer)
											_serverPlayer.connection.teleport((entity.getPersistentData().getDouble("dodge_x")), (entity.getY()), (entity.getPersistentData().getDouble("dodge_z")), _ent.getYRot(), _ent.getXRot());
									}
								}
							}
						}
						if (Math.random() < (1) / ((float) 111)) {
							if ((foundPlayer.position()).distanceTo((entity.position())) > 4) {
								if (foundPlayer.getY() - world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2)) < 6) {
									{
										Entity _ent = entity;
										_ent.teleportTo((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
												(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
												(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2));
										if (_ent instanceof ServerPlayer _serverPlayer)
											_serverPlayer.connection.teleport((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
													(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
													(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2), _ent.getYRot(), _ent.getXRot());
									}
								}
							}
						}
						if (Math.random() < (1) / ((float) 250) && (entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_sonicCooldown) : 0) == 0) {
							{
								final Vec3 _center = new Vec3(x, y, z);
								for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
										.toList()) {
									if (!(entityiterator instanceof RotEntity)) {
										if (foundPlayer instanceof LivingEntity _entity && !_entity.level().isClientSide())
											_entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 25, 1, false, false));
										entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.SONIC_BOOM)), 10);
										entityiterator.setDeltaMovement(new Vec3((((entityiterator.getX() - entity.getX()) / (entityiterator.position()).distanceTo((entity.position()))) * 2), 0.55,
												(((entityiterator.getZ() - entity.getZ()) / (entityiterator.position()).distanceTo((entity.position()))) * 2)));
									}
								}
							}
							if (entity instanceof RotEntity _datEntSetI)
								_datEntSetI.getEntityData().set(RotEntity.DATA_sonicCooldown, 200);
							loop = 0;
							particleAmount = 100;
							masterRadius = 10;
							while (loop < particleAmount) {
								if (world instanceof ServerLevel _level)
									_level.sendParticles(ParticleTypes.SWEEP_ATTACK, (entity.getX() + Math.cos((loop / particleAmount) * Math.PI * 2) * masterRadius), (entity.getY() + 2),
											(entity.getZ() + Math.sin((loop / particleAmount) * Math.PI * 2) * masterRadius), 1, 0, 0, 0, 0);
								loop = loop + 1;
							}
							if (world instanceof Level _level) {
								if (!_level.isClientSide()) {
									_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.warden.sonic_boom")), SoundSource.HOSTILE, 2, (float) 0.6);
								} else {
									_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.warden.sonic_boom")), SoundSource.HOSTILE, 2, (float) 0.6, false);
								}
							}
						}
						if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= 180) {
							if ((foundPlayer.position()).distanceTo((entity.position())) <= 13) {
								if (Math.random() < (1) / ((float) 125)) {
									{
										Entity _ent = entity;
										_ent.teleportTo((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
												(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
												(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2));
										if (_ent instanceof ServerPlayer _serverPlayer)
											_serverPlayer.connection.teleport((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
													(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
													(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2), _ent.getYRot(), _ent.getXRot());
									}
									{
										final Vec3 _center = new Vec3(x, y, z);
										for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
												.toList()) {
											if (!(entityiterator instanceof RotEntity)) {
												entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.EXPLOSION)), 12);
												entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.SONIC_BOOM)), 15);
												entityiterator.setDeltaMovement(new Vec3((((entityiterator.getX() - entity.getX()) / (entityiterator.position()).distanceTo((entity.position()))) * 2), 0.55,
														(((entityiterator.getZ() - entity.getZ()) / (entityiterator.position()).distanceTo((entity.position()))) * 2)));
											}
										}
									}
									if (entity instanceof RotEntity _datEntSetI)
										_datEntSetI.getEntityData().set(RotEntity.DATA_sonicCooldown, 125);
									if (world instanceof Level _level && !_level.isClientSide())
										_level.explode(null, (entity.getX()), (entity.getY()), (entity.getZ()), 14, Level.ExplosionInteraction.TNT);
									loop = 0;
									particleAmount = 80;
									masterRadius = 10;
									while (loop < particleAmount) {
										if (world instanceof ServerLevel _level)
											_level.sendParticles(ParticleTypes.EXPLOSION, (entity.getX() + Math.cos((loop / particleAmount) * Math.PI * 2) * masterRadius), (entity.getY() + 2),
													(entity.getZ() + Math.sin((loop / particleAmount) * Math.PI * 2) * masterRadius), 1, 0, 0, 0, 0);
										if (world instanceof ServerLevel _level)
											_level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, (entity.getX() + Math.cos((loop / particleAmount) * Math.PI * 2) * masterRadius), (entity.getY() + 2),
													(entity.getZ() + Math.sin((loop / particleAmount) * Math.PI * 2) * masterRadius), 2, 0, 0, 0, 0);
										loop = loop + 1;
									}
									if (world instanceof Level _level) {
										if (!_level.isClientSide()) {
											_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.warden.sonic_boom")), SoundSource.HOSTILE, 2, (float) 0.6);
										} else {
											_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.warden.sonic_boom")), SoundSource.HOSTILE, 2, (float) 0.6, false);
										}
									}
								}
							}
						}
						if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) == 250) {
							if (foundPlayer instanceof LivingEntity _entity && !_entity.level().isClientSide())
								_entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, false, false));
							if (world instanceof Level _level) {
								if (!_level.isClientSide()) {
									_level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_roar")), SoundSource.HOSTILE, 1, (float) 0.7);
								} else {
									_level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_roar")), SoundSource.HOSTILE, 1, (float) 0.7, false);
								}
							}
							if ((foundPlayer.position()).distanceTo((entity.position())) > 13) {
								if (Math.random() < (1) / ((float) 5)) {
									if (foundPlayer.getY()
											- world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2)) < 6) {
										{
											Entity _ent = entity;
											_ent.teleportTo((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
													(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
													(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2));
											if (_ent instanceof ServerPlayer _serverPlayer)
												_serverPlayer.connection.teleport((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
														(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
														(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2), _ent.getYRot(), _ent.getXRot());
										}
									}
								}
							}
						}
						if (foundPlayer instanceof LivingEntity _livEnt160 && _livEnt160.hasEffect(MobEffects.NIGHT_VISION)) {
							if (Math.random() < (1) / ((float) 70)) {
								if (foundPlayer instanceof LivingEntity _entity && !_entity.level().isClientSide())
									_entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 1, false, false));
							}
						}
					} else {
						if (Math.random() < (1) / ((float) 80)) {
							if ((foundPlayer.position()).distanceTo((entity.position())) > 15) {
								if (foundPlayer.getY() - world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2)) < 6) {
									{
										Entity _ent = entity;
										_ent.teleportTo((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
												(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
												(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2));
										if (_ent instanceof ServerPlayer _serverPlayer)
											_serverPlayer.connection.teleport((foundPlayer.getX() - foundPlayer.getLookAngle().x * 2),
													(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (foundPlayer.getX() - foundPlayer.getLookAngle().x * 2), (int) (foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2))),
													(foundPlayer.getZ() - foundPlayer.getLookAngle().z * 2), _ent.getYRot(), _ent.getXRot());
									}
								}
							}
						}
					}
					if (foundPlayer.getPersistentData().getDouble("backwoods_time") % 60 != 0) {
						foundPlayer.getPersistentData().putDouble("backwoods_time", (foundPlayer.getPersistentData().getDouble("backwoods_time") - 0.5));
					}
					if (foundPlayer instanceof LivingEntity _livEnt186 && _livEnt186.isFallFlying()) {
						if (foundPlayer.getPersistentData().getDouble("rot_flight_timer") == 0) {
							foundPlayer.getPersistentData().putDouble("rot_flight_timer", 35);
							if (world instanceof Level _level) {
								if (!_level.isClientSide()) {
									_level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_roar")), SoundSource.HOSTILE, 3, (float) 0.5);
								} else {
									_level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_roar")), SoundSource.HOSTILE, 3, (float) 0.5, false);
								}
							}
						}
						if (foundPlayer.getPersistentData().getDouble("rot_flight_timer") > 0) {
							foundPlayer.getPersistentData().putDouble("rot_flight_timer", (foundPlayer.getPersistentData().getDouble("rot_flight_timer") - 1));
							if (foundPlayer.getPersistentData().getDouble("rot_flight_timer") == 0) {
								if (foundPlayer instanceof LivingEntity _entity && !_entity.level().isClientSide())
									_entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 1, false, false));
								if (foundPlayer instanceof Player _plr && _plr.isFallFlying()) {
									_plr.stopFallFlying();
								}
							}
						}
					}
					if (entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(4)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.BLOCK) {
						if (entity instanceof RotEntity _datEntSetI)
							_datEntSetI.getEntityData().set(RotEntity.DATA_mineProgress, (int) ((entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineProgress) : 0) + 1));
						if (entity instanceof RotEntity _datEntSetI)
							_datEntSetI.getEntityData().set(RotEntity.DATA_mineX,
									entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(4)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getBlockPos().getX());
						if (entity instanceof RotEntity _datEntSetI)
							_datEntSetI.getEntityData().set(RotEntity.DATA_mineY,
									entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(4)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getBlockPos().getY());
						if (entity instanceof RotEntity _datEntSetI)
							_datEntSetI.getEntityData().set(RotEntity.DATA_mineZ,
									entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(4)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getBlockPos().getZ());
						if ((entity instanceof RotEntity _datEntI
								? _datEntI.getEntityData().get(RotEntity.DATA_mineProgress)
								: 0) > world
										.getBlockState(BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
												entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0))
										.getDestroySpeed(world,
												BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
														entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0))
										* 12 + 15) {
							if (!((entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0) == foundPlayer.getY() - 2)) {
								if (world
										.getBlockState(BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
												entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0))
										.getDestroySpeed(world, BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
												entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0)) >= 0
										&& world.getBlockState(BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
												entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0))
												.getDestroySpeed(world,
														BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
																entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0,
																entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0)) < 50) {
									world.destroyBlock(BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
											entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0), false);
									if (world instanceof Level _level)
										_level.updateNeighborsAt(
												BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
														entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0),
												_level.getBlockState(BlockPos.containing(entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineX) : 0,
														entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineY) : 0, entity instanceof RotEntity _datEntI ? _datEntI.getEntityData().get(RotEntity.DATA_mineZ) : 0))
														.getBlock());
									if (entity instanceof RotEntity _datEntSetI)
										_datEntSetI.getEntityData().set(RotEntity.DATA_mineProgress, 0);
								}
							}
						}
					} else {
						if (entity instanceof RotEntity _datEntSetI)
							_datEntSetI.getEntityData().set(RotEntity.DATA_mineProgress, 0);
					}
				}
			}
		}
	}

	private static Entity findEntityInWorldRange(LevelAccessor world, Class<? extends Entity> clazz, double x, double y, double z, double range) {
		return (Entity) world.getEntitiesOfClass(clazz, AABB.ofSize(new Vec3(x, y, z), range, range, range), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(x, y, z))).findFirst().orElse(null);
	}

	private static boolean hasEntityInInventory(Entity entity, ItemStack itemstack) {
		if (entity instanceof Player player)
			return player.getInventory().contains(stack -> !stack.isEmpty() && ItemStack.isSameItem(stack, itemstack));
		return false;
	}
}