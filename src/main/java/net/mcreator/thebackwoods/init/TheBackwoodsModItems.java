/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.thebackwoods.item.*;
import net.mcreator.thebackwoods.TheBackwoodsMod;

public class TheBackwoodsModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(TheBackwoodsMod.MODID);
	public static final DeferredItem<Item> BACKWOODS;
	public static final DeferredItem<Item> SPLINTER_SPAWN_EGG;
	public static final DeferredItem<Item> HOLLOW_SPAWN_EGG;
	public static final DeferredItem<Item> LOG_SPLINTER_SPAWN_EGG;
	public static final DeferredItem<Item> ASH_WEAVER_SPAWN_EGG;
	public static final DeferredItem<Item> ASH_ROSE;
	public static final DeferredItem<Item> FADED_BLOCK;
	public static final DeferredItem<Item> PLAQUE;
	public static final DeferredItem<Item> SEEP;
	public static final DeferredItem<Item> PALE_REMEDY;
	public static final DeferredItem<Item> ROT_SPAWN_EGG;
	public static final DeferredItem<Item> ROTTEN_OAK_PLANKS;
	public static final DeferredItem<Item> ROTTEN_OAK_WOOD;
	public static final DeferredItem<Item> ROTTEN_OAK_LOG;
	public static final DeferredItem<Item> GEODE_TELEPORTER;
	public static final DeferredItem<Item> ROTTEN_OAK_STAIR;
	public static final DeferredItem<Item> ROTTEN_OAK_SLAB;
	public static final DeferredItem<Item> ROTTEN_OAK_FENCE;
	public static final DeferredItem<Item> ROTTEN_OAK_TRAPDOOR;
	public static final DeferredItem<Item> ROTTEN_STICK;
	public static final DeferredItem<Item> SPLINTER_SHARD;
	public static final DeferredItem<Item> ROTTEN_SWORD;
	public static final DeferredItem<Item> HEARTWOOD_SHARD;
	public static final DeferredItem<Item> HEARTWOOD_ROTTEN_SWORD;
	public static final DeferredItem<Item> ROTTEN_AXE;
	public static final DeferredItem<Item> ROTTEN_PICKAXE;
	public static final DeferredItem<Item> ROTTEN_SHOVEL;
	public static final DeferredItem<Item> ROTTEN_HOE;
	public static final DeferredItem<Item> ROT_EFFIGY;
	public static final DeferredItem<Item> ROTTEN_OAK_BUTTON;
	public static final DeferredItem<Item> ROTTEN_OAK_GATE;
	public static final DeferredItem<Item> ROTTEN_OAK_PRESSURE_PLATE;
	public static final DeferredItem<Item> PALE_DRAUGHT_BOTTLE;
	public static final DeferredItem<Item> PETRIFIED_ROTTEN_OAK_WOOD;
	public static final DeferredItem<Item> BLINDSPOT_SPLINTER_SPAWN_EGG;
	public static final DeferredItem<Item> BLINDSPOT_SPLINTER_SPAWNER;
	static {
		BACKWOODS = REGISTRY.register("backwoods", BackwoodsItem::new);
		SPLINTER_SPAWN_EGG = REGISTRY.register("splinter_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.SPLINTER, -7643606, -3632054, new Item.Properties()));
		HOLLOW_SPAWN_EGG = REGISTRY.register("hollow_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.HOLLOW, -9943521, -10861791, new Item.Properties()));
		LOG_SPLINTER_SPAWN_EGG = REGISTRY.register("log_splinter_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.LOG_SPLINTER, -8826332, -6984137, new Item.Properties()));
		ASH_WEAVER_SPAWN_EGG = REGISTRY.register("ash_weaver_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.ASH_WEAVER, -10527652, -855310, new Item.Properties()));
		ASH_ROSE = block(TheBackwoodsModBlocks.ASH_ROSE, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
		FADED_BLOCK = block(TheBackwoodsModBlocks.FADED_BLOCK, new Item.Properties().rarity(Rarity.UNCOMMON));
		PLAQUE = block(TheBackwoodsModBlocks.PLAQUE, new Item.Properties().rarity(Rarity.UNCOMMON));
		SEEP = REGISTRY.register("seep", SeepItem::new);
		PALE_REMEDY = REGISTRY.register("pale_remedy", PaleRemedyItem::new);
		ROT_SPAWN_EGG = REGISTRY.register("rot_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.ROT, -6914706, -10263709, new Item.Properties()));
		ROTTEN_OAK_PLANKS = block(TheBackwoodsModBlocks.ROTTEN_OAK_PLANKS);
		ROTTEN_OAK_WOOD = block(TheBackwoodsModBlocks.ROTTEN_OAK_WOOD);
		ROTTEN_OAK_LOG = block(TheBackwoodsModBlocks.ROTTEN_OAK_LOG);
		GEODE_TELEPORTER = block(TheBackwoodsModBlocks.GEODE_TELEPORTER, new Item.Properties().fireResistant());
		ROTTEN_OAK_STAIR = block(TheBackwoodsModBlocks.ROTTEN_OAK_STAIR);
		ROTTEN_OAK_SLAB = block(TheBackwoodsModBlocks.ROTTEN_OAK_SLAB);
		ROTTEN_OAK_FENCE = block(TheBackwoodsModBlocks.ROTTEN_OAK_FENCE);
		ROTTEN_OAK_TRAPDOOR = block(TheBackwoodsModBlocks.ROTTEN_OAK_TRAPDOOR);
		ROTTEN_STICK = REGISTRY.register("rotten_stick", RottenStickItem::new);
		SPLINTER_SHARD = REGISTRY.register("splinter_shard", SplinterShardItem::new);
		ROTTEN_SWORD = REGISTRY.register("rotten_sword", RottenSwordItem::new);
		HEARTWOOD_SHARD = REGISTRY.register("heartwood_shard", HeartwoodShardItem::new);
		HEARTWOOD_ROTTEN_SWORD = REGISTRY.register("heartwood_rotten_sword", HeartwoodRottenSwordItem::new);
		ROTTEN_AXE = REGISTRY.register("rotten_axe", RottenAxeItem::new);
		ROTTEN_PICKAXE = REGISTRY.register("rotten_pickaxe", RottenPickaxeItem::new);
		ROTTEN_SHOVEL = REGISTRY.register("rotten_shovel", RottenShovelItem::new);
		ROTTEN_HOE = REGISTRY.register("rotten_hoe", RottenHoeItem::new);
		ROT_EFFIGY = REGISTRY.register("rot_effigy", RotEffigyItem::new);
		ROTTEN_OAK_BUTTON = block(TheBackwoodsModBlocks.ROTTEN_OAK_BUTTON);
		ROTTEN_OAK_GATE = block(TheBackwoodsModBlocks.ROTTEN_OAK_GATE);
		ROTTEN_OAK_PRESSURE_PLATE = block(TheBackwoodsModBlocks.ROTTEN_OAK_PRESSURE_PLATE);
		PALE_DRAUGHT_BOTTLE = REGISTRY.register("pale_draught_bottle", PaleDraughtBottleItem::new);
		PETRIFIED_ROTTEN_OAK_WOOD = block(TheBackwoodsModBlocks.PETRIFIED_ROTTEN_OAK_WOOD, new Item.Properties().rarity(Rarity.UNCOMMON));
		BLINDSPOT_SPLINTER_SPAWN_EGG = REGISTRY.register("blindspot_splinter_spawn_egg", () -> new DeferredSpawnEggItem(TheBackwoodsModEntities.BLINDSPOT_SPLINTER, -7643606, -3632054, new Item.Properties()));
		BLINDSPOT_SPLINTER_SPAWNER = block(TheBackwoodsModBlocks.BLINDSPOT_SPLINTER_SPAWNER, new Item.Properties().fireResistant());
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}