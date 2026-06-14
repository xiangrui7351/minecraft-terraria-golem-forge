package com.codex.terrariagolem.registry;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.world.block.GolemAltarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TerrariaGolemMod.MODID);

    public static final RegistryObject<Block> GOLEM_ALTAR = BLOCKS.register("golem_altar",
            () -> new GolemAltarBlock(BlockBehaviour.Properties.copy(Blocks.CHISELED_STONE_BRICKS)
                    .strength(8.0F, 36.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 14)
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .noOcclusion()));

    private ModBlocks() {
    }
}
