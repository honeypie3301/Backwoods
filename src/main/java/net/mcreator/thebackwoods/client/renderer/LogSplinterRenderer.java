package net.mcreator.thebackwoods.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;

import net.mcreator.thebackwoods.entity.LogSplinterEntity;

public class LogSplinterRenderer extends HumanoidMobRenderer<LogSplinterEntity, HumanoidModel<LogSplinterEntity>> {
	public LogSplinterRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<LogSplinterEntity>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(LogSplinterEntity entity) {
		return ResourceLocation.parse("the_backwoods:textures/entities/oak_log_biped.png");
	}
}