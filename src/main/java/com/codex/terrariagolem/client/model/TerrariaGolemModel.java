package com.codex.terrariagolem.client.model;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.world.entity.TerrariaGolemEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TerrariaGolemModel<T extends TerrariaGolemEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(TerrariaGolemMod.MODID, "terraria_golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart core;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftFist;
    private final ModelPart rightFist;
    private final ModelPart leftChain;
    private final ModelPart rightChain;

    public TerrariaGolemModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body");
        head = body.getChild("head");
        jaw = head.getChild("jaw");
        core = body.getChild("core");
        leftArm = body.getChild("left_arm");
        rightArm = body.getChild("right_arm");
        leftForearm = leftArm.getChild("left_forearm");
        rightForearm = rightArm.getChild("right_forearm");
        leftFist = leftForearm.getChild("left_fist");
        rightFist = rightForearm.getChild("right_fist");
        leftChain = leftForearm.getChild("left_chain");
        rightChain = rightForearm.getChild("right_chain");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-10.0F, -27.0F, -6.5F, 20.0F, 24.0F, 13.0F)
                        .texOffs(0, 38).addBox(-13.0F, -25.0F, -5.5F, 26.0F, 6.0F, 11.0F)
                        .texOffs(0, 56).addBox(-8.0F, -4.0F, -7.5F, 16.0F, 5.0F, 15.0F)
                        .texOffs(64, 36).addBox(-5.0F, -33.0F, -4.5F, 10.0F, 8.0F, 9.0F)
                        .texOffs(96, 36).addBox(-16.0F, -23.0F, -4.0F, 4.0F, 8.0F, 8.0F)
                        .texOffs(96, 36).mirror().addBox(12.0F, -23.0F, -4.0F, 4.0F, 8.0F, 8.0F)
                        .texOffs(98, 58).addBox(-15.0F, -27.0F, -3.0F, 5.0F, 4.0F, 6.0F)
                        .texOffs(98, 58).mirror().addBox(10.0F, -27.0F, -3.0F, 5.0F, 4.0F, 6.0F)
                        .texOffs(64, 55).addBox(-7.0F, -30.0F, -7.0F, 14.0F, 4.0F, 2.0F)
                        .texOffs(64, 62).addBox(-9.0F, -16.0F, -7.2F, 18.0F, 5.0F, 2.0F)
                        .texOffs(64, 71).addBox(-4.0F, -10.0F, -7.1F, 8.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(144, 32).addBox(-4.5F, -22.0F, -8.55F, 9.0F, 11.0F, 1.0F)
                        .texOffs(176, 32).addBox(-1.75F, -18.75F, -8.8F, 3.5F, 3.5F, 1.0F),
                PartPose.ZERO);

        body.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 216).addBox(-4.0F, -1.0F, -4.0F, 7.0F, 11.0F, 8.0F)
                        .texOffs(36, 216).addBox(-7.0F, 9.0F, -6.0F, 11.0F, 5.0F, 11.0F)
                        .texOffs(88, 216).addBox(-8.0F, 13.0F, -7.0F, 13.0F, 3.0F, 13.0F),
                PartPose.offset(-5.0F, -15.0F, 0.0F));

        body.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 216).mirror().addBox(-3.0F, -1.0F, -4.0F, 7.0F, 11.0F, 8.0F)
                        .texOffs(36, 216).mirror().addBox(-4.0F, 9.0F, -6.0F, 11.0F, 5.0F, 11.0F)
                        .texOffs(88, 216).mirror().addBox(-5.0F, 13.0F, -7.0F, 13.0F, 3.0F, 13.0F),
                PartPose.offset(5.0F, -15.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 128).addBox(-8.0F, -12.0F, -7.0F, 16.0F, 12.0F, 14.0F)
                        .texOffs(64, 128).addBox(-9.0F, -10.0F, -8.2F, 18.0F, 4.0F, 2.0F)
                        .texOffs(112, 128).addBox(-5.0F, -7.0F, -8.4F, 10.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -28.0F, 0.0F));

        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(112, 160).addBox(-6.0F, -1.0F, -7.9F, 12.0F, 4.0F, 3.0F),
                PartPose.ZERO);

        PartDefinition leftArm = body.addOrReplaceChild("left_arm",
                CubeListBuilder.create(),
                PartPose.offset(-13.0F, -24.0F, 0.0F));
        PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create(),
                PartPose.offset(-1.0F, 14.0F, 0.0F));
        leftForearm.addOrReplaceChild("left_chain",
                CubeListBuilder.create(),
                PartPose.ZERO);
        leftForearm.addOrReplaceChild("left_fist",
                CubeListBuilder.create(),
                PartPose.ZERO);

        PartDefinition rightArm = body.addOrReplaceChild("right_arm",
                CubeListBuilder.create(),
                PartPose.offset(13.0F, -24.0F, 0.0F));
        PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create(),
                PartPose.offset(1.0F, 14.0F, 0.0F));
        rightForearm.addOrReplaceChild("right_chain",
                CubeListBuilder.create(),
                PartPose.ZERO);
        rightForearm.addOrReplaceChild("right_fist",
                CubeListBuilder.create(),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        float phasePulse = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.18F);
        int phase = entity.getPhase();
        int attack = entity.getAttackState();
        int attackTicks = entity.getAttackTicks();

        head.visible = false;
        leftChain.visible = false;
        rightChain.visible = false;
        leftArm.visible = false;
        rightArm.visible = false;
        leftForearm.visible = true;
        rightForearm.visible = true;
        leftFist.visible = false;
        rightFist.visible = false;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.45F;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.25F;
        core.zRot = Mth.sin(ageInTicks * 0.12F) * 0.04F;

        if (phase >= 1) {
            jaw.xRot = 0.18F + phasePulse * 0.12F;
            core.xScale = 1.0F + phasePulse * 0.08F;
            core.yScale = 1.0F + phasePulse * 0.08F;
        }

        if (phase >= 2) {
            leftArm.zRot = -0.16F;
            rightArm.zRot = 0.16F;
        }

        float walk = Mth.sin(limbSwing * 0.45F) * limbSwingAmount;
        leftForearm.xRot = -0.12F + walk * 0.18F;
        rightForearm.xRot = -0.12F - walk * 0.18F;

        animatePunch(entity.getLeftArmExtension(), true);
        animatePunch(entity.getRightArmExtension(), false);

        if (attack == TerrariaGolemEntity.ATTACK_FIREBALL) {
            float open = Mth.clamp(attackTicks / 8.0F, 0.0F, 1.0F);
            head.xRot -= 0.12F * open;
            jaw.xRot += 0.55F * open;
            core.xScale = 1.12F;
            core.yScale = 1.12F;
        } else if (attack == TerrariaGolemEntity.ATTACK_LASER) {
            head.xRot -= 0.08F;
            jaw.xRot += 0.18F;
            core.zRot += Mth.sin(ageInTicks * 0.6F) * 0.08F;
        } else if (attack == TerrariaGolemEntity.ATTACK_STOMP) {
            float charge = Mth.clamp(attackTicks / 10.0F, 0.0F, 1.0F);
            body.xRot = 0.16F * charge;
            leftArm.xRot = -0.25F * charge;
            rightArm.xRot = -0.25F * charge;
        }
    }

    private void animatePunch(float extension, boolean left) {
        if (extension <= 0.0F) {
            return;
        }

        ModelPart arm = left ? leftArm : rightArm;
        ModelPart forearm = left ? leftForearm : rightForearm;
        ModelPart chain = left ? leftChain : rightChain;
        float side = left ? -1.0F : 1.0F;

        arm.xRot = -0.68F * extension;
        arm.yRot = side * 0.12F * extension;
        forearm.xRot = -1.18F * extension;
        chain.visible = false;
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
