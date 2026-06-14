package com.codex.terrariagolem.registry;

import com.codex.terrariagolem.TerrariaGolemMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TerrariaGolemMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.terrariagolem.main"))
            .icon(() -> ModItems.GOLEM_POWER_CELL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.GOLEM_POWER_CELL.get());
                output.accept(ModItems.GOLEM_ALTAR.get());
                output.accept(ModItems.GOLEM_TREASURE_BAG.get());
                output.accept(ModItems.BEETLE_HUSK.get());
                output.accept(ModItems.SUN_CORE.get());
                output.accept(ModItems.TERRARIA_GOLEM_SPAWN_EGG.get());
            })
            .build());

    private ModCreativeTabs() {
    }
}
