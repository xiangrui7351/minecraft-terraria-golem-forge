package com.codex.terrariagolem.event;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.client.model.TerrariaGolemFistModel;
import com.codex.terrariagolem.client.model.TerrariaGolemHeadModel;
import com.codex.terrariagolem.client.model.TerrariaGolemModel;
import com.codex.terrariagolem.client.renderer.TerrariaGolemFistRenderer;
import com.codex.terrariagolem.client.renderer.TerrariaGolemHeadRenderer;
import com.codex.terrariagolem.client.renderer.TerrariaGolemRenderer;
import com.codex.terrariagolem.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TerrariaGolemMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TerrariaGolemModel.LAYER_LOCATION, TerrariaGolemModel::createBodyLayer);
        event.registerLayerDefinition(TerrariaGolemHeadModel.LAYER_LOCATION, TerrariaGolemHeadModel::createBodyLayer);
        event.registerLayerDefinition(TerrariaGolemFistModel.LAYER_LOCATION, TerrariaGolemFistModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TERRARIA_GOLEM.get(), TerrariaGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.TERRARIA_GOLEM_HEAD.get(), TerrariaGolemHeadRenderer::new);
        event.registerEntityRenderer(ModEntities.TERRARIA_GOLEM_FIST.get(), TerrariaGolemFistRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_FIREBALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_EYE_BEAM.get(), ThrownItemRenderer::new);
    }

    private ModClientEvents() {
    }
}
