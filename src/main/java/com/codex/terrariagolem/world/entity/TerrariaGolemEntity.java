package com.codex.terrariagolem.world.entity;

import com.codex.terrariagolem.registry.ModEntities;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TerrariaGolemEntity extends Monster {
    public static final int ATTACK_IDLE = 0;
    public static final int ATTACK_LEFT_PUNCH = 1;
    public static final int ATTACK_RIGHT_PUNCH = 2;
    public static final int ATTACK_FIREBALL = 3;
    public static final int ATTACK_LASER = 4;
    public static final int ATTACK_STOMP = 5;

    private static final EntityDataAccessor<Integer> ATTACK_STATE = SynchedEntityData.defineId(TerrariaGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TICKS = SynchedEntityData.defineId(TerrariaGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(TerrariaGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LEFT_ARM_EXTENSION = SynchedEntityData.defineId(TerrariaGolemEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RIGHT_ARM_EXTENSION = SynchedEntityData.defineId(TerrariaGolemEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> LEFT_FIST_ALIVE = SynchedEntityData.defineId(TerrariaGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RIGHT_FIST_ALIVE = SynchedEntityData.defineId(TerrariaGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bodyBossEvent = new ServerBossEvent(Component.translatable("entity.terrariagolem.terraria_golem"), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> entitiesHitThisAttack = new HashSet<>();

    private int actionCooldown = 35;
    private int stompCooldown;
    private boolean stompHasLanded;
    private boolean leftFistDefeated;
    private boolean rightFistDefeated;
    private UUID headUuid;
    private UUID leftFistUuid;
    private UUID rightFistUuid;

    public TerrariaGolemEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        xpReward = 80;
        setMaxUpStep(1.25F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.ARMOR, 18.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 56.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ATTACK_STATE, ATTACK_IDLE);
        entityData.define(ATTACK_TICKS, 0);
        entityData.define(PHASE, 0);
        entityData.define(LEFT_ARM_EXTENSION, 0.0F);
        entityData.define(RIGHT_ARM_EXTENSION, 0.0F);
        entityData.define(LEFT_FIST_ALIVE, true);
        entityData.define(RIGHT_FIST_ALIVE, true);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, TerrariaGolemEntity::isValidTarget));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide && tickCount % 4 == 0) {
            level().addParticle(ParticleTypes.SMOKE, getRandomX(0.8D), getY() + 0.2D, getRandomZ(0.8D), 0.0D, 0.04D, 0.0D);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        if (!isValidTarget(target)) {
            setTarget(null);
            target = null;
        }
        if (target != null) {
            turnBodyToward(target);
        }

        ensureHead();
        ensureFists();
        updatePhase();
        updateBossBars();
        if (stompCooldown > 0) {
            stompCooldown--;
        }

        if (target == null) {
            finishAttack(30);
            getNavigation().stop();
            return;
        }

        getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (getAttackState() == ATTACK_IDLE) {
            moveTowardTarget(target);
            if (--actionCooldown <= 0) {
                chooseAttack(target);
            }
        } else {
            tickAttack(target);
        }
    }

    private void updatePhase() {
        TerrariaGolemHeadEntity head = getTrackedHead();
        int newPhase = 0;
        if (head != null && head.isAlive()) {
            if (head.isDetached()) {
                newPhase = 2;
            } else if (head.getHeadHealthRatio() <= 0.5F) {
                newPhase = 1;
            }
        }

        if (newPhase != getPhase()) {
            entityData.set(PHASE, newPhase);
            level().playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.8F, 0.55F + newPhase * 0.18F);
            bodyBossEvent.setColor(newPhase >= 2 ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.YELLOW);
        }
    }

    private void updateBossBars() {
        bodyBossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        bodyBossEvent.setName(compactBossBarName());
    }

    private Component compactBossBarName() {
        TerrariaGolemHeadEntity head = getTrackedHead();
        Component headStatus = head == null || !head.isAlive()
                ? Component.translatable("bossbar.terrariagolem.missing")
                : head.isDetached()
                ? Component.translatable("bossbar.terrariagolem.detached")
                : healthStatus(head);
        Component bodyStatus = isBodyProtectedByHead()
                ? Component.translatable("bossbar.terrariagolem.locked")
                : healthStatus(this);

        return Component.translatable(
                "bossbar.terrariagolem.compact",
                Component.translatable("entity.terrariagolem.terraria_golem"),
                bodyStatus,
                headStatus,
                fistStatus(getTrackedFist(true), leftFistDefeated),
                fistStatus(getTrackedFist(false), rightFistDefeated));
    }

    private Component healthStatus(LivingEntity entity) {
        int percent = Math.round(Mth.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F) * 100.0F);
        return Component.literal(percent + "%");
    }

    private Component fistStatus(TerrariaGolemFistEntity fist, boolean defeated) {
        if (defeated) {
            return Component.translatable("bossbar.terrariagolem.destroyed");
        }
        return fist == null || !fist.isAlive() ? Component.translatable("bossbar.terrariagolem.missing") : healthStatus(fist);
    }

    private void moveTowardTarget(LivingEntity target) {
        double distanceSqr = distanceToSqr(target);
        double preferredDistance = getPhase() >= 2 ? 18.0D : 7.0D;
        if (distanceSqr > preferredDistance) {
            getNavigation().moveTo(target, getPhase() >= 2 ? 1.15D : 0.86D);
        } else {
            getNavigation().stop();
        }
    }

    private void turnBodyToward(LivingEntity target) {
        Vec3 toward = target.position().subtract(position());
        Vec3 horizontal = new Vec3(toward.x, 0.0D, toward.z);
        if (horizontal.lengthSqr() < 0.001D) {
            return;
        }

        float wantedYaw = (float) (Mth.atan2(horizontal.z, horizontal.x) * Mth.RAD_TO_DEG) - 90.0F;
        float yaw = Mth.approachDegrees(getYRot(), wantedYaw, 8.0F);
        setYRot(yaw);
        yBodyRot = yaw;
        yHeadRot = yaw;
    }

    private void chooseAttack(LivingEntity target) {
        double distanceSqr = distanceToSqr(target);
        float missingBodyHealth = 1.0F - getHealth() / getMaxHealth();
        int destroyedFists = (leftFistDefeated ? 1 : 0) + (rightFistDefeated ? 1 : 0);
        float stompChance = 0.06F + missingBodyHealth * 0.12F + destroyedFists * 0.04F + (getPhase() >= 2 ? 0.04F : 0.0F);
        double jumpDistanceSqr = getPhase() >= 2 ? 144.0D : 196.0D;
        double forcedJumpDistanceSqr = getPhase() >= 2 ? 324.0D : 400.0D;
        boolean canStomp = stompCooldown <= 0 && onGround();

        if (canStomp && (distanceSqr > forcedJumpDistanceSqr || distanceSqr > jumpDistanceSqr && random.nextFloat() < 0.35F || random.nextFloat() < stompChance)) {
            startAttack(ATTACK_STOMP);
        } else {
            actionCooldown = nextActionCooldown(20, 42);
        }
    }

    private int nextActionCooldown(int min, int max) {
        float pressure = 1.0F - getHealth() / getMaxHealth();
        int span = Math.max(1, max - min);
        int base = min + random.nextInt(span + 1);
        return Math.max(5, base - Mth.floor(pressure * 18.0F) - getPhase() * 4);
    }

    private int nextStompCooldown() {
        float pressure = 1.0F - getHealth() / getMaxHealth();
        int base = getPhase() >= 2 ? 85 + random.nextInt(46) : 120 + random.nextInt(61);
        return Math.max(getPhase() >= 2 ? 65 : 95, base - Mth.floor(pressure * 28.0F));
    }

    private void startAttack(int attack) {
        entityData.set(ATTACK_STATE, attack);
        entityData.set(ATTACK_TICKS, 0);
        stompHasLanded = false;
        entitiesHitThisAttack.clear();
        getNavigation().stop();
        if (attack == ATTACK_STOMP) {
            stompCooldown = nextStompCooldown();
        }
    }

    private void tickAttack(LivingEntity target) {
        int tick = getAttackTicks() + 1;
        entityData.set(ATTACK_TICKS, tick);

        if (getAttackState() == ATTACK_STOMP) {
            tickStomp(target, tick);
        } else {
            finishAttack(25);
        }
    }

    public void setFistExtension(boolean left, float extension) {
        entityData.set(left ? LEFT_ARM_EXTENSION : RIGHT_ARM_EXTENSION, Mth.clamp(extension, 0.0F, 1.0F));
    }

    public void markFistDestroyed(boolean left) {
        if (left) {
            leftFistDefeated = true;
            leftFistUuid = null;
            entityData.set(LEFT_FIST_ALIVE, false);
            entityData.set(LEFT_ARM_EXTENSION, 0.0F);
        } else {
            rightFistDefeated = true;
            rightFistUuid = null;
            entityData.set(RIGHT_FIST_ALIVE, false);
            entityData.set(RIGHT_ARM_EXTENSION, 0.0F);
        }
    }

    private boolean isNearLine(LivingEntity entity, Vec3 start, Vec3 end, double radius) {
        Vec3 line = end.subtract(start);
        Vec3 point = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        double t = Mth.clamp(point.subtract(start).dot(line) / line.lengthSqr(), 0.0D, 1.0D);
        Vec3 closest = start.add(line.scale(t));
        return point.distanceToSqr(closest) <= radius * radius;
    }

    private TerrariaGolemHeadEntity ensureHead() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        TerrariaGolemHeadEntity trackedHead = getTrackedHead();
        if (trackedHead != null && trackedHead.isAlive()) {
            return trackedHead;
        }

        TerrariaGolemHeadEntity head = ModEntities.TERRARIA_GOLEM_HEAD.get().create(serverLevel);
        if (head == null) {
            return null;
        }

        Vec3 spawn = getHeadAnchor();
        head.setOwner(this);
        head.setTarget(getTarget());
        head.moveTo(spawn.x, spawn.y, spawn.z, getYRot(), getXRot());
        serverLevel.addFreshEntity(head);
        headUuid = head.getUUID();
        return head;
    }

    private void ensureFists() {
        if (!(level() instanceof ServerLevel)) {
            return;
        }

        if (!leftFistDefeated && getTrackedFist(true) == null) {
            TerrariaGolemFistEntity fist = spawnFist(true);
            leftFistUuid = fist == null ? null : fist.getUUID();
            entityData.set(LEFT_FIST_ALIVE, fist != null);
        }

        if (!rightFistDefeated && getTrackedFist(false) == null) {
            TerrariaGolemFistEntity fist = spawnFist(false);
            rightFistUuid = fist == null ? null : fist.getUUID();
            entityData.set(RIGHT_FIST_ALIVE, fist != null);
        }
    }

    private TerrariaGolemFistEntity spawnFist(boolean left) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        TerrariaGolemFistEntity fist = ModEntities.TERRARIA_GOLEM_FIST.get().create(serverLevel);
        if (fist == null) {
            return null;
        }

        Vec3 spawn = getFistAnchor(left);
        fist.configure(this, left);
        fist.moveTo(spawn.x, spawn.y, spawn.z, getYRot(), 0.0F);
        serverLevel.addFreshEntity(fist);
        return fist;
    }

    private TerrariaGolemHeadEntity getTrackedHead() {
        if (!(level() instanceof ServerLevel serverLevel) || headUuid == null) {
            return null;
        }

        Entity entity = serverLevel.getEntity(headUuid);
        return entity instanceof TerrariaGolemHeadEntity head ? head : null;
    }

    private TerrariaGolemFistEntity getTrackedFist(boolean left) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        UUID uuid = left ? leftFistUuid : rightFistUuid;
        if (uuid == null) {
            return null;
        }

        Entity entity = serverLevel.getEntity(uuid);
        return entity instanceof TerrariaGolemFistEntity fist && fist.isAlive() ? fist : null;
    }

    private void discardHead() {
        if (level() instanceof ServerLevel serverLevel && headUuid != null && serverLevel.getEntity(headUuid) instanceof TerrariaGolemHeadEntity head) {
            head.discard();
        }
        headUuid = null;
    }

    private void discardFists() {
        if (level() instanceof ServerLevel serverLevel) {
            if (leftFistUuid != null && serverLevel.getEntity(leftFistUuid) instanceof TerrariaGolemFistEntity leftFist) {
                leftFist.discard();
            }
            if (rightFistUuid != null && serverLevel.getEntity(rightFistUuid) instanceof TerrariaGolemFistEntity rightFist) {
                rightFist.discard();
            }
        }
        leftFistUuid = null;
        rightFistUuid = null;
    }

    private void tickStomp(LivingEntity target, int tick) {
        if (tick == 8) {
            Vec3 toward = target.position().subtract(position());
            Vec3 horizontal = new Vec3(toward.x, 0.0D, toward.z);
            if (horizontal.lengthSqr() < 0.01D) {
                horizontal = getHorizontalLook();
            }

            double missingBodyHealth = 1.0D - getHealth() / getMaxHealth();
            double horizontalDistance = horizontal.length();
            double distanceBoost = Mth.clamp((horizontalDistance - 8.0D) * 0.035D, 0.0D, getPhase() >= 2 ? 0.62D : 0.48D);
            double leap = (getPhase() >= 2 ? 0.88D : 0.68D) + missingBodyHealth * 0.2D + distanceBoost;
            double lift = (getPhase() >= 2 ? 0.88D : 0.7D) + Math.min(distanceBoost * 0.18D, 0.1D);
            setDeltaMovement(horizontal.normalize().scale(leap).add(0.0D, lift, 0.0D));
            hasImpulse = true;
            fallDistance = 0.0F;
            level().playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.4F, 0.72F);
        }

        if (!stompHasLanded && tick > 13 && onGround()) {
            stompHasLanded = true;
            fallDistance = 0.0F;
            doGroundSlam();
        }

        if (tick >= 52 || stompHasLanded && tick >= 24) {
            finishAttack(nextActionCooldown(13, 28));
        }
    }

    private void doGroundSlam() {
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.25F, 0.55F);
        AABB area = getBoundingBox().inflate(getPhase() >= 2 ? 8.0D : 5.8D, 2.0D, getPhase() >= 2 ? 8.0D : 5.8D);
        List<LivingEntity> victims = level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity != this && entity.isAlive() && !isOwnedEntity(entity) && !isAlliedTo(entity));

        for (LivingEntity victim : victims) {
            double distance = Math.max(0.75D, distanceTo(victim));
            float damage = (float) Mth.clamp((getPhase() >= 2 ? 28.0D : 22.0D) - distance * 2.0D, 8.0D, getPhase() >= 2 ? 28.0D : 22.0D);
            victim.hurt(damageSources().mobAttack(this), damage);
            Vec3 push = victim.position().subtract(position()).normalize().scale(1.4D / distance);
            victim.push(push.x, 0.55D, push.z);
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState()), getX(), getY() + 0.1D, getZ(), 80, 3.0D, 0.2D, 3.0D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.2D, getZ(), 16, 2.4D, 0.1D, 2.4D, 0.04D);
        }
    }

    private void finishAttack(int cooldown) {
        entityData.set(ATTACK_STATE, ATTACK_IDLE);
        entityData.set(ATTACK_TICKS, 0);
        entitiesHitThisAttack.clear();
        actionCooldown = Math.max(getPhase() >= 2 ? 5 : 8, cooldown - getPhase() * 4);
    }

    public Vec3 getHorizontalLook() {
        return horizontalLookForYaw(getPartYaw());
    }

    public float getPartYaw() {
        return yBodyRot;
    }

    public float getPartYaw(float partialTick) {
        return Mth.rotLerp(partialTick, yBodyRotO, yBodyRot);
    }

    private Vec3 horizontalLookForYaw(float yawDegrees) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw)).normalize();
    }

    public Vec3 getHeadAnchor() {
        return position().add(0.0D, getBbHeight() - 0.08D, 0.0D);
    }

    public Vec3 getFistAnchor(boolean left) {
        return fistAnchorAt(position(), getPartYaw(), left, 2.35D);
    }

    public Vec3 getFistAnchor(boolean left, float partialTick) {
        return fistAnchorAt(getPosition(partialTick), getPartYaw(partialTick), left, 2.35D);
    }

    public Vec3 getFistChainAnchor(boolean left) {
        return fistAnchorAt(position(), getPartYaw(), left, 1.55D);
    }

    public Vec3 getFistChainAnchor(boolean left, float partialTick) {
        return fistAnchorAt(getPosition(partialTick), getPartYaw(partialTick), left, 1.55D);
    }

    private Vec3 fistAnchorAt(Vec3 base, float yaw, boolean left, double sideDistance) {
        Vec3 side = horizontalLookForYaw(yaw).cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize().scale(left ? -sideDistance : sideDistance);
        return base.add(0.0D, getBbHeight() * 0.62D, 0.0D).add(side);
    }

    public boolean isOwnedEntity(Entity entity) {
        if (entity == null || entity == this) {
            return true;
        }
        if (entity.getUUID().equals(headUuid) || entity.getUUID().equals(leftFistUuid) || entity.getUUID().equals(rightFistUuid)) {
            return true;
        }
        if (entity instanceof GolemFireballEntity projectile && projectile.getOwner() != null) {
            return isOwnedEntity(projectile.getOwner());
        }
        if (entity instanceof GolemEyeBeamEntity projectile && projectile.getOwner() != null) {
            return isOwnedEntity(projectile.getOwner());
        }
        return false;
    }

    private boolean isFriendlyDamage(DamageSource source) {
        return isOwnedEntity(source.getEntity()) || isOwnedEntity(source.getDirectEntity());
    }

    private boolean isBodyProtectedByHead() {
        TerrariaGolemHeadEntity head = getTrackedHead();
        return head == null ? getPhase() < 2 : head.isAlive() && !head.isDetached();
    }

    private void rememberBodyAttacker(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker && isValidTarget(attacker) && !isOwnedEntity(attacker)) {
            setTarget(attacker);
        }
    }

    public static boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return isValidTarget(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return isOwnedEntity(entity) || super.isAlliedTo(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) || source.is(DamageTypes.FALL) || isFriendlyDamage(source)) {
            return false;
        }
        if (isBodyProtectedByHead()) {
            rememberBodyAttacker(source);
            if (!level().isClientSide && tickCount % 6 == 0) {
                level().playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.7F, 0.55F);
            }
            return false;
        }
        if (source.getEntity() instanceof Mob mob && mob.getMobType() == MobType.UNDEAD) {
            amount *= 0.85F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        this.fallDistance = 0.0F;
        return false;
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
    public void die(DamageSource damageSource) {
        discardHead();
        discardFists();
        super.die(damageSource);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (headUuid != null) {
            tag.putUUID("Head", headUuid);
        }
        if (leftFistUuid != null) {
            tag.putUUID("LeftFist", leftFistUuid);
        }
        if (rightFistUuid != null) {
            tag.putUUID("RightFist", rightFistUuid);
        }
        tag.putBoolean("LeftFistDefeated", leftFistDefeated);
        tag.putBoolean("RightFistDefeated", rightFistDefeated);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Head")) {
            headUuid = tag.getUUID("Head");
        }
        if (tag.hasUUID("LeftFist")) {
            leftFistUuid = tag.getUUID("LeftFist");
        }
        if (tag.hasUUID("RightFist")) {
            rightFistUuid = tag.getUUID("RightFist");
        }
        leftFistDefeated = tag.getBoolean("LeftFistDefeated");
        rightFistDefeated = tag.getBoolean("RightFistDefeated");
        entityData.set(LEFT_FIST_ALIVE, !leftFistDefeated);
        entityData.set(RIGHT_FIST_ALIVE, !rightFistDefeated);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bodyBossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bodyBossEvent.removePlayer(player);
    }

    public int getAttackState() {
        return entityData.get(ATTACK_STATE);
    }

    public int getAttackTicks() {
        return entityData.get(ATTACK_TICKS);
    }

    public int getPhase() {
        return entityData.get(PHASE);
    }

    public float getLeftArmExtension() {
        return entityData.get(LEFT_ARM_EXTENSION);
    }

    public float getRightArmExtension() {
        return entityData.get(RIGHT_ARM_EXTENSION);
    }

    public boolean isLeftFistAlive() {
        return entityData.get(LEFT_FIST_ALIVE);
    }

    public boolean isRightFistAlive() {
        return entityData.get(RIGHT_FIST_ALIVE);
    }
}
