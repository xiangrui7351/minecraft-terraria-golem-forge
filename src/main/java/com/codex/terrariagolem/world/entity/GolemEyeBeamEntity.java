package com.codex.terrariagolem.world.entity;

import com.codex.terrariagolem.registry.ModEntities;
import com.codex.terrariagolem.registry.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Vector3f;

public class GolemEyeBeamEntity extends ThrowableProjectile implements ItemSupplier {
    private int pierces;

    public GolemEyeBeamEntity(EntityType<? extends GolemEyeBeamEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public GolemEyeBeamEntity(Level level, LivingEntity owner) {
        super(ModEntities.GOLEM_EYE_BEAM.get(), owner, level);
        noPhysics = true;
    }

    @Override
    public void tick() {
        noPhysics = true;
        super.tick();

        if (level().isClientSide) {
            level().addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.35F, 0.05F), 1.25F), getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        } else if (tickCount > 45) {
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
            return;
        }

        Entity owner = getOwner();
        result.getEntity().hurt(damageSources().indirectMagic(this, owner), 9.0F);
        pierces++;
        if (pierces >= 2) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    private boolean canHit(Entity entity) {
        return entity != getOwner()
                && !(entity instanceof TerrariaGolemEntity)
                && !(entity instanceof TerrariaGolemHeadEntity)
                && !(entity instanceof TerrariaGolemFistEntity);
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public ItemStack getItem() {
        return ModItems.SUN_CORE.get().getDefaultInstance();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Pierces", pierces);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        pierces = tag.getInt("Pierces");
    }
}
