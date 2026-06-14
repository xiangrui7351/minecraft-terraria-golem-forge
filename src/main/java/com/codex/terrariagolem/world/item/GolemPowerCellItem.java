package com.codex.terrariagolem.world.item;

import com.codex.terrariagolem.registry.ModBlocks;
import com.codex.terrariagolem.world.block.GolemAltarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GolemPowerCellItem extends Item {
    public GolemPowerCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.is(ModBlocks.GOLEM_ALTAR.get())) {
            return GolemAltarBlock.activate(level, clickedPos, context.getPlayer(), context.getItemInHand());
        }

        return InteractionResult.PASS;
    }
}
