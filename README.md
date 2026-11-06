<!-- coderabbit-ai-generated:v1 -->

<!-- coderabbit-ai-generated-section:v1:5b7f1515b51329bbbe184ed67eb4d09ed3c104a9bea9b95d1c2eab1d82681af8 -->

<!-- coderabbit-ai-generated-section:v1:6f4b901c61b9fac0f4008a849abc82e2e6c77a91b563a68671921357fe9f9f4e -->

# JJK Cursed Fate — Forge 1.20.1

A playable development alpha for Minecraft Java 1.20.1 + Forge 47.3.0. Install the mod on both client and server. No animation library or other mod dependency is required. Java 17 is required for development. This is a fan project with original, stylized Minecraft assets.

**This build is not the complete requested canonical mod.** It registers 68 characters, 204 named move definitions, 14 domains, 14 cursed tools and 8 armor sets. Named moves reuse 22 shared effect archetypes; they are bounded Minecraft adaptations, not 204 individually simulated anime abilities. The catalog documents coverage.

## Play

1. Install Minecraft Java 1.20.1 and Forge 47.3.0. Put `jjkcursedate-0.2.0.jar` in `mods/`.
2. Join a world. The Fate Book is granted once. Right-click it, search/select a character and click **Choose character**. Selection consumes the book and closes the UI after server confirmation. Your path persists through death and reconnect.
3. Use Z / X / C for techniques; R for Domain Expansion; V for RCT; G for Simple Domain; B for reinforcement; K for the journal. Rebind in Minecraft Controls. Scroll the right side of the book to read details at smaller GUI sizes.
4. Techniques II and III unlock at levels 8 and 18; RCT at 24; innate domains at 30. Kill hostile mobs/curses for XP. Use `/jjk mission` to claim a reward every five kills.
5. Promote through Grade 4 → Grade 3 → Grade 2 → Grade 1 → Special Grade in the journal. Requirements: levels 8 / 18 / 32 / 48 plus 5 / 10 / 15 / 20 kills since the previous promotion. Health bonus caps at +16 health points (+8 hearts), with +8 further health points for Heavenly Restriction; passive rank damage reduction caps at 16%.

## Ten gameplay additions

1. Timed Black Flash: melee within half a second of casting; costs energy, has a cooldown.
2. Repeatable five-exorcism missions with XP and curse-fragment rewards.
3. Promotion trials requiring both experience and combat progress.
4. Recruitable allies: interact to follow, interact again to dismiss.
5. Toggle cursed reinforcement with sustained energy drain.
6. Reverse Cursed Technique healing with a level gate and cooldown.
7. Simple Domain protects against domain pulses for five seconds.
8. Domain clashes compare level and rank; equal barriers cancel.
9. Temporary summons with a per-caster limit and automatic expiry.
10. Craftable 3D cursed tools with on-hit effects, plus craftable outfits.

Additional mechanics: each combat technique gains mastery from casts aimed at enemies (levels 1–10, lower energy cost and higher damage). Maki and Toji use Heavenly Restriction: zero CE, free physical techniques, extra health and speed; they cannot use RCT or CE defenses. `/jjk vow` trades energy regeneration and defense for damage; `/jjk amplify` suppresses innate techniques while enabling amplification against Infinity. `/jjk summon` lets Megumi summon Mahoraga at level 25 for 70 CE, with a 30-second cooldown.

## World and combat

Rare city blocks, residences and curse ruins generate in new Overworld chunks. City buildings have accessible ladder shafts. Grasshopper curses and finger bearers spawn in darkness. Other NPCs use spawn eggs or commands; they are not all naturally generated. Trainers open the journal; civilians provide ambient dialogue. Named NPCs have textured humanoid models, selected silhouette additions, walking/attack/hurt/death poses and casting arm animation. Choosing a character also changes player appearance, including first-person arms. First-person casting moves the held hand. Summons have distinct meshes and procedural leg, arm or wing motion.

Techniques and domains do not change terrain. Domains last 12 seconds, have a default 12-block radius and an upper bound of eight active per dimension; summons expire after 30 seconds. Default technique PVP is disabled. Config: `config/jjkcursedate-common.toml`, including particle budget, domain radius and technique PVP. This limits effects; no FPS or multiplayer load guarantee has been measured.

## Commands

- Players: `/jjk help`, `/jjk status`, `/jjk book`, `/jjk cast <1-3>`, `/jjk domain`, `/jjk rct`, `/jjk guard`, `/jjk reinforce`, `/jjk mission`, `/jjk promote`, `/jjk summon`, `/jjk vow`, `/jjk amplify`.
- Operators: `/jjk spawn <id>`, `/jjk xp <amount>`, `/jjk reset`, `/jjk structure <city|residence|ruins>`. Structure commands replace a 15×15×18 area four blocks southeast of the caller; use on disposable space.
- Replacement Fate Book recipe: book + amethyst shard. It cannot change an already chosen path. Operators can reset a path.

## Development

```sh
./gradlew build
./gradlew runGameTestServer
python3 scripts/check_resources.py
./gradlew runClient
```

Use Java 17. `scripts/catalog.py` regenerates the roster; `scripts/assets.py` regenerates original textures, models, recipes and world data. The mod JAR is in `build/libs/`. Tests are Forge GameTests and require no Minecraft account. A Java client is required to play; web Preview cannot execute a Minecraft mod.

## Remaining full-mod work

The full canon roster, remaining shikigami models, every technique and domain, character transformations, complete narrative quests, cinematic animation clips, faithful domain interiors, all clothes and weapons, and exhaustive multiplayer/performance verification remain unfinished. Eight summon forms now have original animated models: Divine Dog, Nue, Rika manifestation, cursed spirit, Moon Dregs, Garuda, Death Swarm and Mahoraga. Their combat behavior is simplified; no full shikigami taming/adaptation simulation is implemented. Domains use colored particle borders and status/damage pulses rather than canonical environments. Civilian paths receive clearly noncanonical support adaptations. Character portraits, licensed voices/music and copied anime textures are not included.


<!-- /coderabbit-ai-generated:v1 -->