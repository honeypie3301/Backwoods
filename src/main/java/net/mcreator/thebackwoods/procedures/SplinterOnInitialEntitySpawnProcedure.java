package net.mcreator.thebackwoods.procedures;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class SplinterOnInitialEntitySpawnProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (Math.random() < 0.2) {
			entity.getPersistentData().putBoolean("canTeleport", true);
		}
		if (entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(Attributes.SAFE_FALL_DISTANCE))
			_livingEntity1.getAttribute(Attributes.SAFE_FALL_DISTANCE).setBaseValue(7);
	}
}