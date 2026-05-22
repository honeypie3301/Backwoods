package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.core.registries.BuiltInRegistries;

// Features
import net.mcreator.thebackwoods.LabyrinthineGridsMaze;
import net.mcreator.thebackwoods.CalcifiedSpikeFeature;
import net.mcreator.thebackwoods.LargeCalcifiedSpike; // Added for the big spike variant
import net.mcreator.thebackwoods.SubGrainAtriaFeature;
import net.mcreator.thebackwoods.world.features.OakStalactiteFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.FEATURE, "the_backwoods");

    public static final DeferredHolder<Feature<?>, LabyrinthineGridsMaze> LABYRINTHINE_MAZE = REGISTRY.register("labyrinthine_grids_maze", LabyrinthineGridsMaze::new);
    public static final DeferredHolder<Feature<?>, CalcifiedSpikeFeature> CALCIFIED_SPIKE = REGISTRY.register("calcified_spike", CalcifiedSpikeFeature::new);
    public static final DeferredHolder<Feature<?>, LargeCalcifiedSpike> LARGE_CALCIFIED_SPIKE = REGISTRY.register("large_calcified_spike", LargeCalcifiedSpike::new);
    public static final DeferredHolder<Feature<?>, SubGrainAtriaFeature> SUB_GRAIN_ATRIA = REGISTRY.register("sub_grain_atria", SubGrainAtriaFeature::new);
    
    // Decoration Features
    public static final DeferredHolder<Feature<?>, OakStalactiteFeature> OAK_STALACTITE = REGISTRY.register("oak_stalactite", OakStalactiteFeature::new);

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}