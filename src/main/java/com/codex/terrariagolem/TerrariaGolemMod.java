package com.codex.terrariagolem;

import com.codex.terrariagolem.registry.ModCreativeTabs;
import com.codex.terrariagolem.registry.ModBlocks;
import com.codex.terrariagolem.registry.ModEntities;
import com.codex.terrariagolem.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TerrariaGolemMod.MODID)
public class TerrariaGolemMod {
    public static final String MODID = "terrariagolem";

    public TerrariaGolemMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
