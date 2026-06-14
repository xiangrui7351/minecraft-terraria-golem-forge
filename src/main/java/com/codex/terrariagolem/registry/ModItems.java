package com.codex.terrariagolem.registry;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.world.item.GolemTreasureBagItem;
import com.codex.terrariagolem.world.item.GolemPowerCellItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TerrariaGolemMod.MODID);

    public static final RegistryObject<Item> GOLEM_POWER_CELL = ITEMS.register("golem_power_cell",
            () -> new GolemPowerCellItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> GOLEM_TREASURE_BAG = ITEMS.register("golem_treasure_bag",
            () -> new GolemTreasureBagItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> GOLEM_ALTAR = ITEMS.register("golem_altar",
            () -> new BlockItem(ModBlocks.GOLEM_ALTAR.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> BEETLE_HUSK = ITEMS.register("beetle_husk",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SUN_CORE = ITEMS.register("sun_core",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> TERRARIA_GOLEM_SPAWN_EGG = ITEMS.register("terraria_golem_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TERRARIA_GOLEM, 0x6D5630, 0xF06A22, new Item.Properties()));

    private ModItems() {
    }
}
