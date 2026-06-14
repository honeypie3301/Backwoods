package net.mcreator.thebackwoods.procedures;

import net.minecraft.world.entity.Entity;

import net.mcreator.thebackwoods.entity.FractusEntity;

public class FractusPlaybackConditionProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity instanceof FractusEntity _datEntL0 && _datEntL0.getEntityData().get(FractusEntity.DATA_play_hurt_anim)) {
			return true;
		}
		return false;
	}
}