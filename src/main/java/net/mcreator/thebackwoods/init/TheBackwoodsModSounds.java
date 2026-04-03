/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.mcreator.thebackwoods.TheBackwoodsMod;

public class TheBackwoodsModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, TheBackwoodsMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> WOOD_CREAK_PARANOIA = REGISTRY.register("wood_creak_paranoia", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "wood_creak_paranoia")));
	public static final DeferredHolder<SoundEvent, SoundEvent> WHISPERSTAGE1 = REGISTRY.register("whisperstage1", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "whisperstage1")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ROT_STEP = REGISTRY.register("rot_step", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "rot_step")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ROT_ROAR = REGISTRY.register("rot_roar", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "rot_roar")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SPLINTER_STEP = REGISTRY.register("splinter_step", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "splinter_step")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SPLINTER_ATTACK = REGISTRY.register("splinter_attack", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "splinter_attack")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SPLINTER_IDLE = REGISTRY.register("splinter_idle", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "splinter_idle")));
	public static final DeferredHolder<SoundEvent, SoundEvent> SPLINTER_HURT = REGISTRY.register("splinter_hurt", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "splinter_hurt")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ROTTEN_PLANKS_BREAK = REGISTRY.register("rotten_planks_break", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "rotten_planks_break")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ROTTEN_PLANKS_STEP = REGISTRY.register("rotten_planks_step", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "rotten_planks_step")));
	public static final DeferredHolder<SoundEvent, SoundEvent> DEGRADATION = REGISTRY.register("degradation", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "degradation")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STILL_MUSIC = REGISTRY.register("still_music", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "still_music")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STILL_MOOD = REGISTRY.register("still_mood", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "still_mood")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STILL_MUSIC2 = REGISTRY.register("still_music2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "still_music2")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STILL_MOOD2 = REGISTRY.register("still_mood2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "still_mood2")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STILL_MOOD3 = REGISTRY.register("still_mood3", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "still_mood3")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STILL_AMBIENT = REGISTRY.register("still_ambient", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "still_ambient")));
	public static final DeferredHolder<SoundEvent, SoundEvent> STILL_AMBIENT2 = REGISTRY.register("still_ambient2", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("the_backwoods", "still_ambient2")));
}