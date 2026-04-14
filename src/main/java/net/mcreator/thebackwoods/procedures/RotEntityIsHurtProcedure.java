package net.mcreator.thebackwoods.procedures;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementHolder;

import net.mcreator.thebackwoods.entity.RotEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

@EventBusSubscriber
public class RotEntityIsHurtProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingDamageEvent.Pre event) {
		if (event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
		}
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		Entity attacker = null;
		Entity foundPlayer = null;
		double loop = 0;
		double particleAmount = 0;
		double masterRadius = 0;
		if (entity instanceof RotEntity) {
			attacker = (entity instanceof LivingEntity _entity) ? _entity.getLastHurtByMob() : null;
			foundPlayer = findEntityInWorldRange(world, Player.class, x, y, z, 64);
			if (!(attacker == null)) {
				if (hasEntityInInventory(attacker, new ItemStack(Items.TOTEM_OF_UNDYING)) || (attacker instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.TOTEM_OF_UNDYING
						|| (attacker instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == Items.TOTEM_OF_UNDYING) {
					if (Math.random() < (1) / ((float) 250)) {
						if (attacker instanceof Player _player && !_player.level().isClientSide())
							_player.displayClientMessage(Component.literal("I see the false life you clutch."), true);
					}
					if ((attacker.position()).distanceTo((entity.position())) > 1) {
						if (Math.random() < (1) / ((float) 15)) {
							{
								Entity _ent = entity;
								_ent.teleportTo((attacker.getX() - attacker.getLookAngle().x * 2),
										(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (attacker.getX() - attacker.getLookAngle().x * 2), (int) (attacker.getZ() - attacker.getLookAngle().z * 2))),
										(attacker.getZ() - attacker.getLookAngle().z * 2));
								if (_ent instanceof ServerPlayer _serverPlayer)
									_serverPlayer.connection.teleport((attacker.getX() - attacker.getLookAngle().x * 2),
											(world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) (attacker.getX() - attacker.getLookAngle().x * 2), (int) (attacker.getZ() - attacker.getLookAngle().z * 2))),
											(attacker.getZ() - attacker.getLookAngle().z * 2), _ent.getYRot(), _ent.getXRot());
							}
							{
								final Vec3 _center = new Vec3(x, y, z);
								for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
										.toList()) {
									if (!(entityiterator instanceof RotEntity)) {
										entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.SONIC_BOOM)), 10);
										entityiterator.setDeltaMovement(new Vec3((((entityiterator.getX() - entity.getX()) / (entityiterator.position()).distanceTo((entity.position()))) * 2), 0.6,
												(((entityiterator.getZ() - entity.getZ()) / (entityiterator.position()).distanceTo((entity.position()))) * 2)));
									}
								}
							}
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
					}
				}
			}
			if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= 20) {
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_roar")), SoundSource.HOSTILE, (float) 1.2, (float) 0.4);
					} else {
						_level.playLocalSound((entity.getX()), (entity.getY()), (entity.getZ()), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("the_backwoods:rot_roar")), SoundSource.HOSTILE, (float) 1.2, (float) 0.4, false);
					}
				}
				if (world instanceof ServerLevel _level)
					_level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, (entity.getX()), (entity.getY() + 1), (entity.getZ()), 200, 0.1, 0.1, 0.1, 0.1);
				if (!entity.level().isClientSide())
					entity.discard();
				if (attacker instanceof ServerPlayer _player) {
					AdvancementHolder _adv = _player.server.getAdvancements().get(ResourceLocation.parse("the_backwoods:rot_vanish"));
					if (_adv != null) {
						AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
						if (!_ap.isDone()) {
							for (String criteria : _ap.getRemainingCriteria())
								_player.getAdvancements().award(_adv, criteria);
						}
					}
				}
			}
			if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= 250 && entity.getPersistentData().getDouble("msg1_fired") == 0) {
				if (!(foundPlayer == null)) {
					entity.getPersistentData().putDouble("msg1_fired", 1);
					if (foundPlayer instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal("I have felt deeper cuts than this."), true);
				}
			}
			if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= 100 && entity.getPersistentData().getDouble("msg2_fired") == 0) {
				if (!(foundPlayer == null)) {
					entity.getPersistentData().putDouble("msg2_fired", 1);
					if (foundPlayer instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal("I thin yet the hunger widens."), true);
				}
			}
			if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= 50 && entity.getPersistentData().getDouble("msg3_fired") == 0) {
				if (!(foundPlayer == null)) {
					entity.getPersistentData().putDouble("msg3_fired", 1);
					if (foundPlayer instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal("Pruning..."), true);
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