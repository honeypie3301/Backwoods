package net.mcreator.thebackwoods.mixin;

import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.core.Holder;

import net.mcreator.thebackwoods.init.TheBackwoodsModBiomes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements TheBackwoodsModBiomes.TheBackwoodsModNoiseGeneratorSettings {
	@Unique
	private Holder<DimensionType> the_backwoods_dimensionTypeReference;

	@WrapMethod(method = "surfaceRule")
	public SurfaceRules.RuleSource surfaceRule(Operation<SurfaceRules.RuleSource> original) {
		SurfaceRules.RuleSource retval = original.call();
		if (this.the_backwoods_dimensionTypeReference != null) {
			retval = TheBackwoodsModBiomes.adaptSurfaceRule(retval, this.the_backwoods_dimensionTypeReference);
		}
		return retval;
	}

	@Override
	public void setthe_backwoodsDimensionTypeReference(Holder<DimensionType> dimensionType) {
		this.the_backwoods_dimensionTypeReference = dimensionType;
	}
}