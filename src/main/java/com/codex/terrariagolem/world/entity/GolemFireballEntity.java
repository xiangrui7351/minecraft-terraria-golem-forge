package com.codex.terrariagolem.world.entity;

import com.codex.terrariagolem.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GolemFireballEntity extends ThrowableProjectile implements ItemSupplier {
    private int bounces;

    public GolemFireballEntity(EntityType<? extends GolemFireballEntity> entityType, Level level) {
        super(entityType, level);
    }

    public GolemFireballEntity(Level level, LivingEntity owner) {
        super(ModEntities.GOLEM_FIREBALL.get(), owner, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D);
            }
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        } else if (tickCount > 160) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (level().isClientSide) {
            return;
        }

        if (!canHit(result.getEntity())) {
            discard();
            return;
        }

        Entity owner = getOwner();
        result.getEntity().hurt(damageSources().indirectMagic(this, owner), 12.0F);
        result.getEntity().setSecondsOnFire(5);
        explode(false, result.getEntity());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (level().isClientSide) {
            return;
        }

        BlockState state = level().getBlockState(result.getBlockPos());
        if (!state.isAir() && bounces < 1) {
            Vec3 motion = getDeltaMovement();
            Direction.Axis axis = result.getDirection().getAxis();
            double x = axis == Direction.Axis.X ? -motion.x * 0.72D : motion.x;
            double y = axis == Direction.Axis.Y ? -motion.y * 0.62D : motion.y;
            double z = axis == Direction.Axis.Z ? -motion.z * 0.72D : motion.z;
            setDeltaMovement(x, Math.max(y, 0.18D), z);
            bounces++;
            level().playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.7F, 0.6F);
        } else {
            explode(true, null);
        }
    }

    private boolean canHit(Entity entity) {
        return entity != getOwner()
                && !(entity instanceof TerrariaGolemEntity)
                && !(entity instanceof TerrariaGolemHeadEntity)
                && !(entity instanceof TerrariaGolemFistEntity);
    }

    private void explode(boolean hitBlock, Entity directHit) {
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.7F, 1.45F);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), hitBlock ? 16 : 28, 0.45D, 0.45D, 0.45D, 0.06D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), hitBlock ? 10 : 18, 0.35D, 0.35D, 0.35D, 0.04D);
        }

        AABB area = getBoundingBox().inflate(hitBlock ? 1.2D : 1.6D);
        List<LivingEntity> victims = level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity != directHit && canHit(entity));
        for (LivingEntity victim : victims) {
            victim.hurt(damageSources().indirectMagic(this, getOwner()), hitBlock ? 5.0F : 7.0F);
            victim.setSecondsOnFire(4);
        }
        discard();
    }

    @Override
    protected float getGravity() {
        return 0.035F;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public ItemStack getItem() {
        return Items.FIRE_CHARGE.getDefaultInstance();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Bounces", bounces);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        bounces = tag.getInt("Bounces");
    }
}
