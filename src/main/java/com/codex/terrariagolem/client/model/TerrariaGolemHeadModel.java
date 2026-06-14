package com.codex.terrariagolem.client.model;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.world.entity.TerrariaGolemHeadEntity;
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

public class TerrariaGolemHeadModel<T extends TerrariaGolemHeadEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(TerrariaGolemMod.MODID, "terraria_golem_head"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart jaw;

    public TerrariaGolemHeadModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        jaw = head.getChild("jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 128).addBox(-8.0F, -13.0F, -7.0F, 16.0F, 13.0F, 14.0F)
                        .texOffs(64, 128).addBox(-10.0F, -11.0F, -7.6F, 20.0F, 4.0F, 3.0F)
                        .texOffs(112, 128).addBox(-5.0F, -9.0F, -8.2F, 10.0F, 5.0F, 2.0F)
                        .texOffs(144, 128).addBox(-11.0F, -8.0F, -4.0F, 3.0F, 6.0F, 8.0F)
                        .texOffs(144, 128).mirror().addBox(8.0F, -8.0F, -4.0F, 3.0F, 6.0F, 8.0F)
                        .texOffs(0, 160).addBox(-6.0F, -16.0F, -5.5F, 12.0F, 4.0F, 11.0F)
                        .texOffs(52, 160).addBox(-3.0F, -19.0F, -4.0F, 6.0F, 3.0F, 8.0F)
                        .texOffs(84, 160).addBox(-5.4F, -9.4F, -8.55F, 3.0F, 3.0F, 1.0F)
                        .texOffs(84, 160).addBox(2.4F, -9.4F, -8.55F, 3.0F, 3.0F, 1.0F)
                        .texOffs(96, 160).addBox(-4.45F, -8.45F, -8.75F, 1.0F, 1.0F, 1.0F)
                        .texOffs(96, 160).addBox(3.35F, -8.45F, -8.75F, 1.0F, 1.0F, 1.0F)
                        .texOffs(104, 160).addBox(-3.5F, -4.6F, -8.65F, 7.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(112, 160).addBox(-6.0F, -1.0F, -7.8F, 12.0F, 4.0F, 3.0F)
                        .texOffs(144, 160).addBox(-4.0F, 1.0F, -8.1F, 8.0F, 2.0F, 2.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float headYaw = entity.isDetached() ? netHeadYaw * 0.65F : Mth.clamp(netHeadYaw, -TerrariaGolemHeadEntity.MAX_ATTACHED_HEAD_BODY_YAW, TerrariaGolemHeadEntity.MAX_ATTACHED_HEAD_BODY_YAW);
        head.yRot = headYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.35F + Mth.sin(ageInTicks * 0.12F) * 0.05F;
        jaw.xRot = 0.22F + Mth.sin(ageInTicks * 0.32F) * 0.12F;
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
