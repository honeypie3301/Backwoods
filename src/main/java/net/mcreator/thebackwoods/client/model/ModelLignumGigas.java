package net.mcreator.thebackwoods.client.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class ModelLignumGigas<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("the_backwoods", "model_lignum_gigas"), "main");
	public final ModelPart head;
	public final ModelPart body;
	public final ModelPart left_arm;
	public final ModelPart left_arm_2;
	public final ModelPart left_arm_3;
	public final ModelPart left_arm_4;
	public final ModelPart right_arm;
	public final ModelPart right_arm_2;
	public final ModelPart right_arm_3;
	public final ModelPart right_arm_4;
	public final ModelPart left_leg;
	public final ModelPart right_leg;

	public ModelLignumGigas(ModelPart root) {
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.left_arm = root.getChild("left_arm");
		this.left_arm_2 = this.left_arm.getChild("left_arm_2");
		this.left_arm_3 = this.left_arm.getChild("left_arm_3");
		this.left_arm_4 = this.left_arm.getChild("left_arm_4");
		this.right_arm = root.getChild("right_arm");
		this.right_arm_2 = this.right_arm.getChild("right_arm_2");
		this.right_arm_3 = this.right_arm.getChild("right_arm_3");
		this.right_arm_4 = this.right_arm.getChild("right_arm_4");
		this.left_leg = root.getChild("left_leg");
		this.right_leg = root.getChild("right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition head = partdefinition
				.addOrReplaceChild("head",
						CubeListBuilder.create().texOffs(7, 0).addBox(3.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(1, 0).addBox(-3.0F, -8.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(1, 0)
								.addBox(-3.0F, -1.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(7, 0).mirror().addBox(-4.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
						PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(12, 16)
				.addBox(-8.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(12, 16).mirror().addBox(4.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
		PartDefinition left_arm_2 = left_arm.addOrReplaceChild("left_arm_2", CubeListBuilder.create(), PartPose.offset(0.5F, -0.5F, -1.0F));
		PartDefinition left_arm_r1 = left_arm_2.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(46, 20).mirror().addBox(-0.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false),
				PartPose.offsetAndRotation(-0.5F, 0.0F, 0.5F, 0.0F, 1.5708F, -0.0698F));
		PartDefinition left_arm_r2 = left_arm_2.addOrReplaceChild("left_arm_r2", CubeListBuilder.create().texOffs(47, 20).mirror().addBox(-1.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.0698F));
		PartDefinition left_arm_3 = left_arm.addOrReplaceChild("left_arm_3", CubeListBuilder.create(), PartPose.offset(0.75F, -0.5F, 0.5F));
		PartDefinition left_arm_r3 = left_arm_3.addOrReplaceChild("left_arm_r3", CubeListBuilder.create().texOffs(46, 20).mirror().addBox(-0.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false),
				PartPose.offsetAndRotation(-0.5F, 0.0F, 0.5F, 0.0F, 1.5708F, -0.0698F));
		PartDefinition left_arm_r4 = left_arm_3.addOrReplaceChild("left_arm_r4", CubeListBuilder.create().texOffs(47, 20).mirror().addBox(-1.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.0698F));
		PartDefinition left_arm_4 = left_arm.addOrReplaceChild("left_arm_4", CubeListBuilder.create(), PartPose.offset(1.75F, -0.5F, 0.0F));
		PartDefinition left_arm_r5 = left_arm_4.addOrReplaceChild("left_arm_r5", CubeListBuilder.create().texOffs(47, 20).mirror().addBox(-1.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false),
				PartPose.offsetAndRotation(0.5F, 0.0F, -0.5F, 0.0F, 0.0F, -0.0698F));
		PartDefinition left_arm_r6 = left_arm_4.addOrReplaceChild("left_arm_r6", CubeListBuilder.create().texOffs(46, 20).mirror().addBox(-0.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, -0.0698F));
		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
		PartDefinition right_arm_2 = right_arm.addOrReplaceChild("right_arm_2", CubeListBuilder.create(), PartPose.offset(-0.75F, -0.5F, 0.5F));
		PartDefinition right_arm_r1 = right_arm_2.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(46, 20).addBox(-1.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)),
				PartPose.offsetAndRotation(0.5F, 0.0F, 0.5F, 0.0F, -1.5708F, 0.0698F));
		PartDefinition right_arm_r2 = right_arm_2.addOrReplaceChild("right_arm_r2", CubeListBuilder.create().texOffs(47, 20).addBox(-0.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0698F));
		PartDefinition right_arm_3 = right_arm.addOrReplaceChild("right_arm_3", CubeListBuilder.create(), PartPose.offset(-2.25F, -0.5F, -0.5F));
		PartDefinition right_arm_r3 = right_arm_3.addOrReplaceChild("right_arm_r3", CubeListBuilder.create().texOffs(46, 20).addBox(-1.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)),
				PartPose.offsetAndRotation(0.5F, 0.0F, 0.5F, 0.0F, -1.5708F, 0.0698F));
		PartDefinition right_arm_r4 = right_arm_3.addOrReplaceChild("right_arm_r4", CubeListBuilder.create().texOffs(47, 20).addBox(-0.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0698F));
		PartDefinition right_arm_4 = right_arm.addOrReplaceChild("right_arm_4", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, -0.5F));
		PartDefinition right_arm_r5 = right_arm_4.addOrReplaceChild("right_arm_r5", CubeListBuilder.create().texOffs(47, 20).addBox(-0.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)),
				PartPose.offsetAndRotation(-0.5F, 0.0F, -0.5F, 0.0F, 0.0F, 0.0698F));
		PartDefinition right_arm_r6 = right_arm_4.addOrReplaceChild("right_arm_r6", CubeListBuilder.create().texOffs(46, 20).addBox(-1.5F, -0.5F, 0.0F, 2.0F, 20.0F, 0.0F, new CubeDeformation(0.001F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0698F));
		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
	}
}