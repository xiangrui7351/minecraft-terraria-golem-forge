# Terraria Golem Forge

Forge 1.20.1 / 47.4.20 mod adding a Minecraft-styled stone idol boss inspired by Terraria's Golem.

## Build

This project is pinned to Java 17 in `gradle.properties`:

```properties
org.gradle.java.home=D:/program files/Java/jdk-17
```

Build the installable jar:

```powershell
& 'C:\Users\Administrator\.gradle\wrapper\dists\gradle-8.10.2-bin\5mbnaactcw1nh6vw2lf972n0x\gradle-8.10.2\bin\gradle.bat' build
```

Output:

```text
build/libs/terrariagolem-1.0.0.jar
```

## In Game

- Creative tab: Terraria Golem
- Boss entity: `terrariagolem:terraria_golem`
- Spawn egg: Lihzahrd Golem Spawn Egg
- Summon block: Lihzahrd Altar
- Summon item: Lihzahrd Power Cell

Place the Lihzahrd Altar and use a Lihzahrd Power Cell on it to summon the boss.

## Boss Behavior

- Large stone idol body with separated head, chest core, heavy arms, chain fists, and original pixel textures.
- Four health bars: body, head, left fist, and right fist have independent health pools.
- Phase 1: the head launches bouncing fireballs while the left or right chained fist extends toward valid player positions.
- Head: below 50% head health it keeps the fireballs and adds repeated eye beams.
- Detached phase: when head health is depleted, the head separates, becomes invulnerable, and keeps firing eye beams while the body becomes the kill target.
- Fists: each fist has its own hitbox and health. A depleted fist disappears permanently for that fight.
- Body: jumps and stomps more aggressively as its own health falls or fists are destroyed, and it ignores fall damage during its attacks.
- Friendly-fire filters prevent the body, head, fists, fireballs, and eye beams from damaging their own boss parts.
- Drops: the boss now drops a Golem Treasure Bag. Open it for Sun Core, Beetle Husk, gold, and a chance at another Power Cell.
- Boss bar, persistent entity behavior, fire immunity, loot table, and server-safe projectile logic are included.

Textures are original Minecraft-style pixel art and do not copy Terraria's original assets.
