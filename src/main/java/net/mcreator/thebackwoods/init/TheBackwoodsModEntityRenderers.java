/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.thebackwoods.client.renderer.*;

@EventBusSubscriber(Dist.CLIENT)
public class TheBackwoodsModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(TheBackwoodsModEntities.SPLINTER.get(), SplinterRenderer::new);
		event.registerEntityRenderer(TheBackwoodsModEntities.HOLLOW.get(), HollowRenderer::new);
		event.registerEntityRenderer(TheBackwoodsModEntities.LOG_SPLINTER.get(), LogSplinterRenderer::new);
		event.registerEntityRenderer(TheBackwoodsModEntities.ASH_WEAVER.get(), AshWeaverRenderer::new);
		event.registerEntityRenderer(TheBackwoodsModEntities.ROT.get(), RotRenderer::new);
		event.registerEntityRenderer(TheBackwoodsModEntities.BLINDSPOT_SPLINTER.get(), BlindspotSplinterRenderer::new);
	}
}