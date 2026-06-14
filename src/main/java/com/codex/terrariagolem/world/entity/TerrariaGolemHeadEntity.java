package com.codex.terrariagolem.world.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class TerrariaGolemHeadEntity extends Monster {
    public static final float MAX_ATTACHED_HEAD_BODY_YAW = 90.0F;

    private static final EntityDataAccessor<Boolean> DETACHED = SynchedEntityData.defineId(TerrariaGolemHeadEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerUuid;
    private int laserCooldown = 36;
    private int fireballCooldown = 28;
    private int laserBurstTicks;

    public TerrariaGolemHeadEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        xpReward = 0;
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 750.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 56.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DETACHED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 32.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, TerrariaGolemEntity::isValidTarget));
    }

    public void setOwner(TerrariaGolemEntity owner) {
        ownerUuid = owner.getUUID();
    }

    public boolean isDetached() {
        return entityData.get(DETACHED);
    }

    public float getHeadHealthRatio() {
        return isDetached() ? 0.0F : getHealth() / getMaxHealth();
    }

    public Optional<TerrariaGolemEntity> getBody() {
        if (!(level() instanceof ServerLevel serverLevel) || ownerUuid == null) {
            return Optional.empty();
        }

        Entity entity = serverLevel.getEntity(ownerUuid);
        if (entity instanceof TerrariaGolemEntity golem && golem.isAlive()) {
            return Optional.of(golem);
        }
        return Optional.empty();
    }

    @Override
    public void tick() {
        setNoGravity(true);
        noPhysics = true;
        super.tick();

        if (level().isClientSide) {
            if (tickCount % 3 == 0) {
                level().addParticle(isDetached() ? ParticleTypes.FLAME : ParticleTypes.SMOKE, getRandomX(0.45D), getY() + 0.3D, getRandomZ(0.45D), 0.0D, 0.02D, 0.0D);
            }
            return;
        }

        Optional<TerrariaGolemEntity> body = getBody();
        if (body.isEmpty()) {
            discard();
            return;
        }

        TerrariaGolemEntity golem = body.get();
        LivingEntity target = golem.getTarget();
        if (!TerrariaGolemEntity.isValidTarget(target)) {
            target = getTarget();
            if (!TerrariaGolemEntity.isValidTarget(target)) {
                setTarget(null);
                target = null;
            }
        } else if (getTarget() != target) {
            setTarget(target);
        }

        if (isDetached()) {
            hoverAroundBody(golem, target);
        } else {
            stayAttachedToBody(golem, target);
        }
        tickAttacks(golem, target);
    }

    private void stayAttachedToBody(TerrariaGolemEntity body, LivingEntity target) {
        Vec3 mount = body.getHeadAnchor();
        float bodyYaw = body.getPartYaw();
        float headYaw = bodyYaw;
        float headPitch = body.getXRot();

        moveTo(mount.x, mount.y, mount.z, bodyYaw, headPitch);
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
        noPhysics = true;

        if (target != null) {
            Vec3 look = target.getEyePosition().subtract(getEyePosition());
            headYaw = clampAttachedHeadYaw(bodyYaw, yawFor(look));
            headPitch = pitchFor(look);
        }

        setYRot(bodyYaw);
        yBodyRot = bodyYaw;
        yBodyRotO = bodyYaw;
        yHeadRot = headYaw;
        yHeadRotO = clampAttachedHeadYaw(bodyYaw, yHeadRotO);
        setXRot(headPitch);
        xRotO = headPitch;
    }

    private float clampAttachedHeadYaw(float bodyYaw, float headYaw) {
        float offset = Mth.wrapDegrees(headYaw - bodyYaw);
        return bodyYaw + Mth.clamp(offset, -MAX_ATTACHED_HEAD_BODY_YAW, MAX_ATTACHED_HEAD_BODY_YAW);
    }

    private float yawFor(Vec3 look) {
        return (float) (Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private float pitchFor(Vec3 look) {
        double horizontalDistance = Math.sqrt(look.x * look.x + look.z * look.z);
        return (float) (-(Mth.atan2(look.y, horizontalDistance) * Mth.RAD_TO_DEG));
    }

    private void hoverAroundBody(TerrariaGolemEntity body, LivingEntity target) {
        Vec3 base = body.position().add(0.0D, body.getBbHeight() * 2.0D + 2.0D + Mth.sin(tickCount * 0.08F) * 0.5D, 0.0D);
        Vec3 horizontal = target == null ? body.getHorizontalLook() : target.position().subtract(body.position());
        horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
        if (horizontal.lengthSqr() < 0.01D) {
            horizontal = body.getHorizontalLook();
        }

        Vec3 forward = horizontal.normalize();
        Vec3 side = forward.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        double orbit = Mth.sin(tickCount * 0.055F) * 2.2D;
        Vec3 desired = base.add(forward.scale(4.2D)).add(side.scale(orbit));
        Vec3 pull = desired.subtract(position());
        double speed = Math.min(0.72D, pull.length() * 0.16D);

        if (pull.lengthSqr() > 0.001D) {
            setDeltaMovement(getDeltaMovement().scale(0.55D).add(pull.normalize().scale(speed)));
        } else {
            setDeltaMovement(getDeltaMovement().scale(0.55D));
        }

        if (target != null) {
            getLookControl().setLookAt(target, 45.0F, 45.0F);
        }
    }

    private void tickAttacks(TerrariaGolemEntity body, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            laserBurstTicks = 0;
            return;
        }

        float headPressure = isDetached() ? 1.0F - body.getHealth() / body.getMaxHealth() : 1.0F - getHealth() / getMaxHealth();
        int speedBonus = Mth.floor(headPressure * 26.0F) + (isDetached() ? 8 : 0);

        if (laserBurstTicks > 0) {
            if (laserBurstTicks % 6 == 0) {
                shootEyeBeams(target);
            }
            laserBurstTicks--;
            return;
        }

        boolean canUseLasers = isDetached() || getHeadHealthRatio() <= 0.5F;
        if (canUseLasers && --laserCooldown <= 0) {
            laserBurstTicks = headPressure > 0.72F ? 30 : 18;
            laserCooldown = Math.max(isDetached() ? 16 : 28, 54 + random.nextInt(20) - speedBonus);
            level().playSound(null, blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.8F);
        }

        if (--fireballCooldown <= 0) {
            shootFireball(target);
            fireballCooldown = Math.max(isDetached() ? 18 : 22, 42 + random.nextInt(18) - speedBonus);
        }
    }

    private void shootEyeBeams(LivingEntity target) {
        Vec3 look = target.getEyePosition().subtract(getEyePosition()).normalize();
        Vec3 side = look.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        spawnEyeBeam(target, side.scale(-0.28D));
        spawnEyeBeam(target, side.scale(0.28D));
        level().playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.8F, 1.85F);
    }

    private void spawnEyeBeam(LivingEntity target, Vec3 eyeOffset) {
        GolemEyeBeamEntity beam = new GolemEyeBeamEntity(level(), this);
        Vec3 origin = getEyePosition().add(eyeOffset).add(0.0D, -0.08D, 0.0D);
        Vec3 aim = target.getEyePosition().subtract(origin).normalize();
        beam.setPos(origin.x, origin.y, origin.z);
        beam.shoot(aim.x, aim.y, aim.z, isDetached() ? 2.05F : 1.8F, 0.45F);
        level().addFreshEntity(beam);
    }

    private void shootFireball(LivingEntity target) {
        GolemFireballEntity fireball = new GolemFireballEntity(level(), this);
        Vec3 origin = getEyePosition().add(getLookAngle().scale(0.9D)).add(0.0D, -0.2D, 0.0D);
        Vec3 aim = target.getEyePosition().subtract(origin).normalize();
        fireball.setPos(origin.x, origin.y, origin.z);
        fireball.shoot(aim.x, aim.y + 0.05D, aim.z, isDetached() ? 1.12F : 0.96F, 1.4F);
        level().addFreshEntity(fireball);
        level().playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 0.68F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) || isDetached() || isFriendlyDamage(source)) {
            return false;
        }

        rememberAttacker(source);
        if (amount >= getHealth()) {
            setHealth(1.0F);
            detachFromBody();
            return true;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && getHealth() <= 1.0F && !isDetached()) {
            setHealth(1.0F);
            detachFromBody();
        }
        return hurt;
    }

    private void rememberAttacker(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            getBody().ifPresent(body -> {
                if (TerrariaGolemEntity.isValidTarget(attacker) && !body.isOwnedEntity(attacker)) {
                    body.setTarget(attacker);
                }
            });
        }
    }

    private boolean isFriendlyDamage(DamageSource source) {
        Optional<TerrariaGolemEntity> body = getBody();
        return body.isPresent() && (body.get().isOwnedEntity(source.getEntity()) || body.get().isOwnedEntity(source.getDirectEntity()));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return TerrariaGolemEntity.isValidTarget(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        Optional<TerrariaGolemEntity> body = getBody();
        return body.map(golem -> golem.isOwnedEntity(entity)).orElse(false) || super.isAlliedTo(entity);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (getBody().isPresent()) {
            setHealth(1.0F);
            detachFromBody();
            return;
        }
        super.die(damageSource);
    }

    public void detachFromBody() {
        if (isDetached()) {
            return;
        }
        entityData.set(DETACHED, true);
        setInvulnerable(true);
        setHealth(1.0F);
        noPhysics = true;
        laserCooldown = 12;
        fireballCooldown = 24;
        level().playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.4F, 0.72F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + getBbHeight() * 0.5D, getZ(), 32, 0.65D, 0.38D, 0.65D, 0.06D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + getBbHeight() * 0.5D, getZ(), 40, 0.8D, 0.45D, 0.8D, 0.04D);
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.putBoolean("Detached", isDetached());
        tag.putInt("LaserCooldown", laserCooldown);
        tag.putInt("FireballCooldown", fireballCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            ownerUuid = tag.getUUID("Owner");
        }
        boolean detached = tag.getBoolean("Detached");
        entityData.set(DETACHED, detached);
        setInvulnerable(detached);
        laserCooldown = tag.getInt("LaserCooldown");
        fireballCooldown = tag.getInt("FireballCooldown");
    }
}
