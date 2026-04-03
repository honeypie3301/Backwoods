/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

import net.mcreator.thebackwoods.TheBackwoodsMod;

@EventBusSubscriber
public class TheBackwoodsModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheBackwoodsMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			tabData.accept(TheBackwoodsModItems.BACKWOODS.get());
			tabData.accept(TheBackwoodsModItems.ROTTEN_STICK.get());
			tabData.accept(TheBackwoodsModItems.ROTTEN_AXE.get());
			tabData.accept(TheBackwoodsModItems.ROTTEN_PICKAXE.get());
			tabData.accept(TheBackwoodsModItems.ROTTEN_SHOVEL.get());
			tabData.accept(TheBackwoodsModItems.ROTTEN_HOE.get());
			tabData.accept(TheBackwoodsModItems.ROT_EFFIGY.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(TheBackwoodsModItems.SPLINTER_SPAWN_EGG.get());
			tabData.accept(TheBackwoodsModItems.HOLLOW_SPAWN_EGG.get());
			tabData.accept(TheBackwoodsModItems.LOG_SPLINTER_SPAWN_EGG.get());
			tabData.accept(TheBackwoodsModItems.ASH_WEAVER_SPAWN_EGG.get());
			tabData.accept(TheBackwoodsModItems.ROT_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
			tabData.accept(TheBackwoodsModBlocks.ASH_ROSE.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.FADED_BLOCK.get().asItem());
		} else if (tabData.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
			tabData.accept(TheBackwoodsModBlocks.FADED_BLOCK.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.PLAQUE.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_PLANKS.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_WOOD.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_LOG.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_STAIR.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_SLAB.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_FENCE.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_TRAPDOOR.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_BUTTON.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_GATE.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_PRESSURE_PLATE.get().asItem());
			tabData.accept(TheBackwoodsModBlocks.PETRIFIED_ROTTEN_OAK_WOOD.get().asItem());
		} else if (tabData.getTabKey() == CreativeModeTabs.INGREDIENTS) {
			tabData.accept(TheBackwoodsModItems.SEEP.get());
			tabData.accept(TheBackwoodsModItems.ROTTEN_STICK.get());
			tabData.accept(TheBackwoodsModItems.SPLINTER_SHARD.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
			tabData.accept(TheBackwoodsModItems.PALE_REMEDY.get());
			tabData.accept(TheBackwoodsModItems.PALE_DRAUGHT_BOTTLE.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.accept(TheBackwoodsModItems.ROTTEN_SWORD.get());
			tabData.accept(TheBackwoodsModItems.HEARTWOOD_ROTTEN_SWORD.get());
			tabData.accept(TheBackwoodsModItems.ROTTEN_AXE.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
			tabData.accept(TheBackwoodsModBlocks.ROTTEN_OAK_PRESSURE_PLATE.get().asItem());
		}
	}
}