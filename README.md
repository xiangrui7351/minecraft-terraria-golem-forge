# Terraria Golem Forge / 泰拉瑞亚石巨人 Forge 模组

## 中文介绍

Terraria Golem Forge 是一个适用于 Minecraft Forge 1.20.1 / 47.4.20 的 Boss 模组。它在 Minecraft 中加入了一个受 Terraria 石巨人启发的丛林神庙石像 Boss，并使用原创的 Minecraft 风格像素材质表现石质身躯、分离头部、胸口核心、重型手臂和链拳攻击。

玩家可以放置“丛林神庙祭坛”，再使用“丛林蜥蜴电池”召唤 Boss。战斗包含本体、头部、左拳和右拳等多个可交互部位：头部会发射火球和眼部光束，链拳会伸出追击玩家，本体会跳跃并发动范围震地攻击。头部被打到临界状态后会脱离本体并持续攻击，此时本体才会成为主要击杀目标。

本模组包含 Boss 血条、持久化实体、召唤方块、召唤物品、刷怪蛋、掉落表、宝藏袋和服务端安全的投射物逻辑。所有材质均为原创像素风格，不复制 Terraria 原版素材。

## English Introduction

Terraria Golem Forge is a Minecraft Forge 1.20.1 / 47.4.20 boss mod. It adds a Minecraft-styled jungle temple stone idol boss inspired by Terraria's Golem, with original pixel-art textures for the stone body, detached head, chest core, heavy arms, and chained fists.

Players can place a Lihzahrd Altar and use a Lihzahrd Power Cell on it to summon the boss. The fight uses multiple interactive parts: body, head, left fist, and right fist. The head fires bouncing fireballs and eye beams, the chained fists extend toward players, and the body leaps into heavy ground-slam attacks. Once the head reaches its breaking point, it detaches and keeps attacking while the body becomes the main kill target.

The mod includes boss-bar status, persistent entities, a summon block, summon item, spawn egg, loot tables, treasure bag rewards, and server-safe projectile logic. All textures are original Minecraft-style pixel art and do not copy Terraria's original assets.

## 基本信息 / Basic Info

- Minecraft: `1.20.1`
- Forge: `47.4.20`
- Mod ID: `terrariagolem`
- Version: `1.0.0`
- Creative tab: `Terraria Golem`
- Boss entity: `terrariagolem:terraria_golem`
- Spawn egg: Lihzahrd Golem Spawn Egg / 丛林神庙石巨人刷怪蛋
- Summon block: Lihzahrd Altar / 丛林神庙祭坛
- Summon item: Lihzahrd Power Cell / 丛林蜥蜴电池

## 玩法 / Gameplay

中文：

1. 放置“丛林神庙祭坛”。
2. 手持“丛林蜥蜴电池”右键祭坛。
3. 附近没有已苏醒石巨人时，Boss 会被召唤出来。
4. 先处理头部和链拳，再进入攻击本体的阶段。

English:

1. Place a Lihzahrd Altar.
2. Right-click the altar with a Lihzahrd Power Cell.
3. If no Lihzahrd Golem is already awake nearby, the boss is summoned.
4. Fight through the head and chained fists before finishing the body.

## Boss 行为 / Boss Behavior

- 中文：大型石像本体配有分离头部、胸口核心、重型手臂和链拳。
- English: Large stone idol body with a separated head, chest core, heavy arms, and chained fists.
- 中文：本体、头部、左拳和右拳拥有独立状态；头部未脱离前，本体受到保护。
- English: The body, head, left fist, and right fist have separate states; the body is protected until the head detaches.
- 中文：头部会发射弹跳火球，半血以下会加入连续眼部光束。
- English: The head fires bouncing fireballs and adds repeated eye beams below half health.
- 中文：头部被击破后会进入离体阶段，变为无敌并继续发射光束。
- English: When depleted, the head detaches, becomes invulnerable, and keeps firing beams.
- 中文：左右拳各有独立碰撞箱和血量，被击毁后本场战斗不会再回来。
- English: Each fist has its own hitbox and health; destroyed fists stay gone for that fight.
- 中文：本体会随着血量降低或拳头被摧毁而更频繁地跳跃震地，并免疫坠落伤害。
- English: The body stomps more aggressively as its health falls or fists are destroyed, and it ignores fall damage.
- 中文：Boss 部件、火球和光束之间有友伤过滤，避免互相误伤。
- English: Friendly-fire filters prevent boss parts, fireballs, and beams from damaging each other.
- 中文：击败后掉落石巨人宝藏袋，可开出太阳核心、甲虫外壳、金锭，并有概率获得新的电池。
- English: The boss drops a Golem Treasure Bag with Sun Core, Beetle Husk, gold, and a chance for another Power Cell.

## 构建 / Build

Use Java 17 and run the Gradle build from the project root.

中文：请先安装 Java 17，并确保 `JAVA_HOME` 指向 Java 17，或在 IDE 中选择 Java 17。

English: Install Java 17 first, then make sure `JAVA_HOME` points to Java 17 or select Java 17 in your IDE.

Build the installable jar with your local Gradle setup:

```powershell
gradle build
```

Output:

```text
build/libs/terrariagolem-1.0.0.jar
```
