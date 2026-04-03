/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import net.mcreator.thebackwoods.block.*;
import net.mcreator.thebackwoods.TheBackwoodsMod;

public class TheBackwoodsModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(TheBackwoodsMod.MODID);
	public static final DeferredBlock<Block> BACKWOODS_PORTAL;
	public static final DeferredBlock<Block> ASH_ROSE;
	public static final DeferredBlock<Block> FADED_BLOCK;
	public static final DeferredBlock<Block> PLAQUE;
	public static final DeferredBlock<Block> ROTTEN_OAK_PLANKS;
	public static final DeferredBlock<Block> ROTTEN_OAK_WOOD;
	public static final DeferredBlock<Block> ROTTEN_OAK_LOG;
	public static final DeferredBlock<Block> GEODE_TELEPORTER;
	public static final DeferredBlock<Block> ROTTEN_OAK_STAIR;
	public static final DeferredBlock<Block> ROTTEN_OAK_SLAB;
	public static final DeferredBlock<Block> ROTTEN_OAK_FENCE;
	public static final DeferredBlock<Block> ROTTEN_OAK_TRAPDOOR;
	public static final DeferredBlock<Block> ROTTEN_OAK_BUTTON;
	public static final DeferredBlock<Block> ROTTEN_OAK_GATE;
	public static final DeferredBlock<Block> ROTTEN_OAK_PRESSURE_PLATE;
	public static final DeferredBlock<Block> PETRIFIED_ROTTEN_OAK_WOOD;
	static {
		BACKWOODS_PORTAL = REGISTRY.register("backwoods_portal", BackwoodsPortalBlock::new);
		ASH_ROSE = REGISTRY.register("ash_rose", AshRoseBlock::new);
		FADED_BLOCK = REGISTRY.register("faded_block", FadedBlockBlock::new);
		PLAQUE = REGISTRY.register("plaque", PlaqueBlock::new);
		ROTTEN_OAK_PLANKS = REGISTRY.register("rotten_oak_planks", RottenOakPlanksBlock::new);
		ROTTEN_OAK_WOOD = REGISTRY.register("rotten_oak_wood", RottenOakWoodBlock::new);
		ROTTEN_OAK_LOG = REGISTRY.register("rotten_oak_log", RottenOakLogBlock::new);
		GEODE_TELEPORTER = REGISTRY.register("geode_teleporter", GeodeTeleporterBlock::new);
		ROTTEN_OAK_STAIR = REGISTRY.register("rotten_oak_stair", RottenOakStairBlock::new);
		ROTTEN_OAK_SLAB = REGISTRY.register("rotten_oak_slab", RottenOakSlabBlock::new);
		ROTTEN_OAK_FENCE = REGISTRY.register("rotten_oak_fence", RottenOakFenceBlock::new);
		ROTTEN_OAK_TRAPDOOR = REGISTRY.register("rotten_oak_trapdoor", RottenOakTrapdoorBlock::new);
		ROTTEN_OAK_BUTTON = REGISTRY.register("rotten_oak_button", RottenOakButtonBlock::new);
		ROTTEN_OAK_GATE = REGISTRY.register("rotten_oak_gate", RottenOakGateBlock::new);
		ROTTEN_OAK_PRESSURE_PLATE = REGISTRY.register("rotten_oak_pressure_plate", RottenOakPressurePlateBlock::new);
		PETRIFIED_ROTTEN_OAK_WOOD = REGISTRY.register("petrified_rotten_oak_wood", PetrifiedRottenOakWoodBlock::new);
	}
	// Start of user code block custom blocks
	// End of user code block custom blocks
}