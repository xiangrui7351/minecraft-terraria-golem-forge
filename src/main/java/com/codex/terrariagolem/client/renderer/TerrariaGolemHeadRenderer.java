package com.codex.terrariagolem.client.renderer;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.client.model.TerrariaGolemHeadModel;
import com.codex.terrariagolem.world.entity.TerrariaGolemHeadEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TerrariaGolemHeadRenderer extends MobRenderer<TerrariaGolemHeadEntity, TerrariaGolemHeadModel<TerrariaGolemHeadEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TerrariaGolemMod.MODID, "textures/entity/terraria_golem.png");

    public TerrariaGolemHeadRenderer(EntityRendererProvider.Context context) {
        super(context, new TerrariaGolemHeadModel<>(context.bakeLayer(TerrariaGolemHeadModel.LAYER_LOCATION)), 1.2F);
    }

    @Override
    protected void scale(TerrariaGolemHeadEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.72F, 1.72F, 1.72F);
    }

    @Override
    public ResourceLocation getTextureLocation(TerrariaGolemHeadEntity entity) {
        return TEXTURE;
    }
}
