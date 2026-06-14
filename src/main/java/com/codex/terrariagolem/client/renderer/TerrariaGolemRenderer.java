package com.codex.terrariagolem.client.renderer;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.client.model.TerrariaGolemModel;
import com.codex.terrariagolem.world.entity.TerrariaGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TerrariaGolemRenderer extends MobRenderer<TerrariaGolemEntity, TerrariaGolemModel<TerrariaGolemEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TerrariaGolemMod.MODID, "textures/entity/terraria_golem.png");

    public TerrariaGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new TerrariaGolemModel<>(context.bakeLayer(TerrariaGolemModel.LAYER_LOCATION)), 1.9F);
    }

    @Override
    protected void scale(TerrariaGolemEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.68F, 1.68F, 1.68F);
    }

    @Override
    public ResourceLocation getTextureLocation(TerrariaGolemEntity entity) {
        return TEXTURE;
    }
}
