/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.item.Item;

import net.mcreator.thebackwoods.item.BackwoodsItem;
import net.mcreator.thebackwoods.TheBackwoodsMod;

public class TheBackwoodsModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(TheBackwoodsMod.MODID);
	public static final DeferredItem<Item> BACKWOODS;
	public static final DeferredItem<Item> SPLINTER_SPAWN_EGG;
	public static final DeferredItem<Item> HOLLOW_SPAWN_EGG;
	public static final DeferredItem<Item> LOG_SPLINTER_SPAWN_EGG;
	static {
		BACKWOODS = REGISTRY.register("backwoods", BackwoodsItem::new);
		SPLINTER_SPAWN_EGG = REGISTRY.register("splinter_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.SPLINTER, -7643606, -3632054, new Item.Properties()));
		HOLLOW_SPAWN_EGG = REGISTRY.register("hollow_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.HOLLOW, -9943521, -10861791, new Item.Properties()));
		LOG_SPLINTER_SPAWN_EGG = REGISTRY.register("log_splinter_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.LOG_SPLINTER, -8826332, -6984137, new Item.Properties()));
	}
	// Start of user code block custom items
	// End of user code block custom items
}