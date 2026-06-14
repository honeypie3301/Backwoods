/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.thebackwoods.init;

import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.thebackwoods.client.particle.FractusLaserParticleWhiteParticle;
import net.mcreator.thebackwoods.client.particle.FractusLaserParticleSummonParticle;
import net.mcreator.thebackwoods.client.particle.FractusLaserParticleRedParticle;
import net.mcreator.thebackwoods.client.particle.FractusLaserParticleOrangeParticle;
import net.mcreator.thebackwoods.client.particle.FractusLaserParticleBurstParticle;

@EventBusSubscriber(Dist.CLIENT)
public class TheBackwoodsModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(TheBackwoodsModParticleTypes.FRACTUS_LASER_PARTICLE_WHITE.get(), FractusLaserParticleWhiteParticle::provider);
		event.registerSpriteSet(TheBackwoodsModParticleTypes.FRACTUS_LASER_PARTICLE_ORANGE.get(), FractusLaserParticleOrangeParticle::provider);
		event.registerSpriteSet(TheBackwoodsModParticleTypes.FRACTUS_LASER_PARTICLE_RED.get(), FractusLaserParticleRedParticle::provider);
		event.registerSpriteSet(TheBackwoodsModParticleTypes.FRACTUS_LASER_PARTICLE_BURST.get(), FractusLaserParticleBurstParticle::provider);
		event.registerSpriteSet(TheBackwoodsModParticleTypes.FRACTUS_LASER_PARTICLE_SUMMON.get(), FractusLaserParticleSummonParticle::provider);
	}
}