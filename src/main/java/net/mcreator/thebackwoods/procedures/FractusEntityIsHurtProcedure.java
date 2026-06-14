package net.mcreator.thebackwoods.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

import net.mcreator.thebackwoods.entity.FractusEntity;
import net.mcreator.thebackwoods.TheBackwoodsMod;

public class FractusEntityIsHurtProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof FractusEntity _datEntSetL)
			_datEntSetL.getEntityData().set(FractusEntity.DATA_play_hurt_anim, true);
		TheBackwoodsMod.queueServerWork(8, () -> {
			if (entity instanceof FractusEntity _datEntSetL)
				_datEntSetL.getEntityData().set(FractusEntity.DATA_play_hurt_anim, false);
		});
	}
}