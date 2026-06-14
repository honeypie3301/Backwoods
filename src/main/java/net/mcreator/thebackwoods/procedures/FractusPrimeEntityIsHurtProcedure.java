package net.mcreator.thebackwoods.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

import net.mcreator.thebackwoods.entity.FractusPrimeEntity;
import net.mcreator.thebackwoods.TheBackwoodsMod;

public class FractusPrimeEntityIsHurtProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof FractusPrimeEntity _datEntSetL)
			_datEntSetL.getEntityData().set(FractusPrimeEntity.DATA_play_hurt_anim, true);
		TheBackwoodsMod.queueServerWork(8, () -> {
			if (entity instanceof FractusPrimeEntity _datEntSetL)
				_datEntSetL.getEntityData().set(FractusPrimeEntity.DATA_play_hurt_anim, false);
		});
	}
}