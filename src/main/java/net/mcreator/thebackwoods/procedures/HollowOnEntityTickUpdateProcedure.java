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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;

import net.mcreator.thebackwoods.entity.HollowEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

@EventBusSubscriber
public class HollowOnEntityTickUpdateProcedure {
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
		double offsetX = 0;
		double offsetZ = 0;
		Entity foundPlayer = null;
		if (entity instanceof HollowEntity) {
			if (!world.getEntitiesOfClass(Player.class, new AABB(Vec3.ZERO, Vec3.ZERO).move(new Vec3(x, y, z)).inflate(32 / 2d), e -> true).isEmpty()) {
				if (!world.canSeeSkyFromBelowWater(
						BlockPos.containing((findEntityInWorldRange(world, Player.class, x, y, z, 32)).getX(), (findEntityInWorldRange(world, Player.class, x, y, z, 32)).getY(), (findEntityInWorldRange(world, Player.class, x, y, z, 32)).getZ()))) {
					if (Mth.nextInt(RandomSource.create(), 1, 99) < 2) {
						if (!((findEntityInWorldRange(world, Player.class, x, y, z, 32)) == null)) {
							foundPlayer = findEntityInWorldRange(world, Player.class, x, y, z, 32);
							offsetX = Mth.nextInt(RandomSource.create(), -3, 3);
							offsetZ = Mth.nextInt(RandomSource.create(), -3, 3);
							if (!(offsetX == 0 && offsetZ == 0)) {
								if ((world.getBlockState(BlockPos.containing(foundPlayer.getX() + offsetX, foundPlayer.getY(), foundPlayer.getZ() + offsetZ))).getBlock() == Blocks.AIR
										&& (world.getBlockState(BlockPos.containing(foundPlayer.getX() + offsetX, foundPlayer.getY() + 1, foundPlayer.getZ() + offsetZ))).getBlock() == Blocks.AIR) {
									{
										Entity _ent = entity;
										_ent.teleportTo((foundPlayer.getX() + offsetX), (foundPlayer.getY()), (foundPlayer.getZ() + offsetZ));
										if (_ent instanceof ServerPlayer _serverPlayer)
											_serverPlayer.connection.teleport((foundPlayer.getX() + offsetX), (foundPlayer.getY()), (foundPlayer.getZ() + offsetZ), _ent.getYRot(), _ent.getXRot());
									}
									entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((foundPlayer.getX()), (foundPlayer.getY()), (foundPlayer.getZ())));
									{
										Entity _ent = entity;
										_ent.setYRot((float) (Math.atan2(foundPlayer.getZ() - entity.getZ(), foundPlayer.getX() - entity.getX()) * (180 / 3.14159) - 90));
										_ent.setXRot(0);
										_ent.setYBodyRot(_ent.getYRot());
										_ent.setYHeadRot(_ent.getYRot());
										_ent.yRotO = _ent.getYRot();
										_ent.xRotO = _ent.getXRot();
										if (_ent instanceof LivingEntity _entity) {
											_entity.yBodyRotO = _entity.getYRot();
											_entity.yHeadRotO = _entity.getYRot();
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private static Entity findEntityInWorldRange(LevelAccessor world, Class<? extends Entity> clazz, double x, double y, double z, double range) {
		return (Entity) world.getEntitiesOfClass(clazz, AABB.ofSize(new Vec3(x, y, z), range, range, range), e -> true).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(x, y, z))).findFirst().orElse(null);
	}
}