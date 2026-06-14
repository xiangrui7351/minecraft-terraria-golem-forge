package com.codex.terrariagolem.world.block;

import com.codex.terrariagolem.registry.ModEntities;
import com.codex.terrariagolem.registry.ModItems;
import com.codex.terrariagolem.world.entity.TerrariaGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class GolemAltarBlock extends Block {
    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            Block.box(3.0D, 3.0D, 3.0D, 13.0D, 6.0D, 13.0D),
            Block.box(1.0D, 6.0D, 1.0D, 15.0D, 8.0D, 15.0D),
            Block.box(4.0D, 8.0D, 4.0D, 12.0D, 10.0D, 12.0D));
    private static final VoxelShape OUTLINE_SHAPE = Shapes.or(
            BASE_SHAPE,
            Block.box(3.0D, 13.0D, 3.0D, 13.0D, 24.0D, 13.0D));

    public GolemAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.GOLEM_POWER_CELL.get())) {
            return InteractionResult.PASS;
        }

        return activate(level, pos, player, stack);
    }

    public static InteractionResult activate(Level level, BlockPos altarPos, Player player, ItemStack powerCell) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        List<TerrariaGolemEntity> existing = serverLevel.getEntitiesOfClass(TerrariaGolemEntity.class, new AABB(altarPos).inflate(48.0D), TerrariaGolemEntity::isAlive);
        if (!existing.isEmpty()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.terrariagolem.altar_awake"), true);
            }
            serverLevel.playSound(null, altarPos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 0.75F, 0.6F);
            return InteractionResult.SUCCESS;
        }

        BlockPos spawnPos = altarPos.above();
        TerrariaGolemEntity golem = ModEntities.TERRARIA_GOLEM.get().create(serverLevel);
        if (golem == null) {
            return InteractionResult.FAIL;
        }

        float yaw = player == null ? 0.0F : player.getYRot() + 180.0F;
        golem.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        serverLevel.addFreshEntity(golem);
        serverLevel.playSound(null, altarPos, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.6F, 0.48F);
        serverLevel.playSound(null, altarPos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.1F, 0.72F);
        serverLevel.sendParticles(ParticleTypes.FLAME, altarPos.getX() + 0.5D, altarPos.getY() + 1.42D, altarPos.getZ() + 0.5D, 42, 0.45D, 0.28D, 0.45D, 0.045D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, altarPos.getX() + 0.5D, altarPos.getY() + 1.55D, altarPos.getZ() + 0.5D, 22, 0.28D, 0.2D, 0.28D, 0.025D);

        if (player != null && !player.getAbilities().instabuild) {
            powerCell.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.75F) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            double y = pos.getY() + 1.45D + (random.nextDouble() - 0.5D) * 0.12D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
        if (random.nextFloat() < 0.25F) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 1.35D, pos.getZ() + 0.5D, 0.0D, 0.015D, 0.0D);
        }
    }
}
