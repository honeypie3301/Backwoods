/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.thebackwoods.client.model.ModelStiltWalker;
import net.mcreator.thebackwoods.client.model.ModelListener;
import net.mcreator.thebackwoods.client.model.ModelLignumGigas;

@EventBusSubscriber(Dist.CLIENT)
public class TheBackwoodsModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelLignumGigas.LAYER_LOCATION, ModelLignumGigas::createBodyLayer);
		event.registerLayerDefinition(ModelStiltWalker.LAYER_LOCATION, ModelStiltWalker::createBodyLayer);
		event.registerLayerDefinition(ModelListener.LAYER_LOCATION, ModelListener::createBodyLayer);
	}
}