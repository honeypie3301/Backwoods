package net.mcreator.thebackwoods.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import net.mcreator.thebackwoods.entity.LignumGigasEntity;
import net.mcreator.thebackwoods.client.model.animations.LignumGigas_2Animation;
import net.mcreator.thebackwoods.client.model.ModelLignumGigas_1;

import com.mojang.blaze3d.vertex.PoseStack;

public class LignumGigasRenderer extends MobRenderer<LignumGigasEntity, ModelLignumGigas_1<LignumGigasEntity>> {
	private final ResourceLocation entityTexture = ResourceLocation.parse("the_backwoods:textures/entities/gigas_skin.png");

	public LignumGigasRenderer(EntityRendererProvider.Context context) {
		super(context, new AnimatedModel(context.bakeLayer(ModelLignumGigas_1.LAYER_LOCATION)), 1f);
	}

	@Override
	protected void scale(LignumGigasEntity entity, PoseStack poseStack, float f) {
		poseStack.scale(40f, 40f, 40f);
	}

	@Override
	public ResourceLocation getTextureLocation(LignumGigasEntity entity) {
		return entityTexture;
	}

	private static final class AnimatedModel extends ModelLignumGigas_1<LignumGigasEntity> {
		private final ModelPart root;
		private final HierarchicalModel animator = new HierarchicalModel<LignumGigasEntity>() {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(LignumGigasEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
				this.root().getAllParts().forEach(ModelPart::resetPose);
				this.animate(entity.animationState0, LignumGigas_2Animation.low_health_attack, ageInTicks, 1f);
				this.animate(entity.animationState1, LignumGigas_2Animation.reverse_low_health_attack, ageInTicks, 1f);
			}
		};

		public AnimatedModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		@Override
		public void setupAnim(LignumGigasEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		}
	}
}