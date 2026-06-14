package com.codex.terrariagolem.event;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.registry.ModEntities;
import com.codex.terrariagolem.world.entity.TerrariaGolemEntity;
import com.codex.terrariagolem.world.entity.TerrariaGolemFistEntity;
import com.codex.terrariagolem.world.entity.TerrariaGolemHeadEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TerrariaGolemMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCommonEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TERRARIA_GOLEM.get(), TerrariaGolemEntity.createAttributes().build());
        event.put(ModEntities.TERRARIA_GOLEM_HEAD.get(), TerrariaGolemHeadEntity.createAttributes().build());
        event.put(ModEntities.TERRARIA_GOLEM_FIST.get(), TerrariaGolemFistEntity.createAttributes().build());
    }

    private ModCommonEvents() {
    }
}
