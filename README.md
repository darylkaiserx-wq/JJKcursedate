<!-- coderabbit-ai-generated:v1 -->

<!-- coderabbit-ai-generated-section:v1:b19e39f6ed042878b18be2df148b271ef77396f81d8db66ee79bc5bfca78e5d8 -->

<!-- coderabbit-ai-generated-section:v1:74bff34f314692a9d00a89018bfb177a38463574ab5ecc2fecc8338c47db0af4 -->

<!-- coderabbit-ai-generated-section:v1:1fc5951c3d1a37efd6bd412584a8f47136a934fac35e8bce17454de362dac68d -->

<!-- coderabbit-ai-generated-section:v1:08f63b4e0cdb2704457bfbed7376a897988eaad689fedec97a16871162c6ee72 -->

<!-- coderabbit-ai-generated-section:v1:301e6b19810b4ae8c6e9a794995754d57a0c6f65aae92d04b2602221a02a5eb9 -->

<!-- coderabbit-ai-generated-section:v1:ae01d32aeae6d3131c0a088489bb01fcaedeb24faa4d9bbf06bd5ee60f4e6b39 -->

<!-- coderabbit-ai-generated-section:v1:5b7f1515b51329bbbe184ed67eb4d09ed3c104a9bea9b95d1c2eab1d82681af8 -->

<!-- coderabbit-ai-generated-section:v1:6f4b901c61b9fac0f4008a849abc82e2e6c77a91b563a68671921357fe9f9f4e -->

# JJK Cursed Fate — Forge 1.20.1

A playable development alpha for Minecraft Java 1.20.1 + Forge 47.3.0. Install the mod on both client and server. No animation library or other mod dependency is required. Java 17 is required for development. This is a fan project with stylized Minecraft assets and user-supplied visual references.

**This build is not the complete requested canonical mod.** It registers 68 characters, 204 named move definitions, 14 domains, 14 cursed tools and 8 armor sets. Named moves reuse 22 shared effect archetypes; they are bounded Minecraft adaptations, not 204 individually simulated anime abilities. The catalog documents coverage.

## Play

1. Install Minecraft Java 1.20.1 and Forge 47.3.0. Put `jjkcursedate-0.4.0.jar` in `mods/`.
2. In creative mode, open your inventory and click **JJK Characters** at the bottom of the screen. You can choose or change a path without a book. Switching preserves overall XP/rank, clears path mastery and queued attacks, and ends your domain. Survival retains its one-time book choice.
3. Join a world. The Fate Book is granted once. The character selector automatically appears on joining with no chosen path. Right-click it, search/select a character and click **Choose character**. Selection consumes the book and closes the UI after server confirmation. Your path persists through death and reconnect.
4. Use **Z to cycle techniques** and **X to cast the selected technique**; **C quick punch / H heavy punch / J six-hit barrage**; R for Domain Expansion; V for RCT; G for Simple Domain; B for reinforcement; K for the journal. Rebind in Minecraft Controls. Scroll the right side of the book to read details at smaller GUI sizes.
5. Techniques II and III unlock at levels 8 and 18; RCT at 24; innate domains at 30. Kill hostile mobs/curses for XP. Use `/jjk mission` to claim a reward every five kills.
6. Promote through Grade 4 → Grade 3 → Grade 2 → Grade 1 → Special Grade in the journal. Requirements: levels 8 / 18 / 32 / 48 plus 5 / 10 / 15 / 20 kills since the previous promotion. Health bonus caps at +16 health points (+8 hearts), with +8 further health points for Heavenly Restriction; passive rank damage reduction caps at 16%.

## Selection and movement fix (0.3.1)

The creative inventory now has a persistent character-menu button. Periodic state sync no longer closes the selector; it closes after the chosen path is confirmed. All roster player and NPC models have distinct walking strides and forward-leaning sprint poses, with combat poses taking priority. Airborne, swimming, crouching and mounted movement retain vanilla poses.

## Texture and animation update (0.3.0)

