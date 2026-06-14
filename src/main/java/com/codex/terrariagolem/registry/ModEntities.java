package com.codex.terrariagolem.registry;

import com.codex.terrariagolem.TerrariaGolemMod;
import com.codex.terrariagolem.world.entity.GolemEyeBeamEntity;
import com.codex.terrariagolem.world.entity.GolemFireballEntity;
import com.codex.terrariagolem.world.entity.TerrariaGolemEntity;
import com.codex.terrariagolem.world.entity.TerrariaGolemFistEntity;
import com.codex.terrariagolem.world.entity.TerrariaGolemHeadEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TerrariaGolemMod.MODID);

    public static final RegistryObject<EntityType<TerrariaGolemEntity>> TERRARIA_GOLEM = ENTITY_TYPES.register("terraria_golem",
            () -> EntityType.Builder.of(TerrariaGolemEntity::new, MobCategory.MONSTER)
                    .sized(2.9F, 3.05F)
                    .clientTrackingRange(12)
                    .updateInterval(2)
                    .fireImmune()
                    .build("terraria_golem"));

    public static final RegistryObject<EntityType<TerrariaGolemHeadEntity>> TERRARIA_GOLEM_HEAD = ENTITY_TYPES.register("terraria_golem_head",
            () -> EntityType.Builder.of(TerrariaGolemHeadEntity::new, MobCategory.MONSTER)
                    .sized(2.45F, 2.1F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .fireImmune()
                    .build("terraria_golem_head"));

    public static final RegistryObject<EntityType<TerrariaGolemFistEntity>> TERRARIA_GOLEM_FIST = ENTITY_TYPES.register("terraria_golem_fist",
            () -> EntityType.Builder.of(TerrariaGolemFistEntity::new, MobCategory.MONSTER)
                    .sized(1.8F, 1.65F)
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .fireImmune()
                    .build("terraria_golem_fist"));

    public static final RegistryObject<EntityType<GolemFireballEntity>> GOLEM_FIREBALL = ENTITY_TYPES.register("golem_fireball",
            () -> EntityType.Builder.<GolemFireballEntity>of(GolemFireballEntity::new, MobCategory.MISC)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("golem_fireball"));

    public static final RegistryObject<EntityType<GolemEyeBeamEntity>> GOLEM_EYE_BEAM = ENTITY_TYPES.register("golem_eye_beam",
            () -> EntityType.Builder.<GolemEyeBeamEntity>of(GolemEyeBeamEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("golem_eye_beam"));

    private ModEntities() {
    }
}
