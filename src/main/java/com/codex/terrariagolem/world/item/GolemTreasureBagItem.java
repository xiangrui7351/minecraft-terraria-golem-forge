package com.codex.terrariagolem.world.item;

import com.codex.terrariagolem.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GolemTreasureBagItem extends Item {
    public GolemTreasureBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        if (!level.isClientSide) {
            RandomSource random = level.random;
            if (!player.getAbilities().instabuild) {
                bag.shrink(1);
            }

            giveOrDrop(player, new ItemStack(ModItems.SUN_CORE.get()));
            giveOrDrop(player, new ItemStack(ModItems.BEETLE_HUSK.get(), 6 + random.nextInt(5)));
            giveOrDrop(player, new ItemStack(Items.GOLD_INGOT, 18 + random.nextInt(13)));
            if (random.nextFloat() < 0.35F) {
                giveOrDrop(player, new ItemStack(ModItems.GOLEM_POWER_CELL.get()));
            }

            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.85F, 0.78F + random.nextFloat() * 0.16F);
        }

        return InteractionResultHolder.sidedSuccess(bag, level.isClientSide);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }
}
