package com.codex.terrariagolem.client.model;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.world.entity.TerrariaGolemFistEntity;
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

public class TerrariaGolemFistModel<T extends TerrariaGolemFistEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(TerrariaGolemMod.MODID, "terraria_golem_fist"), "main");

    private final ModelPart root;
    private final ModelPart fist;
    private final ModelPart chain;

    public TerrariaGolemFistModel(ModelPart root) {
        this.root = root;
        fist = root.getChild("fist");
        chain = root.getChild("chain");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("chain",
                CubeListBuilder.create()
                        .texOffs(104, 0).addBox(-1.0F, -3.0F, 1.0F, 2.0F, 6.0F, 192.0F)
                        .texOffs(112, 0).addBox(-3.0F, -1.0F, 1.0F, 6.0F, 2.0F, 192.0F)
                        .texOffs(120, 0).addBox(-2.0F, -2.0F, 1.0F, 4.0F, 4.0F, 192.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("fist",
                CubeListBuilder.create()
                        .texOffs(0, 184).addBox(-8.0F, -7.0F, -7.0F, 16.0F, 13.0F, 13.0F)
                        .texOffs(64, 184).addBox(-9.0F, -5.0F, -10.0F, 5.0F, 7.0F, 5.0F)
                        .texOffs(64, 184).addBox(-3.0F, -6.0F, -10.5F, 6.0F, 8.0F, 5.0F)
                        .texOffs(64, 184).mirror().addBox(4.0F, -5.0F, -10.0F, 5.0F, 7.0F, 5.0F)
                        .texOffs(96, 184).addBox(-10.0F, -1.0F, -4.0F, 4.0F, 6.0F, 7.0F)
                        .texOffs(96, 184).mirror().addBox(6.0F, -1.0F, -4.0F, 4.0F, 6.0F, 7.0F)
                        .texOffs(124, 184).addBox(-5.0F, -3.0F, 5.5F, 10.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        chain.visible = false;
        fist.zRot = Mth.sin(ageInTicks * 0.35F) * 0.05F;
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
