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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TerrariaGolemFistEntity extends Monster {
    private static final EntityDataAccessor<Boolean> LEFT = SynchedEntityData.defineId(TerrariaGolemFistEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> EXTENSION = SynchedEntityData.defineId(TerrariaGolemFistEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(TerrariaGolemFistEntity.class, EntityDataSerializers.INT);
    private static final double PHASE_ONE_PUNCH_RANGE = 40.0D;
    private static final double PHASE_TWO_PUNCH_RANGE = 56.0D;
    private static final double PHASE_ONE_MAX_PUNCH_SPEED = 2.25D;
    private static final double PHASE_TWO_MAX_PUNCH_SPEED = 2.65D;

    private UUID ownerUuid;
    private int cooldown = 34;
    private int attackTicks;
    private Vec3 attackDirection = Vec3.ZERO;
    private UUID lastHitUuid;

    public TerrariaGolemFistEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        xpReward = 0;
        setNoGravity(true);
        noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 56.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LEFT, true);
        entityData.define(EXTENSION, 0.0F);
        entityData.define(OWNER_ID, 0);
    }

    public void configure(TerrariaGolemEntity owner, boolean left) {
        ownerUuid = owner.getUUID();
        entityData.set(OWNER_ID, owner.getId());
        entityData.set(LEFT, left);
    }

    public boolean isLeftFist() {
        return entityData.get(LEFT);
    }

    public float getExtension() {
        return entityData.get(EXTENSION);
    }

    public Entity getOwnerEntity() {
        int ownerId = entityData.get(OWNER_ID);
        return ownerId == 0 ? null : level().getEntity(ownerId);
    }

    private Optional<TerrariaGolemEntity> getBody() {
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
            Entity owner = getOwnerEntity();
            if (owner instanceof TerrariaGolemEntity body && getExtension() <= 0.04F) {
                syncIdleWithBody(body, anchor(body));
            }
            if (getExtension() > 0.04F && tickCount % 3 == 0) {
                level().addParticle(ParticleTypes.FLAME, getRandomX(0.55D), getY() + 0.7D, getRandomZ(0.55D), 0.0D, 0.02D, 0.0D);
            }
            return;
        }

        Optional<TerrariaGolemEntity> body = getBody();
        if (body.isEmpty()) {
            discard();
            return;
        }

        TerrariaGolemEntity golem = body.get();
        if (entityData.get(OWNER_ID) != golem.getId()) {
            entityData.set(OWNER_ID, golem.getId());
        }
        LivingEntity target = golem.getTarget();
        if (!TerrariaGolemEntity.isValidTarget(target)) {
            target = null;
        }
        Vec3 anchor = anchor(golem);

        if (attackTicks <= 0) {
            entityData.set(EXTENSION, 0.0F);
            golem.setFistExtension(isLeftFist(), 0.0F);
            syncIdleWithBody(golem, anchor);
            if (target != null && !otherFistIsBusy(golem) && canPunchTarget(golem, target) && --cooldown <= 0) {
                startPunch(golem, target, anchor);
            }
            return;
        }

        tickPunch(golem, target, anchor);
    }

    private Vec3 anchor(TerrariaGolemEntity body) {
        return body.getFistAnchor(isLeftFist());
    }

    private boolean canPunchTarget(TerrariaGolemEntity body, LivingEntity target) {
        Vec3 side = body.getHorizontalLook().cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        double lateral = target.position().subtract(body.position()).dot(side);
        double vertical = Math.abs(target.getY() - body.getY());
        return vertical > 2.2D || Math.abs(lateral) < 1.4D || (isLeftFist() ? lateral < 0.0D : lateral > 0.0D);
    }

    private boolean otherFistIsBusy(TerrariaGolemEntity body) {
        return isLeftFist() ? body.getRightArmExtension() > 0.01F : body.getLeftArmExtension() > 0.01F;
    }

    private void startPunch(TerrariaGolemEntity body, LivingEntity target, Vec3 anchor) {
        Vec3 aim = target.getEyePosition().subtract(anchor);
        if (aim.lengthSqr() < 0.01D) {
            aim = body.getHorizontalLook();
        }

        attackDirection = aim.normalize();
        attackTicks = 1;
        lastHitUuid = null;
        entityData.set(EXTENSION, 0.02F);
        body.setFistExtension(isLeftFist(), 0.02F);
        cooldown = nextCooldown(body);
        level().playSound(null, blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.85F, 0.75F);
    }

    private int nextCooldown(TerrariaGolemEntity body) {
        float bodyPressure = 1.0F - body.getHealth() / body.getMaxHealth();
        float fistPressure = 1.0F - getHealth() / getMaxHealth();
        float pressure = Math.max(bodyPressure, fistPressure);
        int base = body.getPhase() >= 2 ? 18 + random.nextInt(12) : 30 + random.nextInt(20);
        return Math.max(10, base - Mth.floor(pressure * 18.0F));
    }

    private void tickPunch(TerrariaGolemEntity body, LivingEntity target, Vec3 anchor) {
        boolean fast = body.getPhase() >= 2;
        double range = punchRange(fast);
        float extension = extensionForTick(attackTicks, fast, range);
        entityData.set(EXTENSION, extension);
        body.setFistExtension(isLeftFist(), Math.max(extension, 0.02F));

        Vec3 position = anchor.add(attackDirection.scale(extension * range));
        moveTo(position.x, position.y, position.z, getYRot(), getXRot());
        faceChainTowardAnchor(anchor);

        if (attackTicks == windupTicks(fast) + 1) {
            level().playSound(null, blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 1.2F, 0.5F);
        }

        if (extension > 0.05F) {
            hitTargets(body);
        }

        attackTicks++;
        if (attackTicks > finishTicks(fast, range)) {
            attackTicks = 0;
            entityData.set(EXTENSION, 0.0F);
            body.setFistExtension(isLeftFist(), 0.0F);
        }
    }

    private double punchRange(boolean fast) {
        return fast ? PHASE_TWO_PUNCH_RANGE : PHASE_ONE_PUNCH_RANGE;
    }

    private double maxPunchSpeed(boolean fast) {
        return fast ? PHASE_TWO_MAX_PUNCH_SPEED : PHASE_ONE_MAX_PUNCH_SPEED;
    }

    private int windupTicks(boolean fast) {
        return fast ? 8 : 10;
    }

    private int extendTicks(boolean fast, double range) {
        return Mth.ceil(range / maxPunchSpeed(fast));
    }

    private int holdTicks(boolean fast) {
        return fast ? 6 : 8;
    }

    private int retractTicks(boolean fast, double range) {
        return Mth.ceil(range / (maxPunchSpeed(fast) * 1.15D));
    }

    private int finishTicks(boolean fast, double range) {
        return windupTicks(fast) + extendTicks(fast, range) + holdTicks(fast) + retractTicks(fast, range);
    }

    private float extensionForTick(int tick, boolean fast, double range) {
        int windup = windupTicks(fast);
        int extendEnd = windup + extendTicks(fast, range);
        int holdEnd = extendEnd + holdTicks(fast);
        int finish = holdEnd + retractTicks(fast, range);

        if (tick < windup) {
            return 0.0F;
        }
        if (tick < extendEnd) {
            return Mth.clamp((tick - windup) / (float) Math.max(1, extendEnd - windup), 0.0F, 1.0F);
        }
        if (tick < holdEnd) {
            return 1.0F;
        }
        return Mth.clamp(1.0F - (tick - holdEnd) / (float) Math.max(1, finish - holdEnd), 0.0F, 1.0F);
    }

    private void hitTargets(TerrariaGolemEntity body) {
        AABB area = getBoundingBox().inflate(0.55D);
        List<LivingEntity> victims = level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity != this
                && !body.isOwnedEntity(entity)
                && entity.isAlive()
                && !isAlliedTo(entity));

        for (LivingEntity victim : victims) {
            if (victim.getUUID().equals(lastHitUuid)) {
                continue;
            }

            lastHitUuid = victim.getUUID();
            victim.hurt(damageSources().mobAttack(this), body.getPhase() >= 2 ? 20.0F : 16.0F);
            Vec3 push = attackDirection.scale(1.35D);
            victim.push(push.x, 0.22D, push.z);
        }
    }

    private void faceAlong(Vec3 direction) {
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        setYRot((float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F);
        setXRot((float) (-(Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG)));
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    private void alignIdleWithBody(TerrariaGolemEntity body) {
        setYRot(body.getPartYaw());
        setXRot(0.0F);
        yRotO = getYRot();
        xRotO = getXRot();
        yBodyRot = getYRot();
        yBodyRotO = getYRot();
        yHeadRot = getYRot();
        yHeadRotO = getYRot();
    }

    private void syncIdleWithBody(TerrariaGolemEntity body, Vec3 anchor) {
        setDeltaMovement(Vec3.ZERO);
        moveTo(anchor.x, anchor.y, anchor.z, body.getPartYaw(), 0.0F);
        xo = anchor.x;
        yo = anchor.y;
        zo = anchor.z;
        alignIdleWithBody(body);
    }

    private void faceChainTowardAnchor(Vec3 anchor) {
        Vec3 chainDirection = anchor.subtract(position());
        if (chainDirection.lengthSqr() < 0.001D) {
            return;
        }
        faceAlong(chainDirection.normalize());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) || isFriendlyDamage(source)) {
            return false;
        }

        rememberAttacker(source);
        return super.hurt(source, amount);
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
    public boolean isAlliedTo(Entity entity) {
        Optional<TerrariaGolemEntity> body = getBody();
        return body.map(golem -> golem.isOwnedEntity(entity)).orElse(false) || super.isAlliedTo(entity);
    }

    @Override
    public void die(DamageSource damageSource) {
        getBody().ifPresent(body -> {
            body.setFistExtension(isLeftFist(), 0.0F);
            body.markFistDestroyed(isLeftFist());
        });
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + getBbHeight() * 0.5D, getZ(), 24, 0.55D, 0.35D, 0.55D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + getBbHeight() * 0.5D, getZ(), 14, 0.45D, 0.28D, 0.45D, 0.04D);
        }
        super.die(damageSource);
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
        tag.putBoolean("Left", isLeftFist());
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            ownerUuid = tag.getUUID("Owner");
        }
        entityData.set(LEFT, tag.getBoolean("Left"));
        cooldown = tag.getInt("Cooldown");
    }
}
