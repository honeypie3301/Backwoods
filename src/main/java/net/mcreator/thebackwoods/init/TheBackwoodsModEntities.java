/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.Registries;

import net.mcreator.thebackwoods.entity.*;
import net.mcreator.thebackwoods.TheBackwoodsMod;

@EventBusSubscriber
public class TheBackwoodsModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, TheBackwoodsMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<SplinterEntity>> SPLINTER = register("splinter",
			EntityType.Builder.<SplinterEntity>of(SplinterEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<HollowEntity>> HOLLOW = register("hollow",
			EntityType.Builder.<HollowEntity>of(HollowEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().ridingOffset(-0.6f).sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<LogSplinterEntity>> LOG_SPLINTER = register("log_splinter",
			EntityType.Builder.<LogSplinterEntity>of(LogSplinterEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().ridingOffset(-0.6f).sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<AshWeaverEntity>> ASH_WEAVER = register("ash_weaver",
			EntityType.Builder.<AshWeaverEntity>of(AshWeaverEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<RotEntity>> ROT = register("rot",
			EntityType.Builder.<RotEntity>of(RotEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().ridingOffset(-0.6f).sized(0.7f, 2f));
	public static final DeferredHolder<EntityType<?>, EntityType<BlindspotSplinterEntity>> BLINDSPOT_SPLINTER = register("blindspot_splinter",
			EntityType.Builder.<BlindspotSplinterEntity>of(BlindspotSplinterEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).sized(0.6f, 1.8f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(RegisterSpawnPlacementsEvent event) {
		SplinterEntity.init(event);
		HollowEntity.init(event);
		LogSplinterEntity.init(event);
		AshWeaverEntity.init(event);
		RotEntity.init(event);
		BlindspotSplinterEntity.init(event);
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(SPLINTER.get(), SplinterEntity.createAttributes().build());
		event.put(HOLLOW.get(), HollowEntity.createAttributes().build());
		event.put(LOG_SPLINTER.get(), LogSplinterEntity.createAttributes().build());
		event.put(ASH_WEAVER.get(), AshWeaverEntity.createAttributes().build());
		event.put(ROT.get(), RotEntity.createAttributes().build());
		event.put(BLINDSPOT_SPLINTER.get(), BlindspotSplinterEntity.createAttributes().build());
	}
}