- All 68 roster skins now use 128×128 UV textures with shaded faces, clothes, cuffs, boots, individual palettes and selected character markings. Hair/curse silhouettes also apply to chosen player skins. The supplied trendyguy skin guides fabric shading; this is Minecraft pixel art, not photorealistic anime models.
- Fourteen distinct 3D tool silhouettes use metal edges, wrapped grips, inlays and hand transforms. Weapon animation moves the held model and player arm; chains and staffs do not have separate articulated segments.
- Twelve six-frame animated technique sprite sets include Black Flash and blood-orb effects derived from the supplied images, plus Blue, Red, Purple, slash, fire, ice, lightning, curse, heal and barrier.
- Sixteen 128×64 armor atlases now share the matching character clothing details across all eight outfits.
- Five new building materials: plaster, roof tiles, timber panels, rune stone and paving. Generated buildings gain tiled roofs and lighting; existing generated structures are not retroactively replaced. Materials are in the main creative tab and drop themselves when mined.
- **JJK • Characters & Summons** creative tab contains 68 character eggs and eight summon eggs. Egg-spawned shikigami remain as unowned exhibits; technique summons retain ownership, combat and expiry.
- Nine original synthesized combat sounds join impact/domain effects, with subtitles. No licensed character voices or music.
- Quick punches are free, heavy punches cost 8 CE with an eight-tick windup, and a six-hit barrage costs 16 CE. Shared combo cooldowns prevent overlapping punches. Barrage alone bypasses hurt cooldowns; friendly NPC/PVP protection remains. Maki and Toji pay no CE for punches.
- Protocol 3 requires the same mod version on client and server. All combat keys can be rebound in Controls. H avoids the vanilla F swap-hands binding.

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

## Cursed item loot (0.4.0)

Newly generated city buildings, residences and curse ruins contain loot chests. Every chest has 1–4 curse fragments. City chests have a 55% chance of one cursed tool and a 12% chance of a relic; residence chests have 65% and 20%; ruins have 100% and 45%. Each tool and relic can appear in every chest type; four common tools have three times the weight of the others. Every city has four chests, one per building; residences and ruins have one each. Existing explored chunks do not get retroactive chests. Operators can use `/jjk structure <city|residence|ruins>` on disposable ground to generate a site.

All 14 existing cursed tools can be found, alongside 11 new collectible cursed objects: Sukuna's Finger, Prison Realm, and Death Paintings 1–9. The new objects have 3D models and are collectibles only: possession, sealing and reincarnation mechanics are not implemented. They also appear in the JJK creative tab. These additions do not represent every canonical cursed item. Loot tables can be customized with a data pack under `jjkcursedate:chests/city`, `residence`, and `ruins`.

## World and combat

Rare city blocks, residences and curse ruins generate in new Overworld chunks. City buildings have accessible ladder shafts. Grasshopper curses and finger bearers spawn in darkness. Other NPCs use spawn eggs or commands; they are not all naturally generated. Trainers open the journal; civilians provide ambient dialogue. Named NPCs have textured humanoid models, selected silhouette additions, walking/attack/hurt/death poses and casting arm animation. Choosing a character also changes player appearance, including first-person arms. Synchronized keyframed casting, quick punch, heavy windup, alternating barrage, Black Flash and weapon poses animate the player model. Empty-hand casting and barrage show both arms in first person. Subtle first-person pitch/roll can be disabled with `cameraMotion=false`. These are shared procedural clips, with a one-hand Gojo casting variation; they are not separate cinematic choreography for every character. Summons have distinct meshes and procedural leg, arm or wing motion.

Techniques and domains do not change terrain. Domains last 12 seconds, have a default 12-block radius and an upper bound of eight active per dimension; summons expire after 30 seconds. Default technique PVP is disabled. Config: `config/jjkcursedate-common.toml`, including particle budget, domain radius, camera motion and technique PVP. This limits effects; no FPS or multiplayer load guarantee has been measured.

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

Use Java 17. To regenerate assets in order: `python3 scripts/catalog.py`, `python3 scripts/assets.py`, `python3 scripts/art_upgrade.py`, then `python3 scripts/sound_assets.py`, and finally `python3 scripts/loot_assets.py`. The art upgrade requires Pillow 13 (`python3 -m pip install Pillow==13.0.0` in a virtual environment); sound generation requires ffmpeg with libvorbis. `assets.py` alone overwrites upgraded skins/models. `art/references/` preserves the four user-provided source images; the Gojo JPEG is a visual palette reference, not an in-game portrait. The mod JAR is in `build/libs/`. Tests are Forge GameTests and require no Minecraft account. A Java client is required to play; web Preview cannot execute a Minecraft mod.

## Remaining full-mod work

The full canon roster, remaining shikigami models, every technique and domain, character transformations, complete narrative quests, cinematic animation clips, faithful domain interiors, all clothes and weapons, and exhaustive multiplayer/performance verification remain unfinished. Eight summon forms now have original animated models: Divine Dog, Nue, Rika manifestation, cursed spirit, Moon Dregs, Garuda, Death Swarm and Mahoraga. Their combat behavior is simplified; no full shikigami taming/adaptation simulation is implemented. Domains use colored particle borders and status/damage pulses rather than canonical environments. Civilian paths receive clearly noncanonical support adaptations. Character portraits, licensed voices/music and copied anime textures are not included.


<!-- /coderabbit-ai-generated:v1 -